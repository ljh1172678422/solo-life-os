package com.sololifeos.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 签发与验证服务 (ADR-0006 JWT Authentication)。
 * <p>
 * 算法：HS256（HMAC-SHA256），密钥从 {@link JwtProperties} 注入。
 * <p>
 * 职责：
 * <ul>
 *   <li>{@link #generateToken} 签发 token（subject = userId，claim: nickname）</li>
 *   <li>{@link #parseAndVerify} 解析并验证 token 签名与有效期</li>
 *   <li>{@link #extractUserId} 便捷提取 userId（解析失败返回空）</li>
 * </ul>
 * 不负责：请求拦截（归 {@code JwtAuthFilter}）/ 密码校验（归 {@code AuthService}）。
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** JWT subject claim 中存放 userId。 */
    public static final String CLAIM_USER_ID = "uid";

    /** JWT 自定义 claim 中存放 nickname（调试 / 日志用）。 */
    public static final String CLAIM_NICKNAME = "nickname";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        // secret 在 application.yml 中为 Base64 字符串；jjwt 0.12 要求密钥 >= 256 bit。
        // 这里直接用 secret 的 UTF-8 字节作为 HMAC 密钥（开发默认值已是 64 字节 Base64 文本）。
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = properties.getExpirationMs();
    }

    /**
     * 签发 JWT token。
     *
     * @param userId   用户 ID（写入 subject + uid claim）
     * @param nickname 昵称（写入 nickname claim，便于日志）
     * @return 紧凑序列化的 JWT 字符串
     */
    public String generateToken(Long userId, String nickname) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_NICKNAME, nickname)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并验证 token。签名错误或过期返回空，不抛异常（Filter 层据此放行或拒绝）。
     */
    public Optional<Claims> parseAndVerify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.warn("JWT token 格式非法: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 便捷提取 userId。验证失败返回空。
     */
    public Optional<Long> extractUserId(String token) {
        return parseAndVerify(token)
                .map(c -> c.get(CLAIM_USER_ID, Long.class));
    }
}
