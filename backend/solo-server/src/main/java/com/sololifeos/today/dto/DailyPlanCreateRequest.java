package com.sololifeos.today.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 创建每日计划请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * userId 来自路径变量 {@code /api/users/{userId}/plans}，不在请求体中。
 * 一个用户一天一个计划（DB uk_daily_plan_user_date 兜底，Domain Service 第一道防线）。
 */
public record DailyPlanCreateRequest(
        @NotNull(message = "计划日期不可为空")
        LocalDate date
) {
}
