package com.sololifeos.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link JwtService} 单元测试 (ADR-0006)。
 * <p>
 * 覆盖 token 签发、验证、过期、篡改、解析。
 */
class JwtServiceTest {

    private JwtService jwtService;

    /**
     * 构造一个足够长（>= 256 bit）的密钥用于测试。
     * 真实密钥从环境变量注入，测试用固定值。
     */
    @BeforeEach
    void setUp() {
        // 32 字节（256 bit）随机内容，Base64 编码后作为 secret
        byte[] raw = new byte[32];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) i;
        }
        String secret = Base64.getEncoder().encodeToString(raw);
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setExpirationMs(3600_000L); // 1h
        jwtService = new JwtService(props);
    }

    @Nested
    @DisplayName("generateToken + parseAndVerify 签发与验证")
    class SignVerifyTest {

        @Test
        @DisplayName("签发的 token 可被正确解析：subject / uid / nickname claim 正确")
        void shouldParseGeneratedToken() {
            String token = jwtService.generateToken(42L, "tester");

            Optional<Claims> claims = jwtService.parseAndVerify(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().getSubject()).isEqualTo("42");
            assertThat(claims.get().get(JwtService.CLAIM_USER_ID, Long.class)).isEqualTo(42L);
            assertThat(claims.get().get(JwtService.CLAIM_NICKNAME, String.class)).isEqualTo("tester");
        }

        @Test
        @DisplayName("extractUserId：从合法 token 提取 userId")
        void shouldExtractUserId() {
            String token = jwtService.generateToken(100L, "u100");

            Optional<Long> userId = jwtService.extractUserId(token);

            assertThat(userId).contains(100L);
        }

        @Test
        @DisplayName("空 token：parseAndVerify 返回空")
        void shouldReturnEmptyForNullToken() {
            assertThat(jwtService.parseAndVerify(null)).isEmpty();
            assertThat(jwtService.parseAndVerify("")).isEmpty();
            assertThat(jwtService.parseAndVerify("   ")).isEmpty();
        }

        @Test
        @DisplayName("非法格式 token：parseAndVerify 返回空（不抛异常）")
        void shouldReturnEmptyForMalformedToken() {
            assertThat(jwtService.parseAndVerify("not.a.jwt")).isEmpty();
            assertThat(jwtService.parseAndVerify("randomstring")).isEmpty();
        }

        @Test
        @DisplayName("篡改后的 token：验证失败返回空")
        void shouldRejectTamperedToken() {
            String token = jwtService.generateToken(1L, "user");
            // 修改 payload 部分
            String[] parts = token.split("\\.");
            String tampered = parts[0] + "." + parts[1] + "X" + "." + parts[2];

            assertThat(jwtService.parseAndVerify(tampered)).isEmpty();
        }

        @Test
        @DisplayName("用不同密钥签发的 token：验证失败")
        void shouldRejectTokenFromDifferentKey() {
            // 用另一个密钥签发
            byte[] otherKey = new byte[32];
            for (int i = 0; i < 32; i++) {
                otherKey[i] = (byte) (100 + i);
            }
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret(Base64.getEncoder().encodeToString(otherKey));
            otherProps.setExpirationMs(3600_000L);
            JwtService otherService = new JwtService(otherProps);

            String foreignToken = otherService.generateToken(1L, "foreign");
            assertThat(jwtService.parseAndVerify(foreignToken)).isEmpty();
        }

        @Test
        @DisplayName("过期 token：验证失败返回空")
        void shouldRejectExpiredToken() {
            // expirationMs = 0 立即过期
            byte[] raw = new byte[32];
            JwtProperties props = new JwtProperties();
            props.setSecret(Base64.getEncoder().encodeToString(raw));
            props.setExpirationMs(1L); // 1ms
            JwtService shortLived = new JwtService(props);

            String token = shortLived.generateToken(1L, "u");
            // 等待过期
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(shortLived.parseAndVerify(token)).isEmpty();
        }
    }
}
