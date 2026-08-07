package com.sololifeos.explore.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.explore.application.ExploreAssembler;
import com.sololifeos.explore.application.LocationApplicationService;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import com.sololifeos.explore.dto.LocationCreateRequest;
import com.sololifeos.explore.dto.LocationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地点控制器 (CODE_RULES §3.1 Controller: 接收请求 / 参数校验 / 返回封装)。
 * <p>
 * 路由设计：
 * <ul>
 *   <li>{@code /api/locations} — 地点列表 / 创建</li>
 *   <li>{@code /api/locations/{id}} — 单地点操作（获取 / 更新）</li>
 *   <li>{@code /api/locations/nearby} — 附近搜索</li>
 * </ul>
 * <p>
 * 权限：所有端点需 JWT 认证（JwtAuthFilter）。
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationApplicationService locationApplicationService;

    public LocationController(LocationApplicationService locationApplicationService) {
        this.locationApplicationService = locationApplicationService;
    }

    /**
     * 创建地点。
     */
    @PostMapping
    public ApiResponse<LocationResponse> create(@Valid @RequestBody LocationCreateRequest request) {
        LocationType type = parseType(request.type());
        Location location = locationApplicationService.createLocation(
                request.name(), request.address(), request.city(),
                request.latitude(), request.longitude(), type);
        return ApiResponse.success(ExploreAssembler.toResponse(location));
    }

    /**
     * 查询地点列表。支持 {@code ?city= &type=} 筛选。
     */
    @GetMapping
    public ApiResponse<List<LocationResponse>> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type) {
        List<Location> locations;
        if (city != null && type != null) {
            locations = locationApplicationService.listByCityAndType(city, parseType(type));
        } else if (city != null) {
            locations = locationApplicationService.listByCity(city);
        } else {
            locations = locationApplicationService.listAll();
        }
        return ApiResponse.success(ExploreAssembler.toLocationResponseList(locations));
    }

    /**
     * 按地点 ID 查询详情。
     */
    @GetMapping("/{id}")
    public ApiResponse<LocationResponse> getById(@PathVariable Long id) {
        Location location = locationApplicationService.getById(id);
        return ApiResponse.success(ExploreAssembler.toResponse(location));
    }

    /**
     * 附近搜索。{@code GET /api/locations/nearby?lat=&lng=&radius=&type=}
     */
    @GetMapping("/nearby")
    public ApiResponse<List<LocationResponse>> nearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam Double radius,
            @RequestParam(required = false) String type) {
        List<Location> locations = locationApplicationService.searchNearby(
                lat, lng, radius, parseType(type));
        return ApiResponse.success(ExploreAssembler.toLocationResponseList(locations));
    }

    /**
     * 更新地点信息。
     */
    @PutMapping("/{id}")
    public ApiResponse<LocationResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody LocationCreateRequest request) {
        LocationType type = parseType(request.type());
        Location location = locationApplicationService.updateLocation(
                id, request.name(), request.address(), request.city(), type);
        return ApiResponse.success(ExploreAssembler.toResponse(location));
    }

    /** 安全解析 LocationType，非法值返回 null。 */
    private LocationType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return LocationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
