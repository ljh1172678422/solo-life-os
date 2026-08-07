package com.sololifeos.explore.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import com.sololifeos.explore.repository.LocationRepository;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
import com.sololifeos.user.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Explore 领域服务 (CODE_RULES §3.1 Domain Service)。
 * <p>
 * 职责：封装 Explore Module 业务规则（地点创建校验、收藏唯一性校验）。
 * 不负责持久化（save 归 Application Service，TASK-0303）与事务边界。
 * 通过 Repository 做查询校验，返回领域对象交由上层持久化。
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>收藏唯一性：同一用户不能重复收藏同一目标（§8 uk_favorite_user_target 兜底）</li>
 *   <li>地点参数合法性校验下沉到 {@link Location#create}，本层不重复</li>
 * </ul>
 */
@Service
public class ExploreDomainService {

    private final LocationRepository locationRepository;
    private final FavoriteRepository favoriteRepository;

    public ExploreDomainService(LocationRepository locationRepository,
                                FavoriteRepository favoriteRepository) {
        this.locationRepository = locationRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 创建新地点。参数合法性校验由 {@link Location#create} 保证。
     *
     * @param name      地点名称
     * @param address   详细地址（可空）
     * @param city      所属城市
     * @param latitude  纬度
     * @param longitude 经度
     * @param type      地点类型（可空，null → OTHER）
     * @return 未持久化的 Location
     */
    public Location createLocation(String name, String address, String city,
                                   BigDecimal latitude, BigDecimal longitude, LocationType type) {
        return Location.create(name, address, city, latitude, longitude, type);
    }

    /**
     * 更新地点信息。
     *
     * @param location 已持久化的地点
     * @param name     地点名称
     * @param address  详细地址
     * @param city     所属城市
     * @param type     地点类型
     */
    public void updateLocation(Location location, String name, String address,
                               String city, LocationType type) {
        if (location == null) {
            throw new BusinessException("地点不可为空");
        }
        location.update(name, address, city, type);
    }

    /**
     * 创建新收藏。校验未已收藏后创建 Favorite 对象。
     * <p>
     * 收藏唯一性：Application Service 校验为第一道防线，
     * DB 层 uk_favorite_user_target 兜底。
     *
     * @param userId     用户 ID
     * @param targetType 收藏目标类型
     * @param targetId   收藏目标 ID
     * @return 未持久化的 Favorite
     */
    public Favorite createFavorite(Long userId, FavoriteTarget targetType, Long targetId) {
        if (userId == null) {
            throw new BusinessException("用户 ID 不可为空");
        }
        if (targetType == null) {
            throw new BusinessException("收藏目标类型不可为空");
        }
        if (targetId == null) {
            throw new BusinessException("收藏目标 ID 不可为空");
        }
        if (favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)) {
            throw new BusinessException("已收藏该目标: targetType=" + targetType + ", targetId=" + targetId);
        }
        return Favorite.create(userId, targetType, targetId);
    }
}
