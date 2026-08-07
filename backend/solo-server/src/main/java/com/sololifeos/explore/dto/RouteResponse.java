package com.sololifeos.explore.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线响应 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * MVP 阶段为静态路线，Sprint 5+ 接入地图 SDK 动态生成。
 */
public record RouteResponse(
        Long id,
        String name,
        String description,
        BigDecimal totalDistanceKm,
        Integer estimatedMinutes,
        String difficulty,
        List<RoutePoint> points
) {
    /**
     * 路线途经点。
     */
    public record RoutePoint(
            Integer order,
            Long locationId,
            String name,
            BigDecimal latitude,
            BigDecimal longitude,
            String tip
    ) {
    }
}
