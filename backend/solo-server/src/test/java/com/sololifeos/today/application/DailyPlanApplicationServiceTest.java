package com.sololifeos.today.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.domain.service.TodayDomainService;
import com.sololifeos.today.repository.DailyPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DailyPlanApplicationService} 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖计划创建（含并发冲突兜底）/ 查询 / 状态变更用例，Mock Domain Service 与 Repository。
 */
@ExtendWith(MockitoExtension.class)
class DailyPlanApplicationServiceTest {

    @Mock
    private TodayDomainService todayDomainService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @InjectMocks
    private DailyPlanApplicationService planApplicationService;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 6);

    @Nested
    @DisplayName("createPlan 创建计划")
    class CreatePlanTest {

        @Test
        @DisplayName("正常创建：Domain Service 校验通过后持久化")
        void shouldCreateAndPersistPlan() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(todayDomainService.createPlan(USER_ID, DATE)).thenReturn(plan);
            when(dailyPlanRepository.save(plan)).thenReturn(plan);

            DailyPlan result = planApplicationService.createPlan(USER_ID, DATE);

            assertThat(result).isSameAs(plan);
            verify(dailyPlanRepository).save(plan);
        }

        @Test
        @DisplayName("并发冲突：DB uk 唯一约束触发，转 BusinessException")
        void shouldConvertDataIntegrityViolationToBusinessException() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(todayDomainService.createPlan(USER_ID, DATE)).thenReturn(plan);
            when(dailyPlanRepository.save(plan)).thenThrow(new DataIntegrityViolationException("uk_daily_plan_user_date"));

            assertThatThrownBy(() -> planApplicationService.createPlan(USER_ID, DATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("并发创建冲突");
        }

        @Test
        @DisplayName("Domain Service 抛 BusinessException：直接透传")
        void shouldPropagateBusinessExceptionFromDomain() {
            when(todayDomainService.createPlan(USER_ID, DATE))
                    .thenThrow(new BusinessException("该日期已存在计划: " + DATE));

            assertThatThrownBy(() -> planApplicationService.createPlan(USER_ID, DATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("该日期已存在计划");
            verify(dailyPlanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("查询用例")
    class QueryTest {

        @Test
        @DisplayName("getPlanById：存在返回计划")
        void shouldReturnPlanById() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(dailyPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

            DailyPlan result = planApplicationService.getPlanById(PLAN_ID);

            assertThat(result).isSameAs(plan);
        }

        @Test
        @DisplayName("getPlanById：不存在抛 BusinessException")
        void shouldThrowWhenPlanNotFound() {
            when(dailyPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> planApplicationService.getPlanById(PLAN_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划不存在");
        }

        @Test
        @DisplayName("getPlanById：id 为空抛 BusinessException")
        void shouldThrowWhenPlanIdNull() {
            assertThatThrownBy(() -> planApplicationService.getPlanById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划 ID 不可为空");
        }

        @Test
        @DisplayName("getPlanByUserAndDate：委托 Repository 返回 Optional")
        void shouldReturnOptionalForUserAndDate() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(dailyPlanRepository.findByUserIdAndDate(USER_ID, DATE)).thenReturn(Optional.of(plan));

            Optional<DailyPlan> result = planApplicationService.getPlanByUserAndDate(USER_ID, DATE);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("getPlanByUserAndDate：不存在返回 empty Optional（非异常）")
        void shouldReturnEmptyWhenNoPlan() {
            when(dailyPlanRepository.findByUserIdAndDate(USER_ID, DATE)).thenReturn(Optional.empty());

            Optional<DailyPlan> result = planApplicationService.getPlanByUserAndDate(USER_ID, DATE);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("listUserPlans：委托 Repository")
        void shouldListUserPlans() {
            when(dailyPlanRepository.findByUserIdOrderByDateDesc(USER_ID)).thenReturn(List.of());

            assertThat(planApplicationService.listUserPlans(USER_ID)).isEmpty();
        }

        @Test
        @DisplayName("listPlansByDateRange：委托 Repository")
        void shouldListByDateRange() {
            when(dailyPlanRepository.findByUserIdAndDateBetweenOrderByDateAsc(USER_ID, DATE, DATE.plusDays(7)))
                    .thenReturn(List.of());

            assertThat(planApplicationService.listPlansByDateRange(USER_ID, DATE, DATE.plusDays(7))).isEmpty();
        }

        @Test
        @DisplayName("listPlansByStatus：委托 Repository")
        void shouldListByStatus() {
            when(dailyPlanRepository.findByUserIdAndStatus(USER_ID, PlanStatus.PLANNING)).thenReturn(List.of());

            assertThat(planApplicationService.listPlansByStatus(USER_ID, PlanStatus.PLANNING)).isEmpty();
        }
    }

    @Nested
    @DisplayName("状态变更用例")
    class StateChangeTest {

        @Test
        @DisplayName("startPlan：加载 + 委托 Domain Service + 持久化")
        void shouldStartPlan() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(dailyPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
            when(dailyPlanRepository.save(plan)).thenReturn(plan);

            DailyPlan result = planApplicationService.startPlan(PLAN_ID);

            verify(todayDomainService).startPlan(plan);
            assertThat(result).isSameAs(plan);
        }

        @Test
        @DisplayName("completePlan：加载 + 委托 Domain Service + 持久化")
        void shouldCompletePlan() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(dailyPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
            when(dailyPlanRepository.save(plan)).thenReturn(plan);

            planApplicationService.completePlan(PLAN_ID);

            verify(todayDomainService).completePlan(plan);
        }

        @Test
        @DisplayName("cancelPlan：加载 + 委托 Domain Service + 持久化")
        void shouldCancelPlan() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            when(dailyPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
            when(dailyPlanRepository.save(plan)).thenReturn(plan);

            planApplicationService.cancelPlan(PLAN_ID);

            verify(todayDomainService).cancelPlan(plan);
        }
    }
}
