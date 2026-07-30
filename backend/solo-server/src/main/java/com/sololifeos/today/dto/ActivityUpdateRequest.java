package com.sololifeos.today.dto;

import com.sololifeos.today.domain.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 修改活动请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * endTime 可空（未结束的活动无 endTime）。其余字段必填（整体替换语义，与 Entity.update 一致）。
 */
public record ActivityUpdateRequest(
        @NotBlank(message = "活动标题不可为空")
        @Size(max = 200, message = "活动标题最长 200 字符")
        String title,

        @NotNull(message = "活动类型不可为空")
        ActivityType type,

        @NotNull(message = "活动开始时间不可为空")
        LocalDateTime startTime,

        LocalDateTime endTime
) {
}
