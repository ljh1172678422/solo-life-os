package com.sololifeos.explore.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import com.sololifeos.explore.domain.service.ExploreDomainService;
import com.sololifeos.explore.repository.LocationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地点应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：地点用例协调 / 事务边界。调用 {@link ExploreDomainService} 做业务规则校验，
 * 调用 {@link LocationRepository} 做持久化。入参用原始类型，出参用 Domain Entity
 * （DTO 转换归 Controller 层，TASK-0304）。
 */
@Service
public class LocationApplicationService {

    private final ExploreDomainService exploreDomainService;
    private final LocationRepository locationRepository;

    public LocationApplicationService(ExploreDomainService exploreDomainService,
                                      LocationRepository locationRepository) {
        this.exploreDomainService = exploreDomainService;
        this.locationRepository = locationRepository;
    }

    /**
     * 创建地点。
     *
     * @param name      地点名称
     * @param address   详细地址（可空）
     * @param city      所属城市
     * @param latitude  纬度
     * @param longitude 经度
     * @param type      地点类型（可空，null → OTHER）
     * @return 已持久化的 Location
     */
    @Transactional
    public Location createLocation(String name, String address, String city,
                                   BigDecimal latitude, BigDecimal longitude, LocationType type) {
        Location location = exploreDomainService.createLocation(name, address, city, latitude, longitude, type);
        try {
            return locationRepository.save(location);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("地点创建失败（数据约束冲突）: " + e.getMessage());
        }
    }

    /**
     * 按 ID 查询地点。
     */
    @Transactional(readOnly = true)
    public Location getById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("地点不存在: id=" + id));
    }

    /**
     * 查询全部地点列表。
     */
    @Transactional(readOnly = true)
    public List<Location> listAll() {
        return locationRepository.findAll();
    }

    /**
     * 按城市查询地点列表。
     */
    @Transactional(readOnly = true)
    public List<Location> listByCity(String city) {
        return locationRepository.findByCity(city);
    }

    /**
     * 按城市 + 类型查询地点列表。
     */
    @Transactional(readOnly = true)
    public List<Location> listByCityAndType(String city, LocationType type) {
        return locationRepository.findByCityAndType(city, type);
    }

    /**
     * 附近搜索。按经纬度范围查询（MVP 阶段用矩形范围近似，Sprint 5+ 接入地图 SDK 做精确距离计算）。
     *
     * @param latitude  中心纬度
     * @param longitude 中心经度
     * @param radiusKm  搜索半径（公里）
     * @param type      地点类型筛选（可空）
     * @return 附近地点列表
     */
    @Transactional(readOnly = true)
    public List<Location> searchNearby(BigDecimal latitude, BigDecimal longitude, double radiusKm, LocationType type) {
        // 1 度 ≈ 111km，计算经纬度范围
        BigDecimal latDelta = BigDecimal.valueOf(radiusKm / 111.0);
        BigDecimal minLat = latitude.subtract(latDelta);
        BigDecimal maxLat = latitude.add(latDelta);
        // 经度范围随纬度变化修正（cos(latitude)）
        double cosLat = Math.cos(Math.toRadians(latitude.doubleValue()));
        BigDecimal lngDelta = BigDecimal.valueOf(radiusKm / (111.0 * Math.max(cosLat, 0.01)));
        BigDecimal minLng = longitude.subtract(lngDelta);
        BigDecimal maxLng = longitude.add(lngDelta);

        if (type != null) {
            return locationRepository.findByLatitudeBetweenAndLongitudeBetweenAndType(
                    minLat, maxLat, minLng, maxLng, type);
        }
        return locationRepository.findByLatitudeBetweenAndLongitudeBetween(
                minLat, maxLat, minLng, maxLng);
    }

    /**
     * 更新地点信息。
     */
    @Transactional
    public Location updateLocation(Long id, String name, String address, String city, LocationType type) {
        Location location = getById(id);
        exploreDomainService.updateLocation(location, name, address, city, type);
        return locationRepository.save(location);
    }
}
