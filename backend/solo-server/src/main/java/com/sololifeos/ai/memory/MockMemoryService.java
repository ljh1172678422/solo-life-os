package com.sololifeos.ai.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link MemoryService} 的 Mock 实现（TASK-0207）。
 * <p>
 * Sprint 2 Planner Agent 骨架依赖 Memory，但 Memory 正式实现归 Sprint 5
 * （ARCHITECTURE §7 Risk: Planner Agent 依赖 Memory，Sprint 5 才实现 → 本 Sprint 用 Mock Memory）。
 * 本类以进程内 {@link ConcurrentHashMap} 模拟长期记忆存储，不持久化、不支持向量检索，
 * 仅供 Planner Agent 骨架联调，Sprint 5 替换为基于 ai_memory 表 + Vector DB 的真实实现。
 * <p>
 * 线程安全：单个 Mock 实例可被多线程共享（Map 与计数器均并发安全）。
 */
public class MockMemoryService implements MemoryService {

    /** 内存记忆条目。 */
    private record MemoryEntry(Long id, Long userId, String memoryType, String summary, String content) {
    }

    private final Map<Long, MemoryEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Long store(Long userId, String memoryType, String summary, String content) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不可为空");
        }
        Long id = idGenerator.incrementAndGet();
        store.put(id, new MemoryEntry(id, userId, memoryType, summary, content));
        return id;
    }

    @Override
    public List<String> retrieve(Long userId, String query, int limit) {
        if (userId == null) {
            return List.of();
        }
        int safeLimit = limit > 0 ? limit : 0;
        // Mock 检索：按 summary / content 包含 query 关键词简单匹配，无向量语义检索。
        // query 为空时返回该用户全部记忆（按 id 倒序，近因优先）。
        return store.values().stream()
                .filter(e -> e.userId().equals(userId))
                .filter(e -> query == null || query.isBlank()
                        || (e.summary() != null && e.summary().contains(query))
                        || (e.content() != null && e.content().contains(query)))
                .sorted(Comparator.comparingLong(MemoryEntry::id).reversed())
                .limit(safeLimit)
                .map(MemoryEntry::summary)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public void deleteByUser(Long userId) {
        if (userId == null) {
            return;
        }
        store.values().removeIf(e -> e.userId().equals(userId));
    }

    /** 测试 / 调试用：当前记忆条目总数。 */
    public int size() {
        return store.size();
    }

    /** 测试 / 调试用：清空全部记忆。 */
    public void clear() {
        store.clear();
        idGenerator.set(0);
    }
}
