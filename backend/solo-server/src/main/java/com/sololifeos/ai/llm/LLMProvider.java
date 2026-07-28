package com.sololifeos.ai.llm;

/**
 * LLM Provider 模型调用抽象层接口。
 *
 * 换 GPT / GLM / Claude / Qwen / DeepSeek 不影响业务（ARCHITECTURE §7）。
 * Provider 选型在 Sprint 5 决策（ADR-0008）。
 *
 * Sprint 0 仅定义接口，不实现（Sprint 5）。
 */
public interface LLMProvider {

    /**
     * 调用 LLM 生成回复。
     *
     * @param prompt   输入 prompt
     * @param maxTokens 最大 token 数
     * @return LLM 生成结果
     */
    LLMResponse generate(String prompt, int maxTokens);

    /**
     * LLM Provider 类型标识。
     */
    String getProviderType();
}
