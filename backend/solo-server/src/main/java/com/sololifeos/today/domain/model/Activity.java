package com.sololifeos.today.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 活动 Entity (DATABASE_DESIGN §6.4, Owner: Today Module)。
 * <p>
 * 归属到 {@link DailyPlan}，通过 {@code daily_plan_id} 逻辑关联（L1 决策补充字段，
 * DATABASE_DESIGN §6.4 原未含此字段，Migration V20260730_003 建表时已加，AI_CHANGELOG 记录）。
 * 不建物理 FK（DATABASE_DESIGN §9）。
 * <p>
 * {@code location_id} 逻辑关联 location.id（Explore Module, Sprint 3 创建，本 Sprint 可空）。
 * <p>
 * 软删除：{@code deleted_time} 非空表示已删除 (DATABASE_DESIGN §9)，查询自动过滤（@SQLRestriction）。
 * <p>
 * 时间维护策略（PR #19 Review 改进明确）：
 * {@code created_time} / {@code updated_time} 由 Hibernate {@code @CreationTimestamp} /
 * {@code @UpdateTimestamp} 在应用层自动维护，不使用 DB Trigger。
 * <p>
 * 枚举合法性：{@code type} 由 DB 层 CHECK 约束 {@code chk_activity_type} 兜底
 * （Migration V20260730_004，对齐 DATABASE_DESIGN §7 ACTIVITY_TYPE），
 * Application 层枚举校验为第一道防线。
 */
@Entity
@Table(name = "activity")
@SQLDelete(sql = "UPDATE activity SET deleted_time = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_time IS NULL")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_plan_id", nullable = false)
    private Long dailyPlanId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActivityType type = ActivityType.OTHER;

    /** 逻辑关联 location.id（Explore Module, Sprint 3），本 Sprint 可空。 */
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    /** 软删除时间，NULL 表示未删除。不参与常规查询（见 @SQLRestriction）。由 DB 维护（@SQLDelete）。 */
    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedTime;

    protected Activity() {
        // JPA 规范要求无参构造
    }

    public Activity(Long dailyPlanId, String title, ActivityType type, LocalDateTime startTime) {
        this.dailyPlanId = dailyPlanId;
        this.title = title;
        this.type = type;
        this.startTime = startTime;
    }

    /**
     * 业务构造：在计划下创建活动。
     * <p>
     * 参数合法性由本方法强制保证，确保 Entity 始终处于合法状态（PR #20 Review 反馈）：
     * dailyPlanId / title / startTime 非空，type 为空时回退 OTHER。
     *
     * @param dailyPlanId 所属计划 ID（必须已持久化，由 Domain Service 校验）
     * @param title       活动标题（非空，长度 1-200）
     * @param type        活动类型（可空，null → OTHER）
     * @param startTime   开始时间（非空）
     * @return 合法状态的 Activity
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static Activity create(Long dailyPlanId, String title, ActivityType type, LocalDateTime startTime) {
        if (dailyPlanId == null) {
            throw new IllegalArgumentException("dailyPlanId 不可为空");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("活动标题不可为空");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("活动标题长度不可超过 200");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("活动开始时间不可为空");
        }
        Activity activity = new Activity();
        activity.dailyPlanId = dailyPlanId;
        activity.title = title;
        activity.type = type != null ? type : ActivityType.OTHER;
        activity.startTime = startTime;
        return activity;
    }

    /** 设置结束时间。 */
    public void end(LocalDateTime endTime) {
        if (endTime != null && endTime.isBefore(this.startTime)) {
            throw new IllegalStateException("end_time cannot be earlier than start_time");
        }
        this.endTime = endTime;
    }

    /** 绑定地点（Explore Module, Sprint 3 后可用）。 */
    public void locate(Long locationId) {
        this.locationId = locationId;
    }

    /**
     * 修改活动信息。参数合法性由本方法强制保证，确保 Entity 始终处于合法状态
     * （PR #20 Review 反馈：原实现允许 null 覆盖 NOT NULL 字段）。
     *
     * @param title     新标题（非空，长度 1-200）
     * @param type      新类型（非空，由 Domain Service 保证；防御性兜底）
     * @param startTime 新开始时间（非空）
     * @param endTime   新结束时间（可空，非空时需晚于 startTime）
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public void update(String title, ActivityType type, LocalDateTime startTime, LocalDateTime endTime) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("活动标题不可为空");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("活动标题长度不可超过 200");
        }
        if (type == null) {
            throw new IllegalArgumentException("活动类型不可为空");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("活动开始时间不可为空");
        }
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalStateException("end_time cannot be earlier than start_time");
        }
        this.title = title;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Long getDailyPlanId() {
        return dailyPlanId;
    }

    public String getTitle() {
        return title;
    }

    public ActivityType getType() {
        return type;
    }

    public Long getLocationId() {
        return locationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public LocalDateTime getDeletedTime() {
        return deletedTime;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setDailyPlanId(Long dailyPlanId) {
        this.dailyPlanId = dailyPlanId;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setType(ActivityType type) {
        this.type = type;
    }

    void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
