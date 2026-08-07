package com.sololifeos.user.repository;

import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * Favorite 表 Owner 为 User Module（§6.7），Repository 定义在 user 包下。
 * Spring Data JPA 代理实现，不手写 RepositoryImpl（框架约定）。
 * 查询方法对齐 DATABASE_DESIGN §8：uk_favorite_user_target 唯一索引。
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /** 按用户查询全部收藏。 */
    List<Favorite> findByUserId(Long userId);

    /** 按用户 + 目标类型查询收藏（分类列表）。 */
    List<Favorite> findByUserIdAndTargetType(Long userId, FavoriteTarget targetType);

    /** 检查是否已收藏（唯一性校验，§8 uk_favorite_user_target）。 */
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, FavoriteTarget targetType, Long targetId);

    /** 按用户 + 目标类型 + 目标 ID 查询收藏（取消收藏用）。 */
    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, FavoriteTarget targetType, Long targetId);
}
