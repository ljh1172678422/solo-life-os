package com.sololifeos.common.adapter;

import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mock 地图供应商适配器 (ADR-0007)。
 * <p>
 * Sprint 3 MVP 阶段使用静态数据，Sprint 5+ 替换为真实地图 SDK 实现。
 * 通过 {@code @ConditionalOnProperty} 或 Bean 覆盖切换。
 */
@Component
public class MockMapProviderAdapter implements MapProviderAdapter {

    @Override
    public BigDecimal[] geocode(String address) {
        // Mock：返回北京天安门坐标
        return new BigDecimal[]{new BigDecimal("39.9087"), new BigDecimal("116.3975")};
    }

    @Override
    public String reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        // Mock：返回静态地址
        return "北京市东城区";
    }

    @Override
    public List<Location> searchNearby(BigDecimal latitude, BigDecimal longitude, double radiusKm, LocationType type) {
        // Mock：返回空列表，实际由 LocationApplicationService 从 DB 查询
        return List.of();
    }
}
