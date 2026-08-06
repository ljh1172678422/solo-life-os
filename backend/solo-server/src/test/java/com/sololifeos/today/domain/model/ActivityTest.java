package com.sololifeos.today.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Activity} Entity 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖工厂构造校验、结束时间、地点绑定、修改信息等行为，不依赖数据库。
 */
class ActivityTest {

    private static final Long PLAN_ID = 100L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 6, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 8, 6, 10, 30);

    @Nested
    @DisplayName("create 工厂构造")
    class CreateTest {

        @Test
        @DisplayName("正常创建：type 非空保留")
        void shouldCreateWithGivenType() {
            Activity activity = Activity.create(PLAN_ID, "晨跑", ActivityType.SPORT, START);

            assertThat(activity.getDailyPlanId()).isEqualTo(PLAN_ID);
            assertThat(activity.getTitle()).isEqualTo("晨跑");
            assertThat(activity.getType()).isEqualTo(ActivityType.SPORT);
            assertThat(activity.getStartTime()).isEqualTo(START);
            assertThat(activity.getEndTime()).isNull();
            assertThat(activity.getLocationId()).isNull();
        }

        @Test
        @DisplayName("type 为空：回退 OTHER")
        void shouldFallbackToOtherWhenTypeNull() {
            Activity activity = Activity.create(PLAN_ID, "杂事", null, START);

            assertThat(activity.getType()).isEqualTo(ActivityType.OTHER);
        }

        @Test
        @DisplayName("dailyPlanId 为空：抛 IllegalArgumentException")
        void shouldThrowWhenPlanIdNull() {
            assertThatThrownBy(() -> Activity.create(null, "活动", ActivityType.WORK, START))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dailyPlanId 不可为空");
        }

        @Test
        @DisplayName("title 为空：抛 IllegalArgumentException")
        void shouldThrowWhenTitleBlank() {
            assertThatThrownBy(() -> Activity.create(PLAN_ID, "", ActivityType.WORK, START))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("活动标题不可为空");
        }

        @Test
        @DisplayName("title 超长（>200）：抛 IllegalArgumentException")
        void shouldThrowWhenTitleTooLong() {
            String longTitle = "a".repeat(201);
            assertThatThrownBy(() -> Activity.create(PLAN_ID, longTitle, ActivityType.WORK, START))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200");
        }

        @Test
        @DisplayName("title 恰好 200 字符：合法")
        void shouldAllowTitleAtMaxLength() {
            String title = "b".repeat(200);
            Activity activity = Activity.create(PLAN_ID, title, ActivityType.WORK, START);
            assertThat(activity.getTitle()).hasSize(200);
        }

        @Test
        @DisplayName("startTime 为空：抛 IllegalArgumentException")
        void shouldThrowWhenStartTimeNull() {
            assertThatThrownBy(() -> Activity.create(PLAN_ID, "活动", ActivityType.WORK, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("活动开始时间不可为空");
        }
    }

    @Nested
    @DisplayName("end 设置结束时间")
    class EndTest {

        @Test
        @DisplayName("设置合法结束时间")
        void shouldSetEndTime() {
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);
            activity.end(END);

            assertThat(activity.getEndTime()).isEqualTo(END);
        }

        @Test
        @DisplayName("endTime 为 null：清除结束时间")
        void shouldClearEndTimeWhenNull() {
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);
            activity.end(END);
            activity.end(null);

            assertThat(activity.getEndTime()).isNull();
        }

        @Test
        @DisplayName("endTime 早于 startTime：抛 IllegalStateException")
        void shouldThrowWhenEndBeforeStart() {
            Activity activity = Activity.create(PLAN_ID, "会议", ActivityType.WORK, START);
            LocalDateTime earlier = START.minusMinutes(1);

            assertThatThrownBy(() -> activity.end(earlier))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("end_time cannot be earlier than start_time");
        }
    }

    @Nested
    @DisplayName("locate 绑定地点")
    class LocateTest {

        @Test
        @DisplayName("绑定地点 ID")
        void shouldSetLocationId() {
            Activity activity = Activity.create(PLAN_ID, "探索咖啡馆", ActivityType.EXPLORE, START);
            activity.locate(555L);

            assertThat(activity.getLocationId()).isEqualTo(555L);
        }

        @Test
        @DisplayName("清除地点（传 null）")
        void shouldClearLocationId() {
            Activity activity = Activity.create(PLAN_ID, "探索咖啡馆", ActivityType.EXPLORE, START);
            activity.locate(555L);
            activity.locate(null);

            assertThat(activity.getLocationId()).isNull();
        }
    }

    @Nested
    @DisplayName("update 修改活动信息")
    class UpdateTest {

        @Test
        @DisplayName("整体替换合法字段")
        void shouldReplaceAllFields() {
            Activity activity = Activity.create(PLAN_ID, "旧标题", ActivityType.OTHER, START);
            LocalDateTime newStart = START.plusHours(2);

            activity.update("新标题", ActivityType.STUDY, newStart, END);

            assertThat(activity.getTitle()).isEqualTo("新标题");
            assertThat(activity.getType()).isEqualTo(ActivityType.STUDY);
            assertThat(activity.getStartTime()).isEqualTo(newStart);
            assertThat(activity.getEndTime()).isEqualTo(END);
        }

        @Test
        @DisplayName("title 为空：抛 IllegalArgumentException")
        void shouldThrowWhenTitleBlank() {
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            assertThatThrownBy(() -> activity.update("  ", ActivityType.WORK, START, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("活动标题不可为空");
        }

        @Test
        @DisplayName("type 为空：抛 IllegalArgumentException")
        void shouldThrowWhenTypeNull() {
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            assertThatThrownBy(() -> activity.update("标题", null, START, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("活动类型不可为空");
        }

        @Test
        @DisplayName("startTime 为空：抛 IllegalArgumentException")
        void shouldThrowWhenStartTimeNull() {
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            assertThatThrownBy(() -> activity.update("标题", ActivityType.WORK, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("活动开始时间不可为空");
        }

        @Test
        @DisplayName("endTime 早于新 startTime：抛 IllegalStateException")
        void shouldThrowWhenEndBeforeNewStart() {
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            assertThatThrownBy(() -> activity.update("标题", ActivityType.WORK, START, START.minusSeconds(1)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("end_time cannot be earlier than start_time");
        }

        @Test
        @DisplayName("endTime 为 null 合法（未结束活动）")
        void shouldAllowNullEndTime() {
            Activity activity = Activity.create(PLAN_ID, "标题", ActivityType.WORK, START);

            activity.update("新标题", ActivityType.WORK, START, null);

            assertThat(activity.getEndTime()).isNull();
        }
    }
}
