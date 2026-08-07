package com.sololifeos.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Favorite} Entity 单元测试。
 * <p>
 * Favorite 归属 User Module（§6.7），故测试文件定义在 user 包下。
 * 覆盖 create 工厂 3 参数空值校验。唯一性约束由数据库 uk 与 DomainService 保证，
 * 实体层不重复校验。
 */
class FavoriteEntityTest {

    private static final Long USER_ID = 1L;
    private static final Long TARGET_ID = 555L;
    private static final FavoriteTarget TARGET_TYPE = FavoriteTarget.LOCATION;

    @Nested
    @DisplayName("create 工厂构造")
    class CreateTest {

        @Test
        @DisplayName("正常创建：3 字段保留")
        void shouldCreateWithAllFields() {
            Favorite favorite = Favorite.create(USER_ID, TARGET_TYPE, TARGET_ID);

            assertThat(favorite.getUserId()).isEqualTo(USER_ID);
            assertThat(favorite.getTargetType()).isEqualTo(TARGET_TYPE);
            assertThat(favorite.getTargetId()).isEqualTo(TARGET_ID);
            assertThat(favorite.getId()).isNull();
        }

        @Test
        @DisplayName("userId 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> Favorite.create(null, TARGET_TYPE, TARGET_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("用户 ID 不可为空");
        }

        @Test
        @DisplayName("targetType 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenTargetTypeNull() {
            assertThatThrownBy(() -> Favorite.create(USER_ID, null, TARGET_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("收藏目标类型不可为空");
        }

        @Test
        @DisplayName("targetId 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenTargetIdNull() {
            assertThatThrownBy(() -> Favorite.create(USER_ID, TARGET_TYPE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("收藏目标 ID 不可为空");
        }

        @Test
        @DisplayName("targetType 所有枚举值均合法（FavoriteTarget 4 值）")
        void shouldAcceptAllFavoriteTargetEnumValues() {
            for (FavoriteTarget target : FavoriteTarget.values()) {
                Favorite fav = Favorite.create(USER_ID, target, TARGET_ID);
                assertThat(fav.getTargetType()).isEqualTo(target);
            }
        }
    }
}
