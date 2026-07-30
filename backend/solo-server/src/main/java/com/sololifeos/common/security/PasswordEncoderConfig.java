package com.sololifeos.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置 (ADR-0006 JWT Authentication)。
 * <p>
 * 仅引入 spring-security-crypto 的 {@link BCryptPasswordEncoder}，
 * 不引入完整 Spring Security 框架（无 SecurityFilterChain / UserDetailsService）。
 * <p>
 * BCrypt 是自适应哈希，默认 strength=10，计算成本约 100ms，
 * 足以抵御离线爆破，同时不影响 MVP 单实例登录性能。
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
