package com.sololifeos.user.dto;

import com.sololifeos.user.domain.model.BudgetLevel;
import jakarta.validation.constraints.Size;

/**
 * 用户偏好更新请求 DTO。
 */
public record UserPreferenceUpdateRequest(
        @Size(max = 500, message = "兴趣标签最长 500 字符")
        String interest,

        BudgetLevel budget,

        @Size(max = 500, message = "生活方式描述最长 500 字符")
        String lifestyle
) {
}
