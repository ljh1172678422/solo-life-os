package com.sololifeos.user.dto;

/**
 * 登录响应 DTO (ADR-0006 JWT Authentication, CODE_RULES §5)。
 * <p>
 * 登录成功后返回 JWT token 与基础用户信息，前端持久化 token 后在后续请求的
 * {@code Authorization: Bearer <token>} 头中携带。
 *
 * @param token   JWT token
 * @param userId  用户 ID
 * @param nickname 昵称
 */
public record LoginResponse(
        String token,
        Long userId,
        String nickname
) {
}
