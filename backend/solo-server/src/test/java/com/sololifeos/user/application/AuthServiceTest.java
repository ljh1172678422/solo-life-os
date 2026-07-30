package com.sololifeos.user.application;

import com.sololifeos.common.exception.AuthException;
import com.sololifeos.common.security.JwtService;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserStatus;
import com.sololifeos.user.dto.LoginRequest;
import com.sololifeos.user.dto.LoginResponse;
import com.sololifeos.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthService} 单元测试 (ADR-0006, CODE_RULES §10)。
 * <p>
 * 覆盖登录用例：成功 / 账号不存在 / 密码错误 / 封禁用户 / 邮箱手机号分支。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private static final String RAW_PWD = "secret123";
    private static final String HASHED_PWD = "$2a$10$hashedpasswordplaceholder";
    private static final String JWT_TOKEN = "jwt.token.value";

    @Nested
    @DisplayName("login 登录用例")
    class LoginTest {

        @Test
        @DisplayName("邮箱登录成功：返回 JWT token + 用户信息")
        void shouldLoginSuccessfullyByEmail() {
            User user = buildActiveUser(1L, "tester", "test@example.com", "13800138000", HASHED_PWD);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(RAW_PWD, HASHED_PWD)).thenReturn(true);
            when(jwtService.generateToken(1L, "tester")).thenReturn(JWT_TOKEN);

            LoginResponse res = authService.login(new LoginRequest("test@example.com", RAW_PWD));

            assertThat(res.token()).isEqualTo(JWT_TOKEN);
            assertThat(res.userId()).isEqualTo(1L);
            assertThat(res.nickname()).isEqualTo("tester");
            verify(passwordEncoder, times(1)).matches(RAW_PWD, HASHED_PWD);
            verify(jwtService, times(1)).generateToken(1L, "tester");
        }

        @Test
        @DisplayName("手机号登录成功：含 @ 走邮箱分支，不含 @ 走手机号分支")
        void shouldLoginSuccessfullyByPhone() {
            User user = buildActiveUser(2L, "phoneuser", null, "13800138000", HASHED_PWD);
            when(userRepository.findByPhone("13800138000")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(RAW_PWD, HASHED_PWD)).thenReturn(true);
            when(jwtService.generateToken(2L, "phoneuser")).thenReturn(JWT_TOKEN);

            LoginResponse res = authService.login(new LoginRequest("13800138000", RAW_PWD));

            assertThat(res.token()).isEqualTo(JWT_TOKEN);
            assertThat(res.userId()).isEqualTo(2L);
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository, times(1)).findByPhone("13800138000");
        }

        @Test
        @DisplayName("账号不存在：抛 AuthException（防账号枚举，message 统一）")
        void shouldThrowWhenAccountNotFound() {
            when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", RAW_PWD)))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("账号或密码错误");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("密码错误：抛 AuthException（message 与账号不存在一致，防枚举）")
        void shouldThrowWhenPasswordMismatch() {
            User user = buildActiveUser(3L, "tester", "test@example.com", null, HASHED_PWD);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(RAW_PWD, HASHED_PWD)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", RAW_PWD)))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("账号或密码错误");
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("用户密码为 null（存量数据）：抛 AuthException")
        void shouldThrowWhenPasswordIsNull() {
            User user = buildActiveUser(4L, "tester", "test@example.com", null, null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", RAW_PWD)))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("账号或密码错误");
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("封禁用户：抛 AuthException（message 区分，提示账号被封禁）")
        void shouldThrowWhenUserBanned() {
            User user = buildActiveUser(5L, "banned", "banned@example.com", null, HASHED_PWD);
            user.ban();
            when(userRepository.findByEmail("banned@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(RAW_PWD, HASHED_PWD)).thenReturn(true);

            assertThatThrownBy(() -> authService.login(new LoginRequest("banned@example.com", RAW_PWD)))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("封禁");
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("空账号：抛 AuthException（账号或密码错误）")
        void shouldThrowWhenAccountBlank() {
            assertThatThrownBy(() -> authService.login(new LoginRequest("", RAW_PWD)))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("账号或密码错误");
            verify(userRepository, never()).findByEmail(any());
            verify(userRepository, never()).findByPhone(any());
        }
    }

    /** 构造一个 ACTIVE 状态用户，用于登录测试。 */
    private static User buildActiveUser(Long id, String nickname, String email, String phone, String hashedPwd) {
        User user = User.register(nickname, email, phone, hashedPwd);
        user.activate();
        // 反射设置 id（Entity id 由 DB 生成，测试需模拟）
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new IllegalStateException("设置测试 id 失败", e);
        }
        return user;
    }
}
