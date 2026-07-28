package com.sololifeos.ai.memory;

import java.util.List;

/**
 * AI 长期记忆服务接口。
 *
 * Memory 是提炼后的持久知识（与 Conversation 短期对话互补）。
 * Memory 数据存 ai_memory 表，向量存 Vector DB（经 VectorStoreAdapter）。
 *
 * Sprint 0 仅定义接口，不实现（Sprint 5）。
 */
public interface MemoryService {

    /**
     * 存储记忆。
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型（PREFERENCE / BEHAVIOR / EMOTION / GOAL / EVENT / GENERAL）
     * @param summary 摘要（用于检索展示）
     * @param content 完整内容
     * @return 记忆 ID
     */
    Long store(Long userId, String memoryType, String summary, String content);

    /**
     * 检索用户相关记忆。
     *
     * @param userId 用户 ID
     * @param query  查询条件
     * @param limit  返回条数上限
     * @return 记忆列表
     */
    List<String> retrieve(Long userId, String query, int limit);

    /**
     * 删除用户记忆（支持数据删除，PROJECT_CONTEXT §13）。
     *
     * @param userId 用户 ID
     */
    void deleteByUser(Long userId);
}
