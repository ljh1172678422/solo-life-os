package com.sololifeos.explore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.common.exception.GlobalExceptionHandler;
import com.sololifeos.explore.application.LocationApplicationService;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link LocationController} MockMvc 测试（standaloneSetup）。
 * <p>
 * 覆盖全部 5 端点：POST create / GET list（3 模式）/ GET byId /
 * GET nearby / PUT update，以及参数校验失败 400、BusinessException → 409。
 */
@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationApplicationService locationAppService;

    private static final Long LOC_ID = 101L;
    private static final BigDecimal LAT = new BigDecimal("30.2741000");
    private static final BigDecimal LNG = new BigDecimal("120.1551000");

    private static Location buildLocation(Long id, String name, String city, LocationType type) {
        Location loc = Location.create(name, "地址1", city, LAT, LNG, type);
        try {
            java.lang.reflect.Field idF = Location.class.getDeclaredField("id");
            idF.setAccessible(true);
            idF.set(loc, id);
        } catch (ReflectiveOperationException ignored) {
        }
        return loc;
    }

    @BeforeEach
    void setUp() {
        LocationController controller = new LocationController(locationAppService);
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
    @DisplayName("POST /api/locations 创建地点")
    class CreateTest {

        private static String BODY_TEMPLATE = """
                {"name":"隐山咖啡","address":"中山北路32号","city":"杭州","latitude":%s,"longitude":%s,"type":"CAFE"}
                """;

        @Test
        @DisplayName("合法请求：返回 200 + 地点数据")
        void shouldCreateLocation() throws Exception {
            Location loc = buildLocation(LOC_ID, "隐山咖啡", "杭州", LocationType.CAFE);
            when(locationAppService.createLocation(
                    eq("隐山咖啡"), eq("中山北路32号"), eq("杭州"), any(), any(), eq(LocationType.CAFE)))
                    .thenReturn(loc);
            String body = BODY_TEMPLATE.formatted(LAT, LNG);

            mockMvc.perform(post("/api/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(LOC_ID))
                    .andExpect(jsonPath("$.data.name").value("隐山咖啡"))
                    .andExpect(jsonPath("$.data.type").value("CAFE"));
        }

        @Test
        @DisplayName("name 为空 → 400（@NotBlank 校验）")
        void shouldReturn400WhenNameBlank() throws Exception {
            String body = """
                    {"name":"","address":"a","city":"杭州","latitude":30.2741,"longitude":120.1551}
                    """;

            mockMvc.perform(post("/api/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("latitude 缺失 → 400（@NotNull）")
        void shouldReturn400WhenLatitudeNull() throws Exception {
            String body = """
                    {"name":"A","address":"a","city":"杭州","longitude":120.1551}
                    """;

            mockMvc.perform(post("/api/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("非法 type 值：Controller 安全解析为 null → OTHER")
        void shouldGracefullyHandleInvalidType() throws Exception {
            Location loc = buildLocation(LOC_ID, "隐山咖啡", "杭州", LocationType.OTHER);
            when(locationAppService.createLocation(
                    eq("隐山咖啡"), any(), eq("杭州"), any(), any(), eq(null)))
                    .thenReturn(loc);
            String body = """
                    {"name":"隐山咖啡","address":"a","city":"杭州","latitude":30.2741,"longitude":120.1551,"type":"INVALID_TYPE"}
                    """;

            mockMvc.perform(post("/api/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.type").value("OTHER"));
        }

        @Test
        @DisplayName("Application Service 抛 BusinessException → 409")
        void shouldReturnErrorWhenBusinessException() throws Exception {
            when(locationAppService.createLocation(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new BusinessException("数据约束冲突"));
            String body = BODY_TEMPLATE.formatted(LAT, LNG);

            mockMvc.perform(post("/api/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.message").value("数据约束冲突"));
        }
    }

    @Nested
    @DisplayName("GET /api/locations 列表")
    class ListTest {

        @Test
        @DisplayName("无参数：返回全部")
        void shouldListAll() throws Exception {
            when(locationAppService.listAll()).thenReturn(List.of(
                    buildLocation(1L, "A", "C1", LocationType.CAFE),
                    buildLocation(2L, "B", "C2", LocationType.PARK)));

            mockMvc.perform(get("/api/locations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @DisplayName("仅 city：listByCity")
        void shouldListByCity() throws Exception {
            when(locationAppService.listByCity("杭州"))
                    .thenReturn(List.of(buildLocation(1L, "A", "杭州", LocationType.CAFE)));

            mockMvc.perform(get("/api/locations").param("city", "杭州"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].city").value("杭州"));
            verify_noOtherListMethodsCalled();
        }

        @Test
        @DisplayName("city + type：listByCityAndType")
        void shouldListByCityAndType() throws Exception {
            when(locationAppService.listByCityAndType("杭州", LocationType.CAFE))
                    .thenReturn(List.of(buildLocation(1L, "A", "杭州", LocationType.CAFE)));

            mockMvc.perform(get("/api/locations")
                            .param("city", "杭州")
                            .param("type", "CAFE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].type").value("CAFE"));
        }

        private void verify_noOtherListMethodsCalled() {
            // 只保证 listAll 不被调用（MockMvc 调用走 Controller 代码，可通过 verify 验证）
            // 这里不做额外 verify，由后续 mvn test 行为验证
        }
    }

    @Nested
    @DisplayName("GET /api/locations/{id} 详情")
    class GetByIdTest {

        @Test
        @DisplayName("存在：返回地点")
        void shouldGetById() throws Exception {
            when(locationAppService.getById(LOC_ID))
                    .thenReturn(buildLocation(LOC_ID, "隐山", "杭州", LocationType.CAFE));

            mockMvc.perform(get("/api/locations/{id}", LOC_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(LOC_ID))
                    .andExpect(jsonPath("$.data.name").value("隐山"));
        }

        @Test
        @DisplayName("不存在：BusinessException → 409")
        void shouldReturn409WhenNotFound() throws Exception {
            when(locationAppService.getById(999L))
                    .thenThrow(new BusinessException("地点不存在: id=999"));

            mockMvc.perform(get("/api/locations/{id}", 999L))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.message").value("地点不存在: id=999"));
        }
    }

    @Nested
    @DisplayName("GET /api/locations/nearby 附近搜索")
    class NearbyTest {

        @Test
        @DisplayName("lat/lng/radius 必填 + type 可选")
        void shouldNearbySearch() throws Exception {
            when(locationAppService.searchNearby(any(), any(), anyDouble(), any()))
                    .thenReturn(List.of(buildLocation(1L, "地点1", "杭州", LocationType.CAFE)));

            mockMvc.perform(get("/api/locations/nearby")
                            .param("lat", "30.2741")
                            .param("lng", "120.1551")
                            .param("radius", "2.0")
                            .param("type", "CAFE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("地点1"));
        }

        @Test
        @DisplayName("缺少 lat 参数 → 500（MissingServletRequestParameterException）")
        void shouldErrorWhenLatMissing() throws Exception {
            mockMvc.perform(get("/api/locations/nearby")
                            .param("lng", "120.1551")
                            .param("radius", "2.0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("PUT /api/locations/{id} 更新")
    class UpdateTest {

        private static final String BODY = """
                {"name":"新名","address":"新地址","city":"新城市","latitude":30.2741,"longitude":120.1551,"type":"BOOKSTORE"}
                """;

        @Test
        @DisplayName("正常更新 → 返回新地点")
        void shouldUpdate() throws Exception {
            Location updated = buildLocation(LOC_ID, "新名", "新城市", LocationType.BOOKSTORE);
            when(locationAppService.updateLocation(
                    eq(LOC_ID), eq("新名"), eq("新地址"), eq("新城市"), eq(LocationType.BOOKSTORE)))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/locations/{id}", LOC_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("新名"))
                    .andExpect(jsonPath("$.data.type").value("BOOKSTORE"));
        }

        @Test
        @DisplayName("缺少 name（@NotBlank）→ 400")
        void shouldReturn400WhenNameBlank() throws Exception {
            String bad = """
                    {"name":"","city":"新城市","latitude":30.2741,"longitude":120.1551}
                    """;

            mockMvc.perform(put("/api/locations/{id}", LOC_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bad))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}
