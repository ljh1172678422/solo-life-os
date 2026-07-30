package com.sololifeos.user.controller;

import com.sololifeos.common.exception.AuthException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.user.application.AuthService;
import com.sololifeos.user.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AuthController} API 测试 (ADR-0006, CODE_RULES §10)。
 * <p>
 * 用 standaloneSetup 构建 MockMvc，不加载 Spring context，
 * 避免 spring-security-crypto 触发 SecurityAutoConfiguration 干扰。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/login 登录")
    class LoginTest {

        @Test
        @DisplayName("合法请求：返回 200 + token + 用户信息")
        void shouldLoginSuccessfully() throws Exception {
            when(authService.login(any())).thenReturn(new LoginResponse("jwt.token", 1L, "tester"));

            String body = """
                    {"account":"t@e.com","password":"secret123"}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.token").value("jwt.token"))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("tester"));
        }

        @Test
        @DisplayName("缺少账号：返回 400（参数校验失败）")
        void shouldReturn400WhenAccountMissing() throws Exception {
            String body = """
                    {"password":"secret123"}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少密码：返回 400")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            String body = """
                    {"account":"t@e.com"}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("密码过短（<6）：返回 400")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            String body = """
                    {"account":"t@e.com","password":"123"}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("账号或密码错误：AuthException 转为对应状态")
        void shouldHandleAuthException() throws Exception {
            when(authService.login(any())).thenThrow(new AuthException("账号或密码错误"));

            String body = """
                    {"account":"nobody@e.com","password":"wrongpwd"}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }
    }
}
