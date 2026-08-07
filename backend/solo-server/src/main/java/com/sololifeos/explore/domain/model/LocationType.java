package com.sololifeos.explore.domain.model;

/**
 * 地点类型枚举 (DATABASE_DESIGN §7 LOCATION_TYPE)。
 * <p>
 * 对齐 §7 定义，共 9 个枚举值。默认值为 OTHER。
 */
public enum LocationType {

    RESTAURANT,
    CAFE,
    PARK,
    MUSEUM,
    SHOPPING,
    SPORT,
    ENTERTAINMENT,
    SCENIC,
    OTHER
}
