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

    /** 软删除时间，NULL 表示未删除。不参与常规查询（见 @SQLRestriction）。 */
    @Column(insertable = false)
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

    /** 业务构造：在计划下创建活动。 */
    public static Activity create(Long dailyPlanId, String title, ActivityType type, LocalDateTime startTime) {
        Activity activity = new Activity();
        activity.dailyPlanId = dailyPlanId;
        activity.title = title;
        activity.type = type;
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

    /** 修改活动信息。 */
    public void update(String title, ActivityType type, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
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
