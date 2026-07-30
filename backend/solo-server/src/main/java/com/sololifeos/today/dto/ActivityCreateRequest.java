package com.sololifeos.today.dto;

import com.sololifeos.today.domain.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 创建活动请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * planId 来自路径变量 {@code /api/plans/{planId}/activities}，不在请求体中。
 * type 可空（null → OTHER，由 Entity.create 兜底）。
 */
public record ActivityCreateRequest(
        @NotBlank(message = "活动标题不可为空")
        @Size(max = 200, message = "活动标题最长 200 字符")
        String title,

        ActivityType type,

        @NotNull(message = "活动开始时间不可为空")
        LocalDateTime startTime
) {
}
