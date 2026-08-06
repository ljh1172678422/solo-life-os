package com.sololifeos.today.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DailyPlan} Entity 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖工厂构造校验与状态机流转（PLANNING → ONGOING → COMPLETED / CANCELLED），
 * 不依赖数据库。
 */
class DailyPlanTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 6);

    @Nested
    @DisplayName("create 工厂构造")
    class CreateTest {

        @Test
        @DisplayName("正常创建：初始状态 PLANNING")
        void shouldCreatePlanningPlan() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);

            assertThat(plan.getUserId()).isEqualTo(USER_ID);
            assertThat(plan.getDate()).isEqualTo(DATE);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.PLANNING);
            assertThat(plan.getId()).isNull();
        }

        @Test
        @DisplayName("userId 为空：抛 IllegalArgumentException")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> DailyPlan.create(null, DATE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userId 不可为空");
        }

        @Test
        @DisplayName("date 为空：抛 IllegalArgumentException")
        void shouldThrowWhenDateNull() {
            assertThatThrownBy(() -> DailyPlan.create(USER_ID, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("计划日期不可为空");
        }
    }

    @Nested
    @DisplayName("状态机流转")
    class StateMachineTest {

        @Test
        @DisplayName("start：PLANNING → ONGOING")
        void shouldStartFromPlanning() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.ONGOING);
            assertThat(plan.isClosed()).isFalse();
        }

        @Test
        @DisplayName("complete：ONGOING → COMPLETED")
        void shouldCompleteFromOngoing() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();
            plan.complete();

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.COMPLETED);
            assertThat(plan.isClosed()).isTrue();
        }

        @Test
        @DisplayName("cancel：PLANNING → CANCELLED")
        void shouldCancelFromPlanning() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.cancel();

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.CANCELLED);
            assertThat(plan.isClosed()).isTrue();
        }

        @Test
        @DisplayName("cancel：ONGOING → CANCELLED")
        void shouldCancelFromOngoing() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();
            plan.cancel();

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.CANCELLED);
            assertThat(plan.isClosed()).isTrue();
        }

        @Test
        @DisplayName("start 已 ONGOING：抛 IllegalStateException")
        void shouldThrowWhenStartFromOngoing() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();

            assertThatThrownBy(plan::start)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PLANNING");
        }

        @Test
        @DisplayName("complete 未 ONGOING：抛 IllegalStateException")
        void shouldThrowWhenCompleteFromPlanning() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);

            assertThatThrownBy(plan::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ONGOING");
        }

        @Test
        @DisplayName("cancel 已 COMPLETED：抛 IllegalStateException")
        void shouldThrowWhenCancelFromCompleted() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();
            plan.complete();

            assertThatThrownBy(plan::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("cancel 已 CANCELLED：抛 IllegalStateException")
        void shouldThrowWhenCancelFromCancelled() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.cancel();

            assertThatThrownBy(plan::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("完整生命周期：PLANNING → ONGOING → COMPLETED")
        void shouldGoThroughFullLifecycle() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.PLANNING);

            plan.start();
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.ONGOING);

            plan.complete();
            assertThat(plan.getStatus()).isEqualTo(PlanStatus.COMPLETED);
            assertThat(plan.isClosed()).isTrue();
        }
    }

    @Nested
    @DisplayName("isClosed 关闭判定")
    class IsClosedTest {

        @Test
        @DisplayName("PLANNING：未关闭")
        void planningNotClosed() {
            assertThat(DailyPlan.create(USER_ID, DATE).isClosed()).isFalse();
        }

        @Test
        @DisplayName("ONGOING：未关闭")
        void ongoingNotClosed() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();
            assertThat(plan.isClosed()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED：已关闭")
        void completedClosed() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.start();
            plan.complete();
            assertThat(plan.isClosed()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED：已关闭")
        void cancelledClosed() {
            DailyPlan plan = DailyPlan.create(USER_ID, DATE);
            plan.cancel();
            assertThat(plan.isClosed()).isTrue();
        }
    }
}
