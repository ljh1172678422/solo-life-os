package com.sololifeos.ai.agents.planner;

import com.sololifeos.ai.agents.Agent;
import com.sololifeos.ai.agents.AgentResult;
import com.sololifeos.ai.agents.Context;
import com.sololifeos.ai.agents.PlannerContext;
import com.sololifeos.ai.memory.MemoryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Planner Agent 骨架实现（TASK-0207, ARCHITECTURE §8）。
 * <p>
 * 职责：生成每日计划的活动建议列表。
 * <p>
 * 输入：{@link PlannerContext}（时间 / 地点 / 天气 / 心情 / 用户偏好），
 * 通过 {@link Context#getAttributes()} 的 {@link PlannerContext#KEY} 传入。
 * <p>
 * 依赖：
 * <ul>
 *   <li>{@link MemoryService}：检索用户长期记忆（偏好 / 历史），Sprint 5 前用 {@link com.sololifeos.ai.memory.MockMemoryService}</li>
 *   <li>LLM Provider：正式实现归 Sprint 5（ADR-0008），本骨架用规则模板生成 Mock 计划，不调用真实 LLM</li>
 * </ul>
 * <p>
 * 关键约束（ARCHITECTURE §21）：
 * <ul>
 *   <li>Agent 不直接持久化业务数据，产出 {@link AgentResult} 由调用方通过 Today Domain API 落库</li>
 *   <li>Agent 不持有 Repository 引用</li>
 *   <li>本骨架产出为 JSON 字符串活动建议列表，调用方解析后调用 TodayApplicationService 创建计划 + 活动</li>
 * </ul>
 * <p>
 * DoD（SPRINT_PLAN Sprint 2）：Planner Agent 接口定义完成（实现可 Mock）。
 */
public class PlannerAgent implements Agent {

    /** Agent 类型标识，用于 Router 路由。 */
    public static final String AGENT_TYPE = "PLANNER";

    /** Mock 计划生成的最大活动数。 */
    private static final int MAX_ACTIVITIES = 5;

    /** Memory 检索条数上限。 */
    private static final int MEMORY_RETRIEVE_LIMIT = 5;

    private final MemoryService memoryService;

    public PlannerAgent(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Override
    public AgentResult execute(Context context) {
        if (context == null || context.getUserId() == null) {
            return AgentResult.failure();
        }

        PlannerContext plannerContext = resolvePlannerContext(context);
        if (plannerContext == null) {
            return AgentResult.failure();
        }

        // 1. 检索用户长期记忆（偏好 / 历史行为），Sprint 5 前为 Mock 关键词匹配
        List<String> memories = memoryService.retrieve(
                context.getUserId(),
                buildMemoryQuery(plannerContext),
                MEMORY_RETRIEVE_LIMIT);

        // 2. 生成活动建议（骨架阶段用规则模板，Sprint 5 改为 LLM Provider 生成）
        List<ActivitySuggestion> suggestions = generateMockSuggestions(plannerContext, memories);

        // 3. 序列化为 JSON 供调用方解析（AgentResult.content 为 String）
        String json = serializeSuggestions(suggestions);
        int tokenCount = estimateTokenCount(json);

        return AgentResult.success(json, tokenCount);
    }

    @Override
    public String getAgentType() {
        return AGENT_TYPE;
    }

    // --- 内部方法 ---

    /** 从 Context.attributes 提取 PlannerContext，缺失时回退到最小上下文。 */
    private PlannerContext resolvePlannerContext(Context context) {
        if (context.getAttributes() != null) {
            Object raw = context.getAttributes().get(PlannerContext.KEY);
            if (raw instanceof PlannerContext pc) {
                return pc;
            }
        }
        // 回退：仅有 userId，date 默认今天
        if (context.getUserId() != null) {
            return PlannerContext.minimal(context.getUserId(), LocalDate.now());
        }
        return null;
    }

    /** 构造 Memory 检索 query，结合心情 / 偏好提升相关性。 */
    private String buildMemoryQuery(PlannerContext pc) {
        StringBuilder sb = new StringBuilder();
        if (pc.mood() != null && !pc.mood().isBlank()) {
            sb.append(pc.mood());
        }
        if (pc.preferences() != null && !pc.preferences().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(String.join(" ", pc.preferences()));
        }
        return sb.toString();
    }

    /**
     * 规则模板生成 Mock 活动建议。
     * <p>
     * Sprint 5 将替换为 LLM Provider 基于 prompt + memory 生成。
     * 骨架阶段按时段（早 / 午 / 晚）+ 心情 / 偏好组合选模板，保证产出可演示。
     */
    private List<ActivitySuggestion> generateMockSuggestions(PlannerContext pc, List<String> memories) {
        List<ActivitySuggestion> result = new ArrayList<>();
        LocalDate date = pc.date() != null ? pc.date() : LocalDate.now();

        // 早晨：根据天气选户外 / 室内
        boolean outdoor = isOutdoorWeather(pc.weather());
        result.add(new ActivitySuggestion(
                outdoor ? "晨间散步" : "晨间拉伸",
                outdoor ? "EXPLORE" : "SPORT",
                LocalDateTime.of(date, java.time.LocalTime.of(7, 30))));

        // 上午：工作 / 学习（偏好 quiet 时倾向 STUDY）
        boolean preferQuiet = prefers(pc, "quiet");
        result.add(new ActivitySuggestion(
                preferQuiet ? "专注学习时段" : "处理今日要务",
                preferQuiet ? "STUDY" : "WORK",
                LocalDateTime.of(date, java.time.LocalTime.of(9, 0))));

        // 午后：休闲（心情 tired 时倾向 REST）
        boolean tired = "tired".equalsIgnoreCase(pc.mood());
        result.add(new ActivitySuggestion(
                tired ? "午休小憩" : "午后阅读",
                tired ? "REST" : "LEISURE",
                LocalDateTime.of(date, java.time.LocalTime.of(13, 30))));

        // 傍晚：社交 / 运动
        result.add(new ActivitySuggestion(
                "傍晚活动",
                prefers(pc, "social") ? "SOCIAL" : "SPORT",
                LocalDateTime.of(date, java.time.LocalTime.of(17, 0))));

        // 晚间：放松
        result.add(new ActivitySuggestion(
                "晚间放松", "REST",
                LocalDateTime.of(date, java.time.LocalTime.of(20, 0))));

        // 限制最大数量
        if (result.size() > MAX_ACTIVITIES) {
            result = new ArrayList<>(result.subList(0, MAX_ACTIVITIES));
        }
        return result;
    }

    private boolean isOutdoorWeather(String weather) {
        if (weather == null || weather.isBlank()) {
            return true; // 缺省默认可户外
        }
        String lower = weather.toLowerCase();
        // 雨 / 雪 / 大风 / 雾霾 → 室内
        return !lower.contains("雨") && !lower.contains("雪")
                && !lower.contains("rain") && !lower.contains("snow")
                && !lower.contains("storm");
    }

    private boolean prefers(PlannerContext pc, String tag) {
        return pc.preferences() != null && pc.preferences().stream()
                .anyMatch(p -> p != null && p.equalsIgnoreCase(tag));
    }

    /** 简易 JSON 序列化（避免引入 Jackson 依赖到 AI 层骨架）。 */
    private String serializeSuggestions(List<ActivitySuggestion> suggestions) {
        StringBuilder sb = new StringBuilder("{\"activities\":[");
        for (int i = 0; i < suggestions.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            ActivitySuggestion s = suggestions.get(i);
            sb.append("{\"title\":\"").append(escape(s.title())).append("\"")
                    .append(",\"type\":\"").append(s.type()).append("\"")
                    .append(",\"startTime\":\"").append(s.startTime()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    /** 粗略 token 估算（4 字符 ≈ 1 token，中英混合近似）。 */
    private int estimateTokenCount(String text) {
        return text == null ? 0 : Math.max(1, text.length() / 4);
    }

    /** 活动建议内部结构（骨架阶段用 record，Sprint 5 改为 DTO）。 */
    public record ActivitySuggestion(String title, String type, LocalDateTime startTime) {
    }
}
