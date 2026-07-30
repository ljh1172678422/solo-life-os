package com.sololifeos.user.dto;

import java.time.LocalDateTime;

/**
 * 标签响应 DTO。
 */
public record TagResponse(
        Long id,
        Long userId,
        String name,
        String type,
        LocalDateTime createdTime
) {
}
