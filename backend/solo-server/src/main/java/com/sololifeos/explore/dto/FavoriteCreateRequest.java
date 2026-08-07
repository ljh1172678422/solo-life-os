package com.sololifeos.explore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 收藏请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * userId 来自路径变量 {@code /api/users/{userId}/favorites}，不在请求体中。
 */
public record FavoriteCreateRequest(
        @NotBlank(message = "收藏目标类型不可为空")
        String targetType,

        @NotNull(message = "收藏目标 ID 不可为空")
        Long targetId
) {
}
