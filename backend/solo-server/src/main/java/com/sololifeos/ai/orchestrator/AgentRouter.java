package com.sololifeos.ai.orchestrator;

import com.sololifeos.ai.agents.Agent;
import com.sololifeos.ai.agents.AgentResult;
import com.sololifeos.ai.agents.Context;

/**
 * Agent Router 统一路由接口（ADR-0003）。
 *
 * 所有 AI Agent 必须经 Router 路由，禁止 Agent 之间直接相互调用。
 * Agent 之间需要协作时，通过 Memory Layer 共享上下文。
 *
 * Sprint 0 仅定义接口，不实现路由策略（Sprint 5）。
 */
public interface AgentRouter {

    /**
     * 路由请求到具体 Agent 并执行。
     *
     * @param agentType 目标 Agent 类型
     * @param context   执行上下文
     * @return Agent 执行结果
     */
    AgentResult route(String agentType, Context context);

    /**
     * 注册 Agent 到 Router。
     *
     * @param agent Agent 实现
     */
    void registerAgent(Agent agent);
}
