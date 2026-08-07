package com.sololifeos.explore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 创建地点请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 */
public record LocationCreateRequest(
        @NotBlank(message = "地点名称不可为空")
        @Size(max = 200, message = "地点名称最长 200 字符")
        String name,

        @Size(max = 500, message = "详细地址最长 500 字符")
        String address,

        @NotBlank(message = "所属城市不可为空")
        @Size(max = 100, message = "城市名最长 100 字符")
        String city,

        @NotNull(message = "纬度不可为空")
        BigDecimal latitude,

        @NotNull(message = "经度不可为空")
        BigDecimal longitude,

        String type
) {
}
