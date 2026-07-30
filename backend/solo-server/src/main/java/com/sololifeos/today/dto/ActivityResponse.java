package com.sololifeos.today.dto;

import java.time.LocalDateTime;

/**
 * 活动响应 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * type 以字符串返回（前端不依赖 Java 枚举），对齐 DATABASE_DESIGN §7 ACTIVITY_TYPE。
 * locationId 可空（Explore Module Sprint 3 前不绑定地点）。
 */
public record ActivityResponse(
        Long id,
        Long dailyPlanId,
        String title,
        String type,
        Long locationId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
