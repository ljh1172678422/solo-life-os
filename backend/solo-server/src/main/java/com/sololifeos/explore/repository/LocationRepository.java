package com.sololifeos.explore.repository;

import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地点仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * Spring Data JPA 代理实现，不手写 RepositoryImpl（框架约定）。
 * 查询方法对齐 DATABASE_DESIGN §8 索引：idx_city / idx_lat_lng。
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /** 按城市查询地点（§8 idx_city 索引）。 */
    List<Location> findByCity(String city);

    /** 按城市 + 类型查询地点。 */
    List<Location> findByCityAndType(String city, LocationType type);

    /** 按经纬度范围查询附近地点（§8 idx_lat_lng 索引）。 */
    List<Location> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng);

    /** 按经纬度范围 + 类型查询附近地点。 */
    List<Location> findByLatitudeBetweenAndLongitudeBetweenAndType(
            BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng, LocationType type);
}
