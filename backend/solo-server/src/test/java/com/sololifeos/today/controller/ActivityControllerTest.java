package com.sololifeos.today.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.today.application.ActivityApplicationService;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link ActivityController} API 测试 (ADR-0006, CODE_RULES §10)。
 * <p>
 * 用 standaloneSetup 构建 MockMvc，配置 JavaTimeModule 以正确序列化 / 反序列化 LocalDateTime。
 */
@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ActivityApplicationService activityApplicationService;

    private static final Long PLAN_ID = 100L;
    private static final Long ACTIVITY_ID = 200L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 6, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 8, 6, 10, 30);

    @BeforeEach
    void setUp() {
        ActivityController controller = new ActivityController(activityApplicationService);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Nested
    @DisplayName("POST /api/plans/{planId}/activities 添加活动")
    class CreateTest {

        @Test
        @DisplayName("合法请求：返回 200 + 活动数据")
        void shouldCreateActivitySuccessfully() throws Exception {
            Activity activity = buildPersistedActivity();
            when(activityApplicationService.addActivity(eq(PLAN_ID), eq("晨跑"), eq(ActivityType.SPORT), any(LocalDateTime.class)))
                    .thenReturn(activity);

            String body = """
                    {"title":"晨跑","type":"SPORT","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.title").value("晨跑"))
                    .andExpect(jsonPath("$.data.type").value("SPORT"));
        }

        @Test
        @DisplayName("缺少 title：返回 400")
        void shouldReturn400WhenTitleMissing() throws Exception {
            String body = """
                    {"type":"SPORT","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少 startTime：返回 400")
        void shouldReturn400WhenStartTimeMissing() throws Exception {
            String body = """
                    {"title":"晨跑","type":"SPORT"}
                    """;

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("title 超长（>200）：返回 400")
        void shouldReturn400WhenTitleTooLong() throws Exception {
            String longTitle = "a".repeat(201);
            String body = """
                    {"title":"%s","type":"SPORT","startTime":"2026-08-06T09:00:00"}
                    """.formatted(longTitle);

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("type 为空：合法（null → OTHER，校验不拦，由 Entity 兜底）")
        void shouldAllowNullType() throws Exception {
            Activity activity = buildPersistedActivity();
            when(activityApplicationService.addActivity(eq(PLAN_ID), eq("杂事"), eq(null), any(LocalDateTime.class)))
                    .thenReturn(activity);

            String body = """
                    {"title":"杂事","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("计划已关闭：BusinessException 转 400")
        void shouldReturn400WhenPlanClosed() throws Exception {
            when(activityApplicationService.addActivity(eq(PLAN_ID), any(), any(), any()))
                    .thenThrow(new BusinessException("已COMPLETED的计划不可添加活动"));

            String body = """
                    {"title":"活动","type":"WORK","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(post("/api/plans/{planId}/activities", PLAN_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET 查询用例")
    class QueryTest {

        @Test
        @DisplayName("GET /api/plans/{planId}/activities：列出计划下活动")
        void shouldListActivitiesByPlan() throws Exception {
            when(activityApplicationService.listActivitiesByPlan(PLAN_ID)).thenReturn(List.of(buildPersistedActivity()));

            mockMvc.perform(get("/api/plans/{planId}/activities", PLAN_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].title").value("晨跑"));
        }

        @Test
        @DisplayName("GET /api/activities/{activityId}：返回活动")
        void shouldGetActivityById() throws Exception {
            when(activityApplicationService.getActivity(ACTIVITY_ID)).thenReturn(buildPersistedActivity());

            mockMvc.perform(get("/api/activities/{activityId}", ACTIVITY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(200));
        }

        @Test
        @DisplayName("GET /api/activities/{activityId}：不存在转 400")
        void shouldReturn400WhenActivityNotFound() throws Exception {
            when(activityApplicationService.getActivity(ACTIVITY_ID))
                    .thenThrow(new BusinessException("活动不存在: id=" + ACTIVITY_ID));

            mockMvc.perform(get("/api/activities/{activityId}", ACTIVITY_ID))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/plans/{planId}/activities/{activityId} 修改活动")
    class UpdateTest {

        @Test
        @DisplayName("合法请求：返回 200 + 更新后活动")
        void shouldUpdateActivitySuccessfully() throws Exception {
            when(activityApplicationService.updateActivity(
                    eq(PLAN_ID), eq(ACTIVITY_ID), eq("新标题"), eq(ActivityType.STUDY), any(LocalDateTime.class), any()))
                    .thenReturn(buildPersistedActivity());

            String body = """
                    {"title":"新标题","type":"STUDY","startTime":"2026-08-06T09:00:00","endTime":"2026-08-06T10:30:00"}
                    """;

            mockMvc.perform(put("/api/plans/{planId}/activities/{activityId}", PLAN_ID, ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("缺少 type：返回 400")
        void shouldReturn400WhenTypeMissing() throws Exception {
            String body = """
                    {"title":"新标题","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(put("/api/plans/{planId}/activities/{activityId}", PLAN_ID, ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("活动不属于计划：BusinessException 转 400")
        void shouldReturn400WhenActivityNotBelongToPlan() throws Exception {
            when(activityApplicationService.updateActivity(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new BusinessException("活动不属于该计划"));

            String body = """
                    {"title":"新标题","type":"STUDY","startTime":"2026-08-06T09:00:00"}
                    """;

            mockMvc.perform(put("/api/plans/{planId}/activities/{activityId}", PLAN_ID, ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/activities/{activityId}/end 结束活动")
    class EndTest {

        @Test
        @DisplayName("设置结束时间：返回 200")
        void shouldEndActivity() throws Exception {
            when(activityApplicationService.endActivity(eq(ACTIVITY_ID), any(LocalDateTime.class)))
                    .thenReturn(buildPersistedActivity());

            String body = """
                    {"endTime":"2026-08-06T10:30:00"}
                    """;

            mockMvc.perform(post("/api/activities/{activityId}/end", ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("空 body（清除结束时间）：合法")
        void shouldAllowEmptyBodyToEnd() throws Exception {
            when(activityApplicationService.endActivity(eq(ACTIVITY_ID), eq(null)))
                    .thenReturn(buildPersistedActivity());

            mockMvc.perform(post("/api/activities/{activityId}/end", ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/activities/{activityId}/locate 绑定地点")
    class LocateTest {

        @Test
        @DisplayName("绑定地点：返回 200")
        void shouldLocateActivity() throws Exception {
            when(activityApplicationService.locateActivity(ACTIVITY_ID, 777L))
                    .thenReturn(buildPersistedActivity());

            String body = """
                    {"locationId":777}
                    """;

            mockMvc.perform(post("/api/activities/{activityId}/locate", ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    /** 构造已持久化的活动用于 controller 返回。 */
    private static Activity buildPersistedActivity() {
        Activity activity = Activity.create(PLAN_ID, "晨跑", ActivityType.SPORT, START);
        try {
            var idField = Activity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activity, ACTIVITY_ID);
            var createdField = Activity.class.getDeclaredField("createdTime");
            createdField.setAccessible(true);
            createdField.set(activity, LocalDateTime.now());
            var updatedField = Activity.class.getDeclaredField("updatedTime");
            updatedField.setAccessible(true);
            updatedField.set(activity, LocalDateTime.now());
        } catch (Exception e) {
            throw new IllegalStateException("设置测试字段失败", e);
        }
        return activity;
    }
}
