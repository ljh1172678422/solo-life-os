package com.sololifeos.explore.dto;

import java.time.LocalDateTime;

/**
 * 收藏响应 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 */
public record FavoriteResponse(
        Long id,
        Long userId,
        String targetType,
        Long targetId,
        LocalDateTime createdTime
) {
}
