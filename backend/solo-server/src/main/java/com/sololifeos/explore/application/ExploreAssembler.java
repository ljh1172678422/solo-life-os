package com.sololifeos.explore.application;

import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.dto.LocationResponse;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.explore.dto.FavoriteResponse;

import java.util.List;

/**
 * Explore Module DTO 转换器 (CODE_RULES §5: Controller 与 Application Service 之间必须经 DTO 转换)。
 * <p>
 * 将 Domain Entity 转换为 Response DTO，禁止反向转换（入参由 record 直接承载）。
 * 与 Today Module TodayAssembler 模式一致（Sprint 2 TASK-0204）。
 */
public final class ExploreAssembler {

    private ExploreAssembler() {
    }

    public static LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCity(),
                location.getLatitude(),
                location.getLongitude(),
                location.getType() != null ? location.getType().name() : null,
                location.getCreatedTime(),
                location.getUpdatedTime()
        );
    }

    public static FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getUserId(),
                favorite.getTargetType() != null ? favorite.getTargetType().name() : null,
                favorite.getTargetId(),
                favorite.getCreatedTime()
        );
    }

    public static List<LocationResponse> toLocationResponseList(List<Location> locations) {
        return locations.stream().map(ExploreAssembler::toResponse).toList();
    }

    public static List<FavoriteResponse> toFavoriteResponseList(List<Favorite> favorites) {
        return favorites.stream().map(ExploreAssembler::toResponse).toList();
    }
}
