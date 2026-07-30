package com.sololifeos.user.dto;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO (CODE_RULES §5: 禁止 Entity 直出 Controller)。
 * <p>
 * 不含 deletedTime / updatedTime 等内部字段。
 */
public record UserResponse(
        Long id,
        String nickname,
        String avatar,
        String email,
        String phone,
        String city,
        String status,
        LocalDateTime createdTime
) {
}
