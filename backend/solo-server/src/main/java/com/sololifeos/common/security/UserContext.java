package com.sololifeos.common.security;

/**
 * 当前请求的认证上下文 (ADR-0006 JWT Authentication)。
 * <p>
 * {@code JwtAuthFilter} 在校验 token 成功后，将 userId 写入 ThreadLocal，
 * Application Service / Controller 可通过 {@link #currentUserId()} 读取。
 * <p>
 * 注意：必须在请求结束时调用 {@link #clear()}（Filter 的 finally 块中执行），
 * 防止线程池复用导致上下文泄漏。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    /** 设置当前请求的 userId（由 JwtAuthFilter 调用）。 */
    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /** 获取当前请求的 userId，未认证返回 null。 */
    public static Long currentUserId() {
        return CURRENT_USER_ID.get();
    }

    /** 清除上下文（Filter finally 调用，防止线程复用泄漏）。 */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
