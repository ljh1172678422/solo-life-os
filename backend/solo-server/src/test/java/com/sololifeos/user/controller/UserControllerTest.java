package com.sololifeos.user.controller;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.user.application.UserApplicationService;
import com.sololifeos.user.domain.model.User;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link UserController} API 测试 (CODE_RULES §10)。
 * <p>
 * 用 standaloneSetup 构建 MockMvc，不加载 Spring context，
 * 避免 spring-security-crypto 触发 SecurityAutoConfiguration 干扰。
 * ControllerAdvice（GlobalExceptionHandler）通过 setControllerAdvice 注册。
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserApplicationService userApplicationService;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Nested
    @DisplayName("POST /api/users 注册")
    class RegisterTest {

        @Test
        @DisplayName("合法请求：返回 200 + ApiResponse{code=0} + 用户信息")
        void shouldRegisterSuccessfully() throws Exception {
            User saved = buildUser(1L, "tester", "t@e.com", "ACTIVE");
            when(userApplicationService.register(eq("tester"), eq("t@e.com"), eq("13800138000"), eq("secret123")))
                    .thenReturn(saved);

            String body = """
                    {"nickname":"tester","email":"t@e.com","phone":"13800138000","password":"secret123"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("tester"))
                    .andExpect(jsonPath("$.data.email").value("t@e.com"));
        }

        @Test
        @DisplayName("缺少昵称：返回 400（参数校验失败）")
        void shouldReturn400WhenNicknameMissing() throws Exception {
            String body = """
                    {"email":"t@e.com","password":"secret123"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少密码：返回 400（参数校验失败）")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            String body = """
                    {"nickname":"tester","email":"t@e.com"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("密码过短（<6）：返回 400")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            String body = """
                    {"nickname":"tester","email":"t@e.com","password":"123"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("邮箱格式错误：返回 400")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            String body = """
                    {"nickname":"tester","email":"not-an-email","password":"secret123"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("邮箱已注册：业务异常，GlobalExceptionHandler 返回对应状态")
        void shouldHandleBusinessException() throws Exception {
            when(userApplicationService.register(anyString(), any(), any(), anyString()))
                    .thenThrow(new BusinessException("邮箱已被注册: dup@e.com"));

            String body = """
                    {"nickname":"tester","email":"dup@e.com","password":"secret123"}
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} 查询资料")
    class GetByIdTest {

        @Test
        @DisplayName("用户存在：返回 200 + 用户信息")
        void shouldReturnUserWhenExists() throws Exception {
            User user = buildUser(5L, "tester", "t@e.com", "ACTIVE");
            when(userApplicationService.getById(5L)).thenReturn(user);

            mockMvc.perform(get("/api/users/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(5))
                    .andExpect(jsonPath("$.data.nickname").value("tester"));
        }

        @Test
        @DisplayName("用户不存在：BusinessException 转为 400")
        void shouldReturn400WhenUserNotFound() throws Exception {
            when(userApplicationService.getById(anyLong()))
                    .thenThrow(new BusinessException("用户不存在: id=999"));

            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} 更新资料")
    class UpdateTest {

        @Test
        @DisplayName("合法请求：返回 200 + 更新后信息")
        void shouldUpdateSuccessfully() throws Exception {
            User updated = buildUser(1L, "newname", "t@e.com", "ACTIVE");
            when(userApplicationService.updateProfile(eq(1L), eq("newname"), eq("https://a.png"), eq("Beijing")))
                    .thenReturn(updated);

            String body = """
                    {"nickname":"newname","avatar":"https://a.png","city":"Beijing"}
                    """;

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nickname").value("newname"));
        }

        @Test
        @DisplayName("昵称为空：返回 400")
        void shouldReturn400WhenNicknameBlankOnUpdate() throws Exception {
            String body = """
                    {"avatar":"https://a.png"}
                    """;

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    /** 构造测试用 User（含 id）。 */
    private static User buildUser(Long id, String nickname, String email, String status) {
        User user = new User(nickname, email, null);
        user.activate();
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
            var createdField = User.class.getDeclaredField("createdTime");
            createdField.setAccessible(true);
            createdField.set(user, LocalDateTime.now());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return user;
    }
}
