package com.sololifeos.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * 注意：不含 password —— 登录凭证归 Auth 任务 (ADR-0006)。
 */
public record UserRegisterRequest(
        @NotBlank(message = "昵称不可为空")
        @Size(max = 50, message = "昵称最长 50 字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最长 100 字符")
        String email,

        @Size(max = 20, message = "手机号最长 20 字符")
        String phone
) {
}
