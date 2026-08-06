package com.sololifeos.ai;

import com.sololifeos.ai.agents.Agent;
import com.sololifeos.ai.agents.planner.PlannerAgent;
import com.sololifeos.ai.memory.MemoryService;
import com.sololifeos.ai.memory.MockMemoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 层 Mock Bean 配置（TASK-0207）。
 * <p>
 * Sprint 2 Planner Agent 骨架阶段，MemoryService 与 LLMProvider 尚未正式实现
 * （归 Sprint 5，ADR-0008 / Memory Service）。本配置注册 Mock 实现为 Spring Bean，
 * 使 PlannerAgent 可被注入到未来的 Application Service（如 TodayPlannerApplicationService），
 * 完成 Sprint 2 DoD「Planner Agent 接口定义完成（实现可 Mock）」。
 * <p>
 * Sprint 5 替换策略：删除本配置类的 Mock Bean 定义，改为正式 MemoryService / LLMProvider
 * 实现的 @Service / @Component 注解，PlannerAgent 注入点不变。
 */
@Configuration
public class AiConfig {

    /**
     * Mock 记忆服务（Sprint 5 替换为基于 ai_memory 表 + Vector DB 的实现）。
     */
    @Bean
    public MemoryService memoryService() {
        return new MockMemoryService();
    }

    /**
     * Planner Agent 骨架（Sprint 5 接入 LLM Provider 后升级生成逻辑）。
     */
    @Bean
    public Agent plannerAgent(MemoryService memoryService) {
        return new PlannerAgent(memoryService);
    }
}
