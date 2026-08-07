package com.sololifeos.explore.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import com.sololifeos.explore.repository.LocationRepository;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
import com.sololifeos.user.repository.FavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ExploreDomainService} 单元测试。
 * <p>
 * 覆盖 createLocation（参数校验 → Location.create 路径）、
 * updateLocation（location null 校验 + 委托 Entity.update）、
 * createFavorite（3 参数空值校验 + 唯一性 exists 校验 + Favorite.create）。
 */
@ExtendWith(MockitoExtension.class)
class ExploreDomainServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private ExploreDomainService exploreDomainService;

    private static final Long USER_ID = 1L;
    private static final BigDecimal LAT = new BigDecimal("30.2741000");
    private static final BigDecimal LNG = new BigDecimal("120.1551000");

    @Nested
    @DisplayName("createLocation 新建地点")
    class CreateLocationTest {

        @Test
        @DisplayName("正常创建：返回未持久化 Location")
        void shouldCreateLocation() {
            Location location = exploreDomainService.createLocation(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE);

            assertThat(location.getName()).isEqualTo("隐山咖啡");
            assertThat(location.getCity()).isEqualTo("杭州");
            assertThat(location.getType()).isEqualTo(LocationType.CAFE);
            // Domain Service 不负责持久化
            verify(locationRepository, never()).save(location);
        }

        @Test
        @DisplayName("参数非法：抛 IllegalArgumentException（由 Location.create 抛出）")
        void shouldPropagateFromLocationCreate() {
            assertThatThrownBy(() -> exploreDomainService.createLocation(
                    "  ", null, "杭州", LAT, LNG, LocationType.CAFE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("地点名称不可为空");
        }
    }

    @Nested
    @DisplayName("updateLocation 更新地点")
    class UpdateLocationTest {

        @Test
        @DisplayName("正常更新：委托 location.update 替换字段")
        void shouldUpdateLocationViaEntity() {
            Location location = Location.create(
                    "旧名", "旧地址", "旧城市", LAT, LNG, LocationType.CAFE);

            exploreDomainService.updateLocation(location,
                    "新名", "新地址", "新城市", LocationType.BOOKSTORE);

            assertThat(location.getName()).isEqualTo("新名");
            assertThat(location.getAddress()).isEqualTo("新地址");
            assertThat(location.getCity()).isEqualTo("新城市");
            assertThat(location.getType()).isEqualTo(LocationType.BOOKSTORE);
        }

        @Test
        @DisplayName("location 为 null：抛 BusinessException")
        void shouldThrowWhenLocationNull() {
            assertThatThrownBy(() -> exploreDomainService.updateLocation(
                    null, "x", "x", "x", LocationType.CAFE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("地点不可为空");
        }
    }

    @Nested
    @DisplayName("createFavorite 新建收藏")
    class CreateFavoriteTest {

        @Test
        @DisplayName("正常创建：未已收藏 → 返回 Favorite 对象，不持久化")
        void shouldCreateFavoriteWhenNotExists() {
            FavoriteTarget targetType = FavoriteTarget.LOCATION;
            Long targetId = 777L;
            when(favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(
                    USER_ID, targetType, targetId)).thenReturn(false);

            Favorite favorite = exploreDomainService.createFavorite(USER_ID, targetType, targetId);

            assertThat(favorite.getUserId()).isEqualTo(USER_ID);
            assertThat(favorite.getTargetType()).isEqualTo(targetType);
            assertThat(favorite.getTargetId()).isEqualTo(targetId);
            verify(favoriteRepository, never()).save(favorite);
        }

        @Test
        @DisplayName("已收藏：抛 BusinessException")
        void shouldThrowWhenAlreadyFavorited() {
            FavoriteTarget targetType = FavoriteTarget.LOCATION;
            Long targetId = 777L;
            when(favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(
                    USER_ID, targetType, targetId)).thenReturn(true);

            assertThatThrownBy(() -> exploreDomainService.createFavorite(USER_ID, targetType, targetId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已收藏该目标")
                    .hasMessageContaining("LOCATION")
                    .hasMessageContaining("777");
        }

        @Test
        @DisplayName("userId 为 null：抛 BusinessException，不查库")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> exploreDomainService.createFavorite(
                    null, FavoriteTarget.LOCATION, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户 ID 不可为空");
            verify(favoriteRepository, never()).existsByUserIdAndTargetTypeAndTargetId(
                    null, FavoriteTarget.LOCATION, 1L);
        }

        @Test
        @DisplayName("targetType 为 null：抛 BusinessException")
        void shouldThrowWhenTargetTypeNull() {
            assertThatThrownBy(() -> exploreDomainService.createFavorite(
                    USER_ID, null, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收藏目标类型不可为空");
        }

        @Test
        @DisplayName("targetId 为 null：抛 BusinessException")
        void shouldThrowWhenTargetIdNull() {
            assertThatThrownBy(() -> exploreDomainService.createFavorite(
                    USER_ID, FavoriteTarget.LOCATION, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收藏目标 ID 不可为空");
        }
    }
}
