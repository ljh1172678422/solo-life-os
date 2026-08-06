package com.sololifeos.ai.agents.planner;

import com.sololifeos.ai.agents.AgentResult;
import com.sololifeos.ai.agents.Context;
import com.sololifeos.ai.agents.PlannerContext;
import com.sololifeos.ai.memory.MockMemoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PlannerAgent} 骨架单元测试（TASK-0207）。
 * <p>
 * 验证 Agent 接口契约 + Mock 计划生成 + Memory 集成。
 * 不调用真实 LLM（骨架阶段用规则模板）。
 */
class PlannerAgentTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 6);

    @Nested
    @DisplayName("getAgentType 类型标识")
    class AgentTypeTest {

        @Test
        @DisplayName("返回 PLANNER 标识")
        void shouldReturnPlannerType() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());
            assertThat(agent.getAgentType()).isEqualTo("PLANNER");
        }
    }

    @Nested
    @DisplayName("execute 计划生成")
    class ExecuteTest {

        @Test
        @DisplayName("正常执行：返回成功结果 + JSON 活动列表")
        void shouldGeneratePlanSuccessfully() {
            MockMemoryService memory = new MockMemoryService();
            memory.store(USER_ID, "PREFERENCE", "喜欢安静", "详细");
            PlannerAgent agent = new PlannerAgent(memory);

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, "北京", "晴 28℃", "calm", List.of("quiet")));

            AgentResult result = agent.execute(context);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getContent()).contains("\"activities\"");
            assertThat(result.getContent()).contains("\"title\"");
            assertThat(result.getContent()).contains("\"startTime\"");
            assertThat(result.getTokenCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("晴天 → 早晨活动为户外 EXPLORE")
        void shouldSuggestOutdoorWhenSunny() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, "晴 28℃", null, List.of()));

            AgentResult result = agent.execute(context);

            assertThat(result.getContent()).contains("晨间散步");
            assertThat(result.getContent()).contains("EXPLORE");
        }

        @Test
        @DisplayName("雨天 → 早晨活动为室内 SPORT")
        void shouldSuggestIndoorWhenRainy() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, "小雨 15℃", null, List.of()));

            AgentResult result = agent.execute(context);

            assertThat(result.getContent()).contains("晨间拉伸");
            assertThat(result.getContent()).contains("SPORT");
        }

        @Test
        @DisplayName("偏好 quiet → 上午活动为 STUDY")
        void shouldSuggestStudyWhenPreferQuiet() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, null, null, List.of("quiet")));

            AgentResult result = agent.execute(context);

            assertThat(result.getContent()).contains("专注学习时段");
            assertThat(result.getContent()).contains("STUDY");
        }

        @Test
        @DisplayName("心情 tired → 午后活动为 REST")
        void shouldSuggestRestWhenTired() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, null, "tired", List.of()));

            AgentResult result = agent.execute(context);

            assertThat(result.getContent()).contains("午休小憩");
            assertThat(result.getContent()).contains("REST");
        }

        @Test
        @DisplayName("偏好 social → 傍晚活动为 SOCIAL")
        void shouldSuggestSocialWhenPreferSocial() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, null, null, List.of("social")));

            AgentResult result = agent.execute(context);

            assertThat(result.getContent()).contains("SOCIAL");
        }

        @Test
        @DisplayName("活动数量 ≤ 5")
        void shouldLimitActivityCount() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = buildContext(USER_ID, PlannerContext.minimal(USER_ID, DATE));

            AgentResult result = agent.execute(context);

            int activityCount = countOccurrences(result.getContent(), "\"title\":");
            assertThat(activityCount).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("execute 异常输入")
    class InvalidInputTest {

        @Test
        @DisplayName("context 为空：返回失败")
        void shouldFailWhenContextNull() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            AgentResult result = agent.execute(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getContent()).isNull();
        }

        @Test
        @DisplayName("userId 为空：返回失败")
        void shouldFailWhenUserIdNull() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = new Context();
            // userId 未设置（null）

            AgentResult result = agent.execute(context);

            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("缺 PlannerContext：回退到最小上下文（今天）仍成功")
        void shouldFallbackToMinimalContext() {
            PlannerAgent agent = new PlannerAgent(new MockMemoryService());

            Context context = new Context();
            context.setUserId(USER_ID);
            // attributes 为空，无 plannerContext key

            AgentResult result = agent.execute(context);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getContent()).contains("\"activities\"");
        }
    }

    @Nested
    @DisplayName("Memory 集成")
    class MemoryIntegrationTest {

        @Test
        @DisplayName("记忆存在时参与检索但不影响骨架模板输出结构")
        void shouldRetrieveMemoryButKeepOutputStructure() {
            MockMemoryService memory = new MockMemoryService();
            memory.store(USER_ID, "PREFERENCE", "喜欢户外安静", "详细内容");
            PlannerAgent agent = new PlannerAgent(memory);

            Context context = buildContext(USER_ID, new PlannerContext(
                    USER_ID, DATE, null, "晴", "calm", List.of("quiet")));

            AgentResult result = agent.execute(context);

            // 骨架阶段记忆仅参与检索 query 构造，不影响模板输出结构
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getContent()).contains("\"activities\"");
        }
    }

    // --- 辅助方法 ---

    private static Context buildContext(Long userId, PlannerContext plannerContext) {
        Context context = new Context();
        context.setUserId(userId);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(PlannerContext.KEY, plannerContext);
        context.setAttributes(attrs);
        return context;
    }

    private static int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
