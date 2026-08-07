package com.sololifeos.explore.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.explore.domain.model.Location;
import com.sololifeos.explore.domain.model.LocationType;
import com.sololifeos.explore.domain.service.ExploreDomainService;
import com.sololifeos.explore.repository.LocationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LocationApplicationService} 单元测试。
 * <p>
 * 覆盖：create（含 DataIntegrityViolationException → BusinessException 并发冲突兜底）、
 * getById（存在 / 不存在）、listAll / listByCity / listByCityAndType、
 * searchNearby（有类型 / 无类型）、update（getById 抛出 / 成功替换）。
 */
@ExtendWith(MockitoExtension.class)
class LocationApplicationServiceTest {

    @Mock
    private ExploreDomainService exploreDomainService;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationApplicationService locationAppService;

    private static final Long LOC_ID = 101L;
    private static final BigDecimal LAT = new BigDecimal("30.2741000");
    private static final BigDecimal LNG = new BigDecimal("120.1551000");

    private Location buildPersistedLocation(Long id) {
        Location location = Location.create(
                "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE);
        try {
            java.lang.reflect.Field idF = Location.class.getDeclaredField("id");
            idF.setAccessible(true);
            idF.set(location, id);
        } catch (ReflectiveOperationException ignored) {
        }
        return location;
    }

    @Nested
    @DisplayName("createLocation 创建地点")
    class CreateLocationTest {

        @Test
        @DisplayName("正常创建：Domain → save，返回持久化对象")
        void shouldCreateAndPersist() {
            Location location = Location.create(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE);
            when(exploreDomainService.createLocation(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE))
                    .thenReturn(location);
            Location persisted = buildPersistedLocation(LOC_ID);
            when(locationRepository.save(location)).thenReturn(persisted);

            Location result = locationAppService.createLocation(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE);

            assertThat(result).isEqualTo(persisted);
            assertThat(result.getId()).isEqualTo(LOC_ID);
            verify(locationRepository).save(location);
        }

        @Test
        @DisplayName("并发冲突：DB 唯一约束 → BusinessException")
        void shouldConvertDataIntegrityViolationToBusinessException() {
            Location location = Location.create(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE);
            when(exploreDomainService.createLocation(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE))
                    .thenReturn(location);
            when(locationRepository.save(location))
                    .thenThrow(new DataIntegrityViolationException("uk_location_x_y_z"));

            assertThatThrownBy(() -> locationAppService.createLocation(
                    "隐山咖啡", "中山北路32号", "杭州", LAT, LNG, LocationType.CAFE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("数据约束冲突");
        }

        @Test
        @DisplayName("Domain 抛 BusinessException：透传，不 save")
        void shouldPropagateFromDomain() {
            when(exploreDomainService.createLocation(
                    null, null, null, LAT, LNG, null))
                    .thenThrow(new IllegalArgumentException("地点名称不可为空"));

            assertThatThrownBy(() -> locationAppService.createLocation(
                    null, null, null, LAT, LNG, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("地点名称不可为空");
            verify(locationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("查询用例")
    class QueryTest {

        @Test
        @DisplayName("getById：存在 → 返回地点")
        void shouldGetByIdWhenExists() {
            Location loc = buildPersistedLocation(LOC_ID);
            when(locationRepository.findById(LOC_ID)).thenReturn(Optional.of(loc));

            Location result = locationAppService.getById(LOC_ID);

            assertThat(result).isSameAs(loc);
        }

        @Test
        @DisplayName("getById：不存在 → BusinessException")
        void shouldThrowWhenGetByIdNotFound() {
            when(locationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> locationAppService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("地点不存在: id=999");
        }

        @Test
        @DisplayName("listAll：findAll 透传")
        void shouldListAll() {
            List<Location> list = List.of(buildPersistedLocation(1L), buildPersistedLocation(2L));
            when(locationRepository.findAll()).thenReturn(list);

            List<Location> result = locationAppService.listAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("listByCity：findByCity")
        void shouldListByCity() {
            List<Location> list = List.of(buildPersistedLocation(1L));
            when(locationRepository.findByCity("杭州")).thenReturn(list);

            List<Location> result = locationAppService.listByCity("杭州");

            assertThat(result).hasSize(1);
            verify(locationRepository).findByCity("杭州");
        }

        @Test
        @DisplayName("listByCityAndType：findByCityAndType")
        void shouldListByCityAndType() {
            List<Location> list = List.of(buildPersistedLocation(1L));
            when(locationRepository.findByCityAndType("杭州", LocationType.CAFE)).thenReturn(list);

            List<Location> result = locationAppService.listByCityAndType("杭州", LocationType.CAFE);

            assertThat(result).hasSize(1);
            verify(locationRepository).findByCityAndType("杭州", LocationType.CAFE);
        }
    }

    @Nested
    @DisplayName("searchNearby 附近搜索")
    class SearchNearbyTest {

        @Test
        @DisplayName("type = null：查 findByLatBetweenAndLngBetween")
        void shouldSearchNearbyNoType() {
            List<Location> list = List.of(buildPersistedLocation(1L));
            when(locationRepository.findByLatitudeBetweenAndLongitudeBetween(
                    any(), any(), any(), any())).thenReturn(list);

            List<Location> result = locationAppService.searchNearby(LAT, LNG, 2.0, null);

            assertThat(result).hasSize(1);
            verify(locationRepository)
                    .findByLatitudeBetweenAndLongitudeBetween(any(), any(), any(), any());
            verify(locationRepository, never())
                    .findByLatitudeBetweenAndLongitudeBetweenAndType(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("type 指定：带类型查询")
        void shouldSearchNearbyWithType() {
            List<Location> list = List.of(buildPersistedLocation(1L));
            when(locationRepository.findByLatitudeBetweenAndLongitudeBetweenAndType(
                    any(), any(), any(), any(), any())).thenReturn(list);

            List<Location> result = locationAppService.searchNearby(LAT, LNG, 1.0, LocationType.PARK);

            assertThat(result).hasSize(1);
            verify(locationRepository)
                    .findByLatitudeBetweenAndLongitudeBetweenAndType(
                            any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateLocation 更新")
    class UpdateTest {

        @Test
        @DisplayName("正常更新：getById → Domain.updateLocation → save")
        void shouldUpdate() {
            Location location = buildPersistedLocation(LOC_ID);
            when(locationRepository.findById(LOC_ID)).thenReturn(Optional.of(location));
            Location updated = buildPersistedLocation(LOC_ID);
            updated.update("新名", "新地址", "新城市", LocationType.BOOKSTORE);
            when(locationRepository.save(location)).thenReturn(updated);

            Location result = locationAppService.updateLocation(
                    LOC_ID, "新名", "新地址", "新城市", LocationType.BOOKSTORE);

            assertThat(result.getName()).isEqualTo("新名");
            verify(exploreDomainService).updateLocation(
                    location, "新名", "新地址", "新城市", LocationType.BOOKSTORE);
            verify(locationRepository).save(location);
        }

        @Test
        @DisplayName("id 不存在 → getById 抛 BusinessException")
        void shouldThrowWhenUpdateNotFound() {
            when(locationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> locationAppService.updateLocation(
                    999L, "a", "b", "c", LocationType.CAFE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("地点不存在: id=999");
        }
    }
}
