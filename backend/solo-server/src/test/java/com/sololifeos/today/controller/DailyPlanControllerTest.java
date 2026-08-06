package com.sololifeos.today.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.today.application.DailyPlanApplicationService;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link DailyPlanController} API 测试 (ADR-0006, CODE_RULES §10)。
 * <p>
 * 用 standaloneSetup 构建 MockMvc，不加载 Spring context。
 * 配置 ISO 日期 ConversionService 以支持 {@code @DateTimeFormat(iso=DATE)} 查询参数绑定。
 */
@ExtendWith(MockitoExtension.class)
class DailyPlanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DailyPlanApplicationService planApplicationService;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 6);

    @BeforeEach
    void setUp() {
        DailyPlanController controller = new DailyPlanController(planApplicationService);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setConversionService(isoDateConversionService())
                .build();
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/plans 创建计划")
    class CreateTest {

        @Test
        @DisplayName("合法请求：返回 200 + 计划数据")
        void shouldCreatePlanSuccessfully() throws Exception {
            DailyPlan plan = buildPersistedPlan();
            when(planApplicationService.createPlan(USER_ID, DATE)).thenReturn(plan);

            String body = """
                    {"date":"2026-08-06"}
                    """;

            mockMvc.perform(post("/api/users/{userId}/plans", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.date").value("2026-08-06"))
                    .andExpect(jsonPath("$.data.status").value("PLANNING"));
        }

        @Test
        @DisplayName("缺少 date：返回 400（参数校验失败）")
        void shouldReturn400WhenDateMissing() throws Exception {
            String body = "{}";

            mockMvc.perform(post("/api/users/{userId}/plans", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("当日已存在计划：BusinessException 转 400")
        void shouldReturn400WhenPlanAlreadyExists() throws Exception {
            when(planApplicationService.createPlan(eq(USER_ID), any(LocalDate.class)))
                    .thenThrow(new BusinessException("该日期已存在计划: 2026-08-06"));

            String body = """
                    {"date":"2026-08-06"}
                    """;

            mockMvc.perform(post("/api/users/{userId}/plans", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/plans/today 查询今日计划")
    class GetTodayTest {

        @Test
        @DisplayName("存在今日计划：返回 200 + 计划数据")
        void shouldReturnTodayPlan() throws Exception {
            DailyPlan plan = buildPersistedPlan();
            when(planApplicationService.getPlanByUserAndDate(USER_ID, DATE)).thenReturn(Optional.of(plan));

            mockMvc.perform(get("/api/users/{userId}/plans/today", USER_ID)
                            .param("date", "2026-08-06"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.status").value("PLANNING"));
        }

        @Test
        @DisplayName("不存在今日计划：返回 200 + data=null（非 404）")
        void shouldReturnNullWhenNoTodayPlan() throws Exception {
            when(planApplicationService.getPlanByUserAndDate(USER_ID, DATE)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/{userId}/plans/today", USER_ID)
                            .param("date", "2026-08-06"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/plans 计划列表")
    class ListTest {

        @Test
        @DisplayName("无筛选：返回全部计划")
        void shouldListAllPlans() throws Exception {
            when(planApplicationService.listUserPlans(USER_ID)).thenReturn(List.of(buildPersistedPlan()));

            mockMvc.perform(get("/api/users/{userId}/plans", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data[0].userId").value(1));
        }

        @Test
        @DisplayName("按日期范围筛选：startDate + endDate")
        void shouldListByDateRange() throws Exception {
            when(planApplicationService.listPlansByDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/users/{userId}/plans", USER_ID)
                            .param("startDate", "2026-08-01")
                            .param("endDate", "2026-08-07"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("按状态筛选：status=PLANNING")
        void shouldListByStatus() throws Exception {
            when(planApplicationService.listPlansByStatus(USER_ID, PlanStatus.PLANNING)).thenReturn(List.of());

            mockMvc.perform(get("/api/users/{userId}/plans", USER_ID)
                            .param("status", "PLANNING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("单计划操作")
    class SinglePlanTest {

        @Test
        @DisplayName("GET /api/plans/{planId}：返回计划")
        void shouldGetPlanById() throws Exception {
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(buildPersistedPlan());

            mockMvc.perform(get("/api/plans/{planId}", PLAN_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(100));
        }

        @Test
        @DisplayName("GET /api/plans/{planId}：不存在转 400")
        void shouldReturn400WhenPlanNotFound() throws Exception {
            when(planApplicationService.getPlanById(PLAN_ID))
                    .thenThrow(new BusinessException("计划不存在: id=" + PLAN_ID));

            mockMvc.perform(get("/api/plans/{planId}", PLAN_ID))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/plans/{planId}/start：PLANNING → ONGOING")
        void shouldStartPlan() throws Exception {
            when(planApplicationService.startPlan(PLAN_ID)).thenReturn(buildPersistedPlan());

            mockMvc.perform(post("/api/plans/{planId}/start", PLAN_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("POST /api/plans/{planId}/complete：ONGOING → COMPLETED")
        void shouldCompletePlan() throws Exception {
            when(planApplicationService.completePlan(PLAN_ID)).thenReturn(buildPersistedPlan());

            mockMvc.perform(post("/api/plans/{planId}/complete", PLAN_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/plans/{planId}/cancel：→ CANCELLED")
        void shouldCancelPlan() throws Exception {
            when(planApplicationService.cancelPlan(PLAN_ID)).thenReturn(buildPersistedPlan());

            mockMvc.perform(post("/api/plans/{planId}/cancel", PLAN_ID))
                    .andExpect(status().isOk());
        }
    }

    /** 构造已持久化的计划用于 controller 返回（含 id 与时间戳）。 */
    private static DailyPlan buildPersistedPlan() {
        DailyPlan plan = DailyPlan.create(USER_ID, DATE);
        try {
            var idField = DailyPlan.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(plan, PLAN_ID);
            var createdField = DailyPlan.class.getDeclaredField("createdTime");
            createdField.setAccessible(true);
            createdField.set(plan, LocalDateTime.now());
            var updatedField = DailyPlan.class.getDeclaredField("updatedTime");
            updatedField.setAccessible(true);
            updatedField.set(plan, LocalDateTime.now());
        } catch (Exception e) {
            throw new IllegalStateException("设置测试字段失败", e);
        }
        return plan;
    }

    /** 注册 ISO LocalDate / LocalDateTime 转换器，支持 @DateTimeFormat(iso=DATE) 查询参数绑定。 */
    private static FormattingConversionService isoDateConversionService() {
        DefaultFormattingConversionService service = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(service);
        return service;
    }
}
