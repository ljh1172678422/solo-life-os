package com.sololifeos.today.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日计划响应 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * status 以字符串返回（前端不依赖 Java 枚举），对齐 DATABASE_DESIGN §7 PLAN_STATUS。
 */
public record DailyPlanResponse(
        Long id,
        Long userId,
        LocalDate date,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
