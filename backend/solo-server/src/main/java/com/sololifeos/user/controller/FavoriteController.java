package com.sololifeos.user.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.explore.application.ExploreAssembler;
import com.sololifeos.explore.dto.FavoriteCreateRequest;
import com.sololifeos.explore.dto.FavoriteResponse;
import com.sololifeos.user.application.FavoriteApplicationService;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收藏控制器 (CODE_RULES §3.1 Controller: 接收请求 / 参数校验 / 返回封装)。
 * <p>
 * Favorite 表 Owner 为 User Module（§6.7），Controller 定义在 user 包下。
 * 路由设计：用户维度资源 {@code /api/users/{userId}/favorites}。
 * <p>
 * 权限：所有端点需 JWT 认证（JwtAuthFilter）。
 */
@RestController
@RequestMapping("/api/users/{userId}/favorites")
public class FavoriteController {

    private final FavoriteApplicationService favoriteApplicationService;

    public FavoriteController(FavoriteApplicationService favoriteApplicationService) {
        this.favoriteApplicationService = favoriteApplicationService;
    }

    /**
     * 添加收藏。
     */
    @PostMapping
    public ApiResponse<FavoriteResponse> add(@PathVariable Long userId,
                                             @RequestBody FavoriteCreateRequest request) {
        FavoriteTarget targetType = FavoriteTarget.valueOf(request.targetType().toUpperCase());
        Favorite favorite = favoriteApplicationService.addFavorite(userId, targetType, request.targetId());
        return ApiResponse.success(ExploreAssembler.toResponse(favorite));
    }

    /**
     * 取消收藏。{@code DELETE /api/users/{userId}/favorites/{targetType}/{targetId}}
     */
    @DeleteMapping("/{targetType}/{targetId}")
    public ApiResponse<Void> remove(@PathVariable Long userId,
                                    @PathVariable String targetType,
                                    @PathVariable Long targetId) {
        FavoriteTarget target = FavoriteTarget.valueOf(targetType.toUpperCase());
        favoriteApplicationService.removeFavorite(userId, target, targetId);
        return ApiResponse.success();
    }

    /**
     * 查询用户收藏列表。支持 {@code ?targetType=} 筛选。
     */
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list(@PathVariable Long userId,
                                                    @RequestParam(required = false) String targetType) {
        List<Favorite> favorites;
        if (targetType != null) {
            FavoriteTarget target = FavoriteTarget.valueOf(targetType.toUpperCase());
            favorites = favoriteApplicationService.listFavoritesByType(userId, target);
        } else {
            favorites = favoriteApplicationService.listFavorites(userId);
        }
        return ApiResponse.success(ExploreAssembler.toFavoriteResponseList(favorites));
    }

    /**
     * 检查是否已收藏。{@code GET /api/users/{userId}/favorites/check?targetType=&targetId=}
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> check(@PathVariable Long userId,
                                      @RequestParam String targetType,
                                      @RequestParam Long targetId) {
        FavoriteTarget target = FavoriteTarget.valueOf(targetType.toUpperCase());
        boolean favorited = favoriteApplicationService.checkFavorited(userId, target, targetId);
        return ApiResponse.success(favorited);
    }
}
