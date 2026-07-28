package com.sololifeos.ai.memory;

import java.util.List;

/**
 * AI 短期对话上下文服务接口。
 *
 * Conversation 记录原始交互流（与 Memory 长期记忆互补）。
 * 数据存 ai_conversation 表。
 *
 * Sprint 0 仅定义接口，不实现（Sprint 5）。
 */
public interface ConversationService {

    /**
     * 记录对话消息。
     *
     * @param userId    用户 ID
     * @param agentType Agent 类型
     * @param role      对话角色（USER / ASSISTANT / SYSTEM）
     * @param content   消息内容
     * @param tokenCount Token 消耗（仅 ASSISTANT 消息，其他传 null）
     */
    void record(Long userId, String agentType, String role, String content, Integer tokenCount);

    /**
     * 获取用户与指定 Agent 的近期对话。
     *
     * @param userId    用户 ID
     * @param agentType Agent 类型
     * @param limit     返回条数上限
     * @return 对话消息列表
     */
    List<String> getRecentConversation(Long userId, String agentType, int limit);

    /**
     * 清除用户对话历史（支持数据删除）。
     *
     * @param userId 用户 ID
     */
    void clearByUser(Long userId);
}
