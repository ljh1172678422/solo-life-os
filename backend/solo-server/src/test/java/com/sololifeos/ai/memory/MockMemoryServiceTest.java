package com.sololifeos.ai.memory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MockMemoryService} 单元测试（TASK-0207）。
 * <p>
 * 验证 Mock 记忆存储 / 检索 / 删除行为，供 Planner Agent 骨架联调。
 */
class MockMemoryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Nested
    @DisplayName("store 存储")
    class StoreTest {

        @Test
        @DisplayName("存储返回递增 id")
        void shouldReturnIncrementalId() {
            MockMemoryService service = new MockMemoryService();
            Long id1 = service.store(USER_ID, "PREFERENCE", "喜欢安静", "用户偏好安静环境");
            Long id2 = service.store(USER_ID, "BEHAVIOR", "常去咖啡馆", "用户常去咖啡馆工作");

            assertThat(id1).isNotNull();
            assertThat(id2).isNotNull();
            assertThat(id2).isGreaterThan(id1);
            assertThat(service.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("userId 为空：抛 IllegalArgumentException")
        void shouldThrowWhenUserIdNull() {
            MockMemoryService service = new MockMemoryService();
            assertThatThrownBy(() -> service.store(null, "PREFERENCE", "s", "c"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userId 不可为空");
        }
    }

    @Nested
    @DisplayName("retrieve 检索")
    class RetrieveTest {

        @Test
        @DisplayName("按 query 关键词匹配 summary")
        void shouldRetrieveByKeywordInSummary() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "喜欢安静环境", "详细内容");
            service.store(USER_ID, "BEHAVIOR", "常去咖啡馆", "详细内容");

            List<String> result = service.retrieve(USER_ID, "安静", 10);

            assertThat(result).containsExactly("喜欢安静环境");
        }

        @Test
        @DisplayName("query 匹配 content")
        void shouldRetrieveByKeywordInContent() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "BEHAVIOR", "summary", "用户常去咖啡馆工作");

            List<String> result = service.retrieve(USER_ID, "咖啡馆", 10);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("query 为空：返回该用户全部记忆")
        void shouldReturnAllWhenQueryBlank() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "偏好1", "内容");
            service.store(USER_ID, "PREFERENCE", "偏好2", "内容");
            service.store(OTHER_USER_ID, "PREFERENCE", "他人", "内容");

            List<String> result = service.retrieve(USER_ID, "", 10);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("按 id 倒序返回（近因优先）")
        void shouldReturnInReverseIdOrder() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "第一", "内容");
            service.store(USER_ID, "PREFERENCE", "第二", "内容");
            service.store(USER_ID, "PREFERENCE", "第三", "内容");

            List<String> result = service.retrieve(USER_ID, "", 10);

            assertThat(result).containsExactly("第三", "第二", "第一");
        }

        @Test
        @DisplayName("limit 限制返回条数")
        void shouldRespectLimit() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "a", "内容");
            service.store(USER_ID, "PREFERENCE", "b", "内容");
            service.store(USER_ID, "PREFERENCE", "c", "内容");

            List<String> result = service.retrieve(USER_ID, "", 2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("用户隔离：不返回他人记忆")
        void shouldIsolateByUser() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "我的", "内容");
            service.store(OTHER_USER_ID, "PREFERENCE", "他人", "内容");

            List<String> result = service.retrieve(USER_ID, "", 10);

            assertThat(result).containsExactly("我的");
        }

        @Test
        @DisplayName("无记忆：返回空列表")
        void shouldReturnEmptyWhenNoMemory() {
            MockMemoryService service = new MockMemoryService();

            assertThat(service.retrieve(USER_ID, "anything", 10)).isEmpty();
        }

        @Test
        @DisplayName("userId 为空：返回空列表")
        void shouldReturnEmptyWhenUserIdNull() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "s", "c");

            assertThat(service.retrieve(null, "s", 10)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUser 删除")
    class DeleteTest {

        @Test
        @DisplayName("删除指定用户全部记忆")
        void shouldDeleteAllUserMemories() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "a", "内容");
            service.store(USER_ID, "PREFERENCE", "b", "内容");
            service.store(OTHER_USER_ID, "PREFERENCE", "他人", "内容");

            service.deleteByUser(USER_ID);

            assertThat(service.retrieve(USER_ID, "", 10)).isEmpty();
            assertThat(service.retrieve(OTHER_USER_ID, "", 10)).hasSize(1);
        }

        @Test
        @DisplayName("userId 为空：不删除任何记忆")
        void shouldNotDeleteWhenUserIdNull() {
            MockMemoryService service = new MockMemoryService();
            service.store(USER_ID, "PREFERENCE", "a", "内容");

            service.deleteByUser(null);

            assertThat(service.size()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("clear：清空全部并重置 id 生成器")
    void shouldClearAllAndResetIdGenerator() {
        MockMemoryService service = new MockMemoryService();
        Long firstId = service.store(USER_ID, "PREFERENCE", "a", "内容");
        service.clear();
        Long secondId = service.store(USER_ID, "PREFERENCE", "b", "内容");

        assertThat(service.size()).isEqualTo(1);
        assertThat(secondId).isEqualTo(firstId); // id 重置后重新从 1 开始
    }
}
