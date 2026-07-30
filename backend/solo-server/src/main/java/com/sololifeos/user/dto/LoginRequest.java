package com.sololifeos.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求 DTO (ADR-0006 JWT Authentication, CODE_RULES §5)。
 * <p>
 * 账号支持邮箱或手机号（任选其一），密码为明文（HTTPS 传输，不入库 / 不记日志）。
 */
public record LoginRequest(

        @NotBlank(message = "账号不可为空")
        @Size(max = 100, message = "账号最长 100 字符")
        String account,

        @NotBlank(message = "密码不可为空")
        @Size(min = 6, max = 100, message = "密码长度 6-100 字符")
        String password
) {
}
