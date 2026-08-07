package com.sololifeos.explore.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Location} Entity 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖 create 工厂构造（6 参数校验 + type null → OTHER）+ update 字段替换（2 校验 + 合法替换）。
 * 不依赖数据库 / JPA Context。
 */
class LocationEntityTest {

    private static final BigDecimal LAT = new BigDecimal("30.2741000");
    private static final BigDecimal LNG = new BigDecimal("120.1551000");

    @Nested
    @DisplayName("create 工厂构造")
    class CreateTest {

        @Test
        @DisplayName("正常创建：全字段保留，type 指定值")
        void shouldCreateWithAllFields() {
            Location location = Location.create(
                    "隐山咖啡", "中山北路32号", "杭州",
                    LAT, LNG, LocationType.CAFE);

            assertThat(location.getName()).isEqualTo("隐山咖啡");
            assertThat(location.getAddress()).isEqualTo("中山北路32号");
            assertThat(location.getCity()).isEqualTo("杭州");
            assertThat(location.getLatitude()).isEqualByComparingTo(LAT);
            assertThat(location.getLongitude()).isEqualByComparingTo(LNG);
            assertThat(location.getType()).isEqualTo(LocationType.CAFE);
            assertThat(location.getId()).isNull();
        }

        @Test
        @DisplayName("type 为 null：默认 OTHER")
        void shouldFallbackTypeToOtherWhenNull() {
            Location location = Location.create(
                    "随机角落", null, "上海",
                    LAT, LNG, null);

            assertThat(location.getType()).isEqualTo(LocationType.OTHER);
            assertThat(location.getAddress()).isNull();
        }

        @Test
        @DisplayName("name 为空：抛 IllegalArgumentException")
        void shouldThrowWhenNameBlank() {
            assertThatThrownBy(() -> Location.create(
                    "  ", null, "杭州", LAT, LNG, LocationType.BOOKSTORE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("地点名称不可为空");
        }

        @Test
        @DisplayName("name 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenNameNull() {
            assertThatThrownBy(() -> Location.create(
                    null, null, "杭州", LAT, LNG, LocationType.BOOKSTORE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("地点名称不可为空");
        }

        @Test
        @DisplayName("city 为空：抛 IllegalArgumentException")
        void shouldThrowWhenCityBlank() {
            assertThatThrownBy(() -> Location.create(
                    "方所书店", "天河路", "", LAT, LNG, LocationType.BOOKSTORE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("所属城市不可为空");
        }

        @Test
        @DisplayName("latitude 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenLatitudeNull() {
            assertThatThrownBy(() -> Location.create(
                    "西湖", null, "杭州", null, LNG, LocationType.PARK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("纬度不可为空");
        }

        @Test
        @DisplayName("longitude 为 null：抛 IllegalArgumentException")
        void shouldThrowWhenLongitudeNull() {
            assertThatThrownBy(() -> Location.create(
                    "西湖", null, "杭州", LAT, null, LocationType.PARK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("经度不可为空");
        }
    }

    @Nested
    @DisplayName("update 更新信息")
    class UpdateTest {

        @Test
        @DisplayName("合法替换字段，type 指定值")
        void shouldUpdateAllFields() {
            Location location = Location.create(
                    "旧名称", "旧地址", "旧城市", LAT, LNG, LocationType.CAFE);

            location.update("新名称", "新地址", "新城市", LocationType.BOOKSTORE);

            assertThat(location.getName()).isEqualTo("新名称");
            assertThat(location.getAddress()).isEqualTo("新地址");
            assertThat(location.getCity()).isEqualTo("新城市");
            assertThat(location.getType()).isEqualTo(LocationType.BOOKSTORE);
        }

        @Test
        @DisplayName("update type 为 null → OTHER")
        void shouldFallbackTypeToOtherWhenUpdateNull() {
            Location location = Location.create(
                    "名称", null, "城市", LAT, LNG, LocationType.EXHIBITION);

            location.update("名称", "地址", "城市", null);

            assertThat(location.getType()).isEqualTo(LocationType.OTHER);
        }

        @Test
        @DisplayName("update name 为空：抛 IllegalArgumentException")
        void shouldThrowWhenUpdateNameBlank() {
            Location location = Location.create(
                    "名称", null, "城市", LAT, LNG, LocationType.CAFE);

            assertThatThrownBy(() -> location.update("   ", "地址", "城市", LocationType.CAFE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("地点名称不可为空");
        }

        @Test
        @DisplayName("update city 为空：抛 IllegalArgumentException")
        void shouldThrowWhenUpdateCityBlank() {
            Location location = Location.create(
                    "名称", null, "城市", LAT, LNG, LocationType.CAFE);

            assertThatThrownBy(() -> location.update("名称", "地址", null, LocationType.CAFE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("所属城市不可为空");
        }
    }
}
