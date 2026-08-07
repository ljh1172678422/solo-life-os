package com.sololifeos.user.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.service.ExploreDomainService;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
import com.sololifeos.user.repository.FavoriteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * Favorite 表 Owner 为 User Module（§6.7），Application Service 定义在 user 包下。
 * 职责：收藏用例协调 / 事务边界。调用 {@link ExploreDomainService} 做业务规则校验
 * （收藏唯一性），调用 {@link FavoriteRepository} 做持久化。
 */
@Service
public class FavoriteApplicationService {

    private final ExploreDomainService exploreDomainService;
    private final FavoriteRepository favoriteRepository;

    public FavoriteApplicationService(ExploreDomainService exploreDomainService,
                                      FavoriteRepository favoriteRepository) {
        this.exploreDomainService = exploreDomainService;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 添加收藏。校验未已收藏后持久化。
     * <p>
     * 并发安全：两个事务同时通过 existsByUserIdAndTargetTypeAndTargetId 校验时，
     * DB 层 uk_favorite_user_target 拒绝第二个 commit。
     *
     * @param userId     用户 ID
     * @param targetType 收藏目标类型
     * @param targetId   收藏目标 ID
     * @return 已持久化的 Favorite
     */
    @Transactional
    public Favorite addFavorite(Long userId, FavoriteTarget targetType, Long targetId) {
        Favorite favorite = exploreDomainService.createFavorite(userId, targetType, targetId);
        try {
            return favoriteRepository.save(favorite);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("已收藏该目标（并发创建冲突）");
        }
    }

    /**
     * 取消收藏。按用户 + 目标类型 + 目标 ID 删除。
     */
    @Transactional
    public void removeFavorite(Long userId, FavoriteTarget targetType, Long targetId) {
        Favorite favorite = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElseThrow(() -> new BusinessException("收藏记录不存在"));
        favoriteRepository.delete(favorite);
    }

    /**
     * 查询用户全部收藏。
     */
    @Transactional(readOnly = true)
    public List<Favorite> listFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    /**
     * 按目标类型查询用户收藏（分类列表）。
     */
    @Transactional(readOnly = true)
    public List<Favorite> listFavoritesByType(Long userId, FavoriteTarget targetType) {
        return favoriteRepository.findByUserIdAndTargetType(userId, targetType);
    }

    /**
     * 检查是否已收藏。
     */
    @Transactional(readOnly = true)
    public boolean checkFavorited(Long userId, FavoriteTarget targetType, Long targetId) {
        return favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }
}
