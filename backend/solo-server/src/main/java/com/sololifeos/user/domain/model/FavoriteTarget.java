package com.sololifeos.user.domain.model;

/**
 * 收藏目标类型枚举 (DATABASE_DESIGN §7 FAVORITE_TARGET)。
 * <p>
 * 对齐 §7 定义，共 3 个枚举值。
 * Favorite 表 Owner 为 User Module (§6.7)，故枚举定义在 user 包下。
 */
public enum FavoriteTarget {

    LOCATION,
    ACTIVITY,
    ROUTE
}
