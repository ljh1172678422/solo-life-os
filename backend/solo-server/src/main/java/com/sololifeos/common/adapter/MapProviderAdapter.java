package com.sololifeos.common.adapter;

import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地图供应商适配器接口 (ADR-0007 Map Provider Adapter)。
 * <p>
 * Provider Adapter Pattern，MVP 阶段不绑定具体地图供应商（高德 / 腾讯 / 百度）。
 * Sprint 3 提供 {@code MockMapProviderAdapter}（静态数据），
 * Sprint 5+ 接入真实 SDK 实现（如 {@code GaodeMapProviderAdapter}）。
 * <p>
 * 所有外部地图 API 调用必须通过此接口，禁止业务层直接调用地图 SDK（ARCHITECTURE §18）。
 */
public interface MapProviderAdapter {

    /**
     * 地址 → 坐标（地理编码）。
     *
     * @param address 地址字符串
     * @return 坐标 [latitude, longitude]，查不到返回 null
     */
    BigDecimal[] geocode(String address);

    /**
     * 坐标 → 地址（逆地理编码）。
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址字符串，查不到返回 null
     */
    String reverseGeocode(BigDecimal latitude, BigDecimal longitude);

    /**
     * 附近搜索。按中心点 + 半径搜索地点。
     *
     * @param latitude  中心纬度
     * @param longitude 中心经度
     * @param radiusKm  搜索半径（公里）
     * @param type      地点类型筛选（可空）
     * @return 附近地点列表
     */
    List<Location> searchNearby(BigDecimal latitude, BigDecimal longitude, double radiusKm, LocationType type);
}
