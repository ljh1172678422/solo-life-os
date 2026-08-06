package com.sololifeos.today.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.repository.DailyPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TodayDomainService} 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖计划创建 / 活动添加 / 状态变更 / 活动修改的业务规则校验。
 * Mock DailyPlanRepository，不依赖数据库。
 */
@ExtendWith(MockitoExtension.class)
class TodayDomainServiceTest {

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @InjectMocks
    private TodayDomainService todayDomainService;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 6);
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 6, 9, 0);

    @Nested
    @DisplayName("createPlan 创建计划")
    class CreatePlanTest {

        @Test
        @DisplayName("正常创建：当日无计划，返回 PLANNING 状态计划")
        void shouldCreatePlanWhenNoExisting() {
            when(dailyPlanRepository.existsByUserIdAndDate(USER_ID, DATE)).thenReturn(false);

            DailyPlan plan = todayDomainService.createPlan(USER_ID, DATE);

            assertThat(plan.getUserId()).isEqualTo(USER_ID);
            assertThat(plan.getDate()).isEqualTo(DATE);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.PLANNING);
        }

        @Test
        @DisplayName("当日已存在计划：抛 BusinessException")
        void shouldThrowWhenPlanAlreadyExists() {
            when(dailyPlanRepository.existsByUserIdAndDate(USER_ID, DATE)).thenReturn(true);

            assertThatThrownBy(() -> todayDomainService.createPlan(USER_ID, DATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("该日期已存在计划");
        }

        @Test
        @DisplayName("userId 为空：抛 BusinessException")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> todayDomainService.createPlan(null, DATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户 ID 不可为空");
            verify(dailyPlanRepository, never()).existsByUserIdAndDate(null, DATE);
        }

        @Test
        @DisplayName("date 为空：抛 BusinessException")
        void shouldThrowWhenDateNull() {
            assertThatThrownBy(() -> todayDomainService.createPlan(USER_ID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划日期不可为空");
            verify(dailyPlanRepository, never()).existsByUserIdAndDate(USER_ID, null);
        }
    }

    @Nested
    @DisplayName("addActivityToPlan 添加活动")
    class AddActivityTest {

        @Test
        @DisplayName("正常添加：返回关联 planId 的活动")
        void shouldAddActivityToOpenPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);

            Activity activity = todayDomainService.addActivityToPlan(plan, "晨跑", ActivityType.SPORT, START);

            assertThat(activity.getDailyPlanId()).isEqualTo(PLAN_ID);
            assertThat(activity.getTitle()).isEqualTo("晨跑");
            assertThat(activity.getType()).isEqualTo(ActivityType.SPORT);
        }

        @Test
        @DisplayName("type 为空：回退 OTHER")
        void shouldFallbackTypeToOther() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);

            Activity activity = todayDomainService.addActivityToPlan(plan, "杂事", null, START);

            assertThat(activity.getType()).isEqualTo(ActivityType.OTHER);
        }

        @Test
        @DisplayName("plan 为空：抛 BusinessException")
        void shouldThrowWhenPlanNull() {
            assertThatThrownBy(() -> todayDomainService.addActivityToPlan(null, "活动", ActivityType.WORK, START))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("所属计划不可为空");
        }

        @Test
        @DisplayName("plan 未持久化（id 为空）：抛 BusinessException")
        void shouldThrowWhenPlanNotPersisted() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE); // id == null

            assertThatThrownBy(() -> todayDomainService.addActivityToPlan(plan, "活动", ActivityType.WORK, START))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("未持久化");
        }

        @Test
        @DisplayName("plan 已 COMPLETED：抛 BusinessException")
        void shouldThrowWhenPlanCompleted() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.start();
            plan.complete();

            assertThatThrownBy(() -> todayDomainService.addActivityToPlan(plan, "活动", ActivityType.WORK, START))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("plan 已 CANCELLED：抛 BusinessException")
        void shouldThrowWhenPlanCancelled() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.cancel();

            assertThatThrownBy(() -> todayDomainService.addActivityToPlan(plan, "活动", ActivityType.WORK, START))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CANCELLED");
        }
    }

    @Nested
    @DisplayName("startPlan / completePlan / cancelPlan 状态变更")
    class StateChangeTest {

        @Test
        @DisplayName("startPlan：PLANNING → ONGOING")
        void shouldStartPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            todayDomainService.startPlan(plan);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.ONGOING);
        }

        @Test
        @DisplayName("completePlan：ONGOING → COMPLETED")
        void shouldCompletePlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.start();
            todayDomainService.completePlan(plan);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.COMPLETED);
        }

        @Test
        @DisplayName("cancelPlan：PLANNING → CANCELLED")
        void shouldCancelPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            todayDomainService.cancelPlan(plan);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.CANCELLED);
        }

        @Test
        @DisplayName("startPlan 传 null：抛 BusinessException")
        void shouldThrowWhenStartNullPlan() {
            assertThatThrownBy(() -> todayDomainService.startPlan(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划不可为空");
        }

        @Test
        @DisplayName("completePlan 传 null：抛 BusinessException")
        void shouldThrowWhenCompleteNullPlan() {
            assertThatThrownBy(() -> todayDomainService.completePlan(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划不可为空");
        }

        @Test
        @DisplayName("cancelPlan 传 null：抛 BusinessException")
        void shouldThrowWhenCancelNullPlan() {
            assertThatThrownBy(() -> todayDomainService.cancelPlan(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("计划不可为空");
        }
    }

    @Nested
    @DisplayName("updateActivity 修改活动")
    class UpdateActivityTest {

        @Test
        @DisplayName("正常修改：计划未关闭")
        void shouldUpdateActivityInOpenPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "旧", ActivityType.WORK, START);

            todayDomainService.updateActivity(plan, activity, "新", ActivityType.STUDY, START.plusHours(1), null);

            assertThat(activity.getTitle()).isEqualTo("新");
            assertThat(activity.getType()).isEqualTo(ActivityType.STUDY);
        }

        @Test
        @DisplayName("plan 已关闭：抛 BusinessException")
        void shouldThrowWhenPlanClosed() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.start();
            plan.complete();
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            assertThatThrownBy(() -> todayDomainService.updateActivity(plan, activity, "新", ActivityType.WORK, START, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("plan 或 activity 为空：抛 BusinessException")
        void shouldThrowWhenArgsNull() {
            assertThatThrownBy(() -> todayDomainService.updateActivity(null, null, "新", ActivityType.WORK, START, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不可为空");
        }
    }

    @Nested
    @DisplayName("endActivity / locateActivity")
    class ActivityMutationTest {

        @Test
        @DisplayName("endActivity：设置结束时间")
        void shouldEndActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);

            todayDomainService.endActivity(plan, activity, START.plusHours(1));

            assertThat(activity.getEndTime()).isEqualTo(START.plusHours(1));
        }

        @Test
        @DisplayName("endActivity：plan 已关闭抛 BusinessException")
        void shouldThrowWhenEndInClosedPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.cancel();
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);

            assertThatThrownBy(() -> todayDomainService.endActivity(plan, activity, START.plusHours(1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        @DisplayName("locateActivity：绑定地点")
        void shouldLocateActivity() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            Activity activity = Activity.create(PLAN_ID, "探索", ActivityType.EXPLORE, START);

            todayDomainService.locateActivity(plan, activity, 777L);

            assertThat(activity.getLocationId()).isEqualTo(777L);
        }

        @Test
        @DisplayName("locateActivity：plan 已关闭抛 BusinessException")
        void shouldThrowWhenLocateInClosedPlan() {
            DailyPlan plan = persistedPlan(PLAN_ID, PlanStatus.PLANNING);
            plan.start();
            plan.complete();
            Activity activity = Activity.create(PLAN_ID, "探索", ActivityType.EXPLORE, START);

            assertThatThrownBy(() -> todayDomainService.locateActivity(plan, activity, 777L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("endActivity / locateActivity 传 null：抛 BusinessException")
        void shouldThrowWhenActivityArgsNull() {
            assertThatThrownBy(() -> todayDomainService.endActivity(null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不可为空");
            assertThatThrownBy(() -> todayDomainService.locateActivity(null, null, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不可为空");
        }
    }

    /** 构造已持久化（含 id）的计划，模拟 DB 已分配 id。 */
    private static DailyPlan persistedPlan(Long id, PlanStatus initialStatus) {
        DailyPlan plan = DailyPlan.create(USER_ID, DATE);
        try {
            Field idField = DailyPlan.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(plan, id);
            // initialStatus 默认 PLANNING，无需额外设置
        } catch (Exception e) {
            throw new IllegalStateException("设置测试 id 失败", e);
        }
        return plan;
    }
}
