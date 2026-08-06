package com.sololifeos.today.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.domain.service.TodayDomainService;
import com.sololifeos.today.repository.ActivityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ActivityApplicationService} 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖活动添加 / 查询 / 修改 / 结束 / 定位 / 归属校验用例。
 * Mock TodayDomainService / ActivityRepository / DailyPlanApplicationService。
 */
@ExtendWith(MockitoExtension.class)
class ActivityApplicationServiceTest {

    @Mock
    private TodayDomainService todayDomainService;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DailyPlanApplicationService planApplicationService;

    @InjectMocks
    private ActivityApplicationService activityApplicationService;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 100L;
    private static final Long ACTIVITY_ID = 200L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 6, 9, 0);

    @Nested
    @DisplayName("addActivity 添加活动")
    class AddActivityTest {

        @Test
        @DisplayName("正常添加：加载计划 + 委托 Domain Service + 持久化")
        void shouldAddActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "晨跑", ActivityType.SPORT, START);
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(todayDomainService.addActivityToPlan(plan, "晨跑", ActivityType.SPORT, START)).thenReturn(activity);
            when(activityRepository.save(activity)).thenReturn(activity);

            Activity result = activityApplicationService.addActivity(PLAN_ID, "晨跑", ActivityType.SPORT, START);

            assertThat(result).isSameAs(activity);
            verify(activityRepository).save(activity);
        }

        @Test
        @DisplayName("DB CHECK 约束冲突：转 BusinessException")
        void shouldConvertDataIntegrityViolationToBusinessException() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "活动", ActivityType.WORK, START);
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(todayDomainService.addActivityToPlan(plan, "活动", ActivityType.WORK, START)).thenReturn(activity);
            when(activityRepository.save(activity)).thenThrow(new DataIntegrityViolationException("chk_activity_type"));

            assertThatThrownBy(() -> activityApplicationService.addActivity(PLAN_ID, "活动", ActivityType.WORK, START))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动数据不合法");
        }
    }

    @Nested
    @DisplayName("查询用例")
    class QueryTest {

        @Test
        @DisplayName("getActivity：存在返回活动")
        void shouldReturnActivity() {
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

            Activity result = activityApplicationService.getActivity(ACTIVITY_ID);

            assertThat(result).isSameAs(activity);
        }

        @Test
        @DisplayName("getActivity：不存在抛 BusinessException")
        void shouldThrowWhenActivityNotFound() {
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> activityApplicationService.getActivity(ACTIVITY_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动不存在");
        }

        @Test
        @DisplayName("getActivity：id 为空抛 BusinessException")
        void shouldThrowWhenActivityIdNull() {
            assertThatThrownBy(() -> activityApplicationService.getActivity(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动 ID 不可为空");
        }

        @Test
        @DisplayName("listActivitiesByPlan：委托 Repository")
        void shouldListByPlan() {
            when(activityRepository.findByDailyPlanIdOrderByStartTimeAsc(PLAN_ID)).thenReturn(List.of());

            assertThat(activityApplicationService.listActivitiesByPlan(PLAN_ID)).isEmpty();
        }

        @Test
        @DisplayName("listActivitiesByPlans：批量查委托 Repository")
        void shouldListByPlans() {
            when(activityRepository.findByDailyPlanIdInOrderByStartTimeAsc(List.of(1L, 2L))).thenReturn(List.of());

            assertThat(activityApplicationService.listActivitiesByPlans(List.of(1L, 2L))).isEmpty();
        }

        @Test
        @DisplayName("listActivitiesByLocation：委托 Repository")
        void shouldListByLocation() {
            when(activityRepository.findByLocationIdOrderByStartTimeDesc(555L)).thenReturn(List.of());

            assertThat(activityApplicationService.listActivitiesByLocation(555L)).isEmpty();
        }

        @Test
        @DisplayName("listActivitiesByTimeRange：委托 Repository")
        void shouldListByTimeRange() {
            when(activityRepository.findByStartTimeBetweenOrderByStartTimeAsc(START, START.plusDays(1)))
                    .thenReturn(List.of());

            assertThat(activityApplicationService.listActivitiesByTimeRange(START, START.plusDays(1))).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateActivity 修改活动")
    class UpdateActivityTest {

        @Test
        @DisplayName("正常修改：归属校验通过后委托 Domain Service")
        void shouldUpdateActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "旧", ActivityType.WORK, START);
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
            when(activityRepository.save(activity)).thenReturn(activity);

            activityApplicationService.updateActivity(PLAN_ID, ACTIVITY_ID, "新", ActivityType.STUDY, START, null);

            verify(todayDomainService).updateActivity(plan, activity, "新", ActivityType.STUDY, START, null);
        }

        @Test
        @DisplayName("活动不属于该计划：抛 BusinessException")
        void shouldThrowWhenActivityNotBelongToPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity otherPlanActivity = Activity.create(999L, "活动", ActivityType.WORK, START);
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(otherPlanActivity));

            assertThatThrownBy(() -> activityApplicationService.updateActivity(
                    PLAN_ID, ACTIVITY_ID, "新", ActivityType.WORK, START, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动不属于该计划");
            verify(todayDomainService, never()).updateActivity(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("DB 约束冲突：转 BusinessException")
        void shouldConvertDataIntegrityViolation() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "旧", ActivityType.WORK, START);
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
            when(activityRepository.save(activity)).thenThrow(new DataIntegrityViolationException("chk_activity_type"));

            assertThatThrownBy(() -> activityApplicationService.updateActivity(
                    PLAN_ID, ACTIVITY_ID, "新", ActivityType.WORK, START, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动数据不合法");
        }
    }

    @Nested
    @DisplayName("endActivity / locateActivity")
    class ActivityMutationTest {

        @Test
        @DisplayName("endActivity：加载活动 + 通过 planId 加载计划 + 委托 Domain Service")
        void shouldEndActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(activityRepository.save(activity)).thenReturn(activity);

            activityApplicationService.endActivity(ACTIVITY_ID, START.plusHours(1));

            verify(todayDomainService).endActivity(plan, activity, START.plusHours(1));
        }

        @Test
        @DisplayName("locateActivity：加载活动 + 通过 planId 加载计划 + 委托 Domain Service")
        void shouldLocateActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "探索", ActivityType.EXPLORE, START);
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
            when(planApplicationService.getPlanById(PLAN_ID)).thenReturn(plan);
            when(activityRepository.save(activity)).thenReturn(activity);

            activityApplicationService.locateActivity(ACTIVITY_ID, 777L);

            verify(todayDomainService).locateActivity(plan, activity, 777L);
        }

        @Test
        @DisplayName("endActivity：活动不存在抛 BusinessException")
        void shouldThrowWhenEndActivityNotFound() {
            when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> activityApplicationService.endActivity(ACTIVITY_ID, START.plusHours(1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("活动不存在");
        }
    }

    /** 构造已持久化（含 id）的计划。 */
    private static DailyPlan persistedPlan(Long id, PlanStatus initialStatus) {
        DailyPlan plan = DailyPlan.create(USER_ID, java.time.LocalDate.of(2026, 8, 6));
        try {
            Field idField = DailyPlan.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(plan, id);
        } catch (Exception e) {
            throw new IllegalStateException("设置测试 id 失败", e);
        }
        return plan;
    }
}
