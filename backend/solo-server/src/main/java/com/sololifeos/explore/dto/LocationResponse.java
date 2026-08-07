package com.sololifeos.explore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地点响应 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 */
public record LocationResponse(
        Long id,
        String name,
        String address,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        String type,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
