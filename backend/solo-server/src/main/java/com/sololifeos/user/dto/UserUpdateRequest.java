package com.sololifeos.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户资料更新请求 DTO。
 */
public record UserUpdateRequest(
        @NotBlank(message = "昵称不可为空")
        @Size(max = 50, message = "昵称最长 50 字符")
        String nickname,

        @Size(max = 500, message = "头像地址最长 500 字符")
        String avatar,

        @Size(max = 100, message = "城市最长 100 字符")
        String city
) {
}
