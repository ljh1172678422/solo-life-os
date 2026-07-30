package com.sololifeos.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求 DTO (CODE_RULES §5: DTO 不进入 Domain)。
 * <p>
 * 包含 {@code password} 字段（ADR-0006 JWT Authentication）：
 * 明文经 HTTPS 传输，Application Service 层 BCrypt 哈希后入库，明文不入库 / 不记日志。
 */
public record UserRegisterRequest(
        @NotBlank(message = "昵称不可为空")
        @Size(max = 50, message = "昵称最长 50 字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最长 100 字符")
        String email,

        @Size(max = 20, message = "手机号最长 20 字符")
        String phone,

        @NotBlank(message = "密码不可为空")
        @Size(min = 6, max = 100, message = "密码长度 6-100 字符")
        String password
) {
}
