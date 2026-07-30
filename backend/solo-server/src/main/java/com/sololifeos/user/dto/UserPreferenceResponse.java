package com.sololifeos.user.dto;

import java.time.LocalDateTime;

/**
 * 用户偏好响应 DTO。
 */
public record UserPreferenceResponse(
        Long id,
        Long userId,
        String interest,
        String budget,
        String lifestyle,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
