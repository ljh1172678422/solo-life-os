package com.sololifeos.ai.agents;

/**
 * Planner Agent 专用输入上下文（TASK-0207）。
 * <p>
 * ARCHITECTURE §8 Planner Agent 输入：时间 / 地点 / 天气 / 心情 / 用户偏好。
 * 本 record 将散落字段结构化，避免用 {@link Context#getAttributes()} 的 Map 取值
 * 导致类型不安全。调用方构造 PlannerContext 后塞入 Context.attributes 供
 * {@link com.sololifeos.ai.agents.planner.PlannerAgent} 读取。
 *
 * @param userId        用户 ID
 * @param date          目标日期（计划生成日）
 * @param location      用户所在地点（可空，Sprint 3 Explore Module 后填充）
 * @param weather       天气描述（可空，如 "晴 / 28℃"，外部 API 失败降级为空）
 * @param mood          用户当前心情（可空，如 "calm" / "energetic" / "tired"）
 * @param preferences   用户偏好标签（可空，如 ["quiet", "outdoor", "low-budget"]）
 */
public record PlannerContext(
        Long userId,
        java.time.LocalDate date,
        String location,
        String weather,
        String mood,
        java.util.List<String> preferences
) {

    /** attributes Map 中存放 PlannerContext 的 key。 */
    public static final String KEY = "plannerContext";

    /** 构造最小可用上下文（仅 userId + date，其余为空）。 */
    public static PlannerContext minimal(Long userId, java.time.LocalDate date) {
        return new PlannerContext(userId, date, null, null, null, java.util.List.of());
    }
}
