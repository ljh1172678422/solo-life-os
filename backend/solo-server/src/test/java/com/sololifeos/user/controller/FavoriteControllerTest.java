package com.sololifeos.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.user.application.FavoriteApplicationService;
import com.sololifeos.user.domain.model.Favorite;
import com.sololifeos.user.domain.model.FavoriteTarget;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link FavoriteController} MockMvc 测试。
 * <p>
 * 覆盖 4 端点：POST add、DELETE remove、GET list（全部 / 按类型）、GET check。
 * Favorite 归属 User Module，故测试文件位于 user 包下。
 * 路由前缀：/api/users/{userId}/favorites。
 */
@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FavoriteApplicationService favoriteAppService;

    private static final Long USER_ID = 1L;
    private static final Long TARGET_ID = 555L;
    private static final FavoriteTarget TARGET_TYPE = FavoriteTarget.LOCATION;

    private static Favorite buildFavorite(Long id) {
        Favorite fav = Favorite.create(USER_ID, TARGET_TYPE, TARGET_ID);
        try {
            java.lang.reflect.Field idF = Favorite.class.getDeclaredField("id");
            idF.setAccessible(true);
            idF.set(fav, id);
        } catch (ReflectiveOperationException ignored) {
        }
        return fav;
    }

    @BeforeEach
    void setUp() {
        FavoriteController controller = new FavoriteController(favoriteAppService);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/favorites 添加收藏")
    class AddTest {

        @Test
        @DisplayName("合法请求 → 200 + 收藏记录")
        void shouldAddFavorite() throws Exception {
            Favorite fav = buildFavorite(1L);
            when(favoriteAppService.addFavorite(eq(USER_ID), eq(TARGET_TYPE), eq(TARGET_ID)))
                    .thenReturn(fav);
            String body = """
                    {"targetType":"LOCATION","targetId":555}
                    """;

            mockMvc.perform(post("/api/users/{userId}/favorites", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.targetType").value("LOCATION"))
                    .andExpect(jsonPath("$.data.targetId").value(TARGET_ID));
        }

        @Test
        @DisplayName("targetType 缺失（@NotBlank）→ 400")
        void shouldReturn400WhenTargetTypeBlank() throws Exception {
            String body = """
                    {"targetType":"","targetId":555}
                    """;

            mockMvc.perform(post("/api/users/{userId}/favorites", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("targetId 缺失（@NotNull）→ 400")
        void shouldReturn400WhenTargetIdNull() throws Exception {
            String body = """
                    {"targetType":"LOCATION"}
                    """;

            mockMvc.perform(post("/api/users/{userId}/favorites", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("已收藏 → BusinessException 409")
        void shouldReturn409WhenAlreadyFavorited() throws Exception {
            when(favoriteAppService.addFavorite(eq(USER_ID), eq(TARGET_TYPE), eq(TARGET_ID)))
                    .thenThrow(new BusinessException("已收藏该目标（并发创建冲突）"));
            String body = """
                    {"targetType":"LOCATION","targetId":555}
                    """;

            mockMvc.perform(post("/api/users/{userId}/favorites", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.message").value("已收藏该目标（并发创建冲突）"));
        }

        @Test
        @DisplayName("非法 targetType 字符串：valueOf 抛 IllegalArgumentException → GlobalExceptionHandler")
        void shouldReturn400WhenInvalidTargetType() throws Exception {
            String body = """
                    {"targetType":"INVALID","targetId":1}
                    """;
            mockMvc.perform(post("/api/users/{userId}/favorites", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{userId}/favorites/{targetType}/{targetId} 删除")
    class RemoveTest {

        @Test
        @DisplayName("正常删除 → 200 成功")
        void shouldRemove() throws Exception {
            mockMvc.perform(delete(
                            "/api/users/{userId}/favorites/{targetType}/{targetId}",
                            USER_ID, "LOCATION", TARGET_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("未收藏 → BusinessException 409")
        void shouldReturn409WhenNotFavorited() throws Exception {
            org.mockito.Mockito.doThrow(new BusinessException("收藏记录不存在"))
                    .when(favoriteAppService)
                    .removeFavorite(eq(USER_ID), eq(TARGET_TYPE), eq(TARGET_ID));

            mockMvc.perform(delete(
                            "/api/users/{userId}/favorites/{targetType}/{targetId}",
                            USER_ID, "LOCATION", TARGET_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("收藏记录不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/favorites 列表")
    class ListTest {

        @Test
        @DisplayName("无 targetType → 全部")
        void shouldListAll() throws Exception {
            when(favoriteAppService.listFavorites(USER_ID))
                    .thenReturn(List.of(buildFavorite(1L), buildFavorite(2L)));

            mockMvc.perform(get("/api/users/{userId}/favorites", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("带 targetType → 按类型")
        void shouldListByType() throws Exception {
            when(favoriteAppService.listFavoritesByType(USER_ID, TARGET_TYPE))
                    .thenReturn(List.of(buildFavorite(1L)));

            mockMvc.perform(get("/api/users/{userId}/favorites", USER_ID)
                            .param("targetType", "LOCATION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/favorites/check 检查收藏状态")
    class CheckTest {

        @Test
        @DisplayName("已收藏 → true")
        void shouldReturnTrueWhenFavorited() throws Exception {
            when(favoriteAppService.checkFavorited(USER_ID, TARGET_TYPE, TARGET_ID))
                    .thenReturn(true);

            mockMvc.perform(get("/api/users/{userId}/favorites/check", USER_ID)
                            .param("targetType", "LOCATION")
                            .param("targetId", String.valueOf(TARGET_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("未收藏 → false")
        void shouldReturnFalseWhenNotFavorited() throws Exception {
            when(favoriteAppService.checkFavorited(USER_ID, TARGET_TYPE, 999L))
                    .thenReturn(false);

            mockMvc.perform(get("/api/users/{userId}/favorites/check", USER_ID)
                            .param("targetType", "LOCATION")
                            .param("targetId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("缺少必需 param → 400")
        void shouldReturn400WhenParamMissing() throws Exception {
            mockMvc.perform(get("/api/users/{userId}/favorites/check", USER_ID)
                            .param("targetType", "LOCATION"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}
