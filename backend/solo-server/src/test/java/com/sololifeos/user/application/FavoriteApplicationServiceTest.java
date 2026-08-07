package com.sololifeos.user.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.service.ExploreDomainService;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FavoriteApplicationService} 单元测试。
 * <p>
 * Favorite Service 归属 User Module（§6.7），测试文件位于 user 包下。
 * 覆盖：addFavorite（正常 / 并发冲突 / Domain 透传）、
 * removeFavorite（正常 / 不存在）、listFavorites（全部 / 按类型）、checkFavorited。
 */
@ExtendWith(MockitoExtension.class)
class FavoriteApplicationServiceTest {

    @Mock
    private ExploreDomainService exploreDomainService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteApplicationService favoriteAppService;

    private static final Long USER_ID = 1L;
    private static final Long TARGET_ID = 555L;
    private static final FavoriteTarget TARGET_TYPE = FavoriteTarget.LOCATION;

    private Favorite buildPersistedFavorite(Long id) {
        Favorite favorite = Favorite.create(USER_ID, TARGET_TYPE, TARGET_ID);
        try {
            java.lang.reflect.Field idF = Favorite.class.getDeclaredField("id");
            idF.setAccessible(true);
            idF.set(favorite, id);
        } catch (ReflectiveOperationException ignored) {
        }
        return favorite;
    }

    @Nested
    @DisplayName("addFavorite 添加收藏")
    class AddFavoriteTest {

        @Test
        @DisplayName("正常：Domain → save")
        void shouldAddAndPersist() {
            Favorite fav = Favorite.create(USER_ID, TARGET_TYPE, TARGET_ID);
            when(exploreDomainService.createFavorite(USER_ID, TARGET_TYPE, TARGET_ID)).thenReturn(fav);
            Favorite persisted = buildPersistedFavorite(99L);
            when(favoriteRepository.save(fav)).thenReturn(persisted);

            Favorite result = favoriteAppService.addFavorite(USER_ID, TARGET_TYPE, TARGET_ID);

            assertThat(result.getId()).isEqualTo(99L);
            verify(favoriteRepository).save(fav);
        }

        @Test
        @DisplayName("并发冲突：DB uk 拒绝 → BusinessException")
        void shouldConvertDataIntegrityViolationToBusinessException() {
            Favorite fav = Favorite.create(USER_ID, TARGET_TYPE, TARGET_ID);
            when(exploreDomainService.createFavorite(USER_ID, TARGET_TYPE, TARGET_ID)).thenReturn(fav);
            when(favoriteRepository.save(fav))
                    .thenThrow(new DataIntegrityViolationException("uk_favorite_user_target"));

            assertThatThrownBy(() -> favoriteAppService.addFavorite(USER_ID, TARGET_TYPE, TARGET_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("并发创建冲突");
        }

        @Test
        @DisplayName("Domain 抛 BusinessException（已收藏）：透传，不 save")
        void shouldPropagateFromDomain() {
            when(exploreDomainService.createFavorite(USER_ID, TARGET_TYPE, TARGET_ID))
                    .thenThrow(new BusinessException("已收藏该目标"));

            assertThatThrownBy(() -> favoriteAppService.addFavorite(USER_ID, TARGET_TYPE, TARGET_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已收藏该目标");
            verify(favoriteRepository, never()).save(null);
        }
    }

    @Nested
    @DisplayName("removeFavorite 取消收藏")
    class RemoveFavoriteTest {

        @Test
        @DisplayName("正常：find 存在 → delete")
        void shouldRemoveWhenExists() {
            Favorite fav = buildPersistedFavorite(99L);
            when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(
                    USER_ID, TARGET_TYPE, TARGET_ID))
                    .thenReturn(Optional.of(fav));

            favoriteAppService.removeFavorite(USER_ID, TARGET_TYPE, TARGET_ID);

            verify(favoriteRepository).delete(fav);
        }

        @Test
        @DisplayName("未收藏 → BusinessException")
        void shouldThrowWhenNotFavorited() {
            when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(
                    USER_ID, TARGET_TYPE, TARGET_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteAppService.removeFavorite(USER_ID, TARGET_TYPE, TARGET_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收藏记录不存在");
            verify(favoriteRepository, never()).delete(null);
        }
    }

    @Nested
    @DisplayName("查询 / 检查")
    class QueryTest {

        @Test
        @DisplayName("listFavorites：findByUserId")
        void shouldListAllFavorites() {
            List<Favorite> list = List.of(
                    buildPersistedFavorite(1L), buildPersistedFavorite(2L));
            when(favoriteRepository.findByUserId(USER_ID)).thenReturn(list);

            List<Favorite> result = favoriteAppService.listFavorites(USER_ID);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("listFavoritesByType：findByUserIdAndTargetType")
        void shouldListFavoritesByType() {
            List<Favorite> list = List.of(buildPersistedFavorite(1L));
            when(favoriteRepository.findByUserIdAndTargetType(USER_ID, TARGET_TYPE))
                    .thenReturn(list);

            List<Favorite> result = favoriteAppService.listFavoritesByType(USER_ID, TARGET_TYPE);

            assertThat(result).hasSize(1);
            verify(favoriteRepository).findByUserIdAndTargetType(USER_ID, TARGET_TYPE);
        }

        @Test
        @DisplayName("checkFavorited：exists 返回 true/false")
        void shouldCheckFavorited() {
            when(favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(
                    USER_ID, TARGET_TYPE, TARGET_ID)).thenReturn(true);
            assertThat(favoriteAppService.checkFavorited(USER_ID, TARGET_TYPE, TARGET_ID)).isTrue();

            when(favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(
                    USER_ID, TARGET_TYPE, 999L)).thenReturn(false);
            assertThat(favoriteAppService.checkFavorited(USER_ID, TARGET_TYPE, 999L)).isFalse();
        }
    }
}
