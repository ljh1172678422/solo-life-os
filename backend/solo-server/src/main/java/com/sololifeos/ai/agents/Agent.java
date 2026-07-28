package com.sololifeos.ai.agents;

/**
 * AI Agent 统一接口契约。
 *
 * 所有 AI Agent（Planner / Recommendation / Emotion / Story / Assistant）必须实现此接口。
 * Agent 不拥有业务状态，不直接持久化业务数据（ADR-0003 / ARCHITECTURE §21）。
 * Agent 产出必须通过业务模块的 Domain API 落库。
 *
 * Sprint 0 仅定义接口，不实现具体 Agent（Sprint 5）。
 */
public interface Agent {

    /**
     * 执行 Agent 任务。
     *
     * @param context Agent 执行上下文（用户 / 时间 / 位置 / 心情 / 偏好等）
     * @return Agent 执行结果
     */
    AgentResult execute(Context context);

    /**
     * Agent 类型标识，用于 Router 路由。
     */
    String getAgentType();
}
