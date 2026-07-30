package com.sololifeos.common.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 配置属性 (ADR-0006 JWT Authentication)。
 * <p>
 * 绑定 {@code application.yml} 根级 {@code jwt.*} 配置：
 * <pre>
 * jwt:
 *   secret: ${JWT_SECRET:...}          # HS256 签名密钥（Base64 编码）
 *   expiration-ms: ${JWT_EXPIRATION_MS:86400000}  # token 有效期，默认 24h
 * </pre>
 * 生产环境必须通过 {@code JWT_SECRET} 环境变量注入强随机密钥，默认值仅供开发使用。
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥（Base64 编码）。生产环境必须由 JWT_SECRET 环境变量注入。 */
    @NotBlank
    private String secret;

    /** Token 有效期（毫秒），默认 24h。 */
    @Positive
    private long expirationMs = 86_400_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
