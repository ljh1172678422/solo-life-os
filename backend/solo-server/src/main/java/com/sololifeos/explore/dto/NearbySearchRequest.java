package com.sololifeos.explore.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 附近搜索请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * MVP 阶段用矩形范围近似搜索，Sprint 5+ 接入地图 SDK 做精确距离计算。
 */
public record NearbySearchRequest(
        @NotNull(message = "纬度不可为空")
        BigDecimal latitude,

        @NotNull(message = "经度不可为空")
        BigDecimal longitude,

        @NotNull(message = "搜索半径不可为空")
        Double radiusKm,

        String type
) {
}
