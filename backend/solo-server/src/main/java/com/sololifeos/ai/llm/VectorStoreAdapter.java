package com.sololifeos.ai.llm;

import java.util.List;
import java.util.Map;

/**
 * Vector Store Adapter 接口（ADR-0005）。
 *
 * 隔离具体 Vector DB 实现（pgvector / Milvus / Qdrant）。
 * 业务代码与 AI Memory 层禁止直接依赖具体 Vector DB SDK。
 * Provider 在 Sprint 5 决策。
 *
 * Sprint 0 仅定义接口，不部署 Vector DB 实例（Sprint 5）。
 */
public interface VectorStoreAdapter {

    /**
     * 存储向量。
     *
     * @param id        向量 ID（与 ai_memory.embedding_id 关联）
     * @param embedding 向量数据
     * @param metadata  元数据
     */
    void store(String id, float[] embedding, Map<String, String> metadata);

    /**
     * 语义检索相似向量。
     *
     * @param embedding  查询向量
     * @param topK       返回条数
     * @param filter     过滤条件（如 userId）
     * @return 相似向量 ID 列表
     */
    List<String> search(float[] embedding, int topK, String filter);

    /**
     * 删除向量。
     *
     * @param id 向量 ID
     */
    void delete(String id);
}
