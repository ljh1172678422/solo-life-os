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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日计划 Entity (DATABASE_DESIGN §6.3, Owner: Today Module)。
 * <p>
 * 一个用户一天一个计划，按 (user_id, date) 查询索引（DATABASE_DESIGN §8 idx_daily_plan_user_date）。
 * 业务唯一性由 Application Service 校验，数据库层不强制唯一约束（DATABASE_DESIGN §8 仅列为普通索引）。
 * 计划包含多个 {@link Activity}，通过 activity.daily_plan_id 逻辑关联（L1 决策补充，不建物理 FK）。
 * <p>
 * 软删除：{@code deleted_time} 非空表示已删除 (DATABASE_DESIGN §9)，查询自动过滤（@SQLRestriction）。
 * <p>
 * 状态流转：PLANNING → ONGOING → COMPLETED / CANCELLED。
 */
@Entity
@Table(name = "daily_plan")
@SQLDelete(sql = "UPDATE daily_plan SET deleted_time = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_time IS NULL")
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PlanStatus status = PlanStatus.PLANNING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    /** 软删除时间，NULL 表示未删除。不参与常规查询（见 @SQLRestriction）。 */
    @Column(insertable = false)
    private LocalDateTime deletedTime;

    protected DailyPlan() {
        // JPA 规范要求无参构造
    }

    public DailyPlan(Long userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
        this.status = PlanStatus.PLANNING;
    }

    /** 业务构造：创建新计划，初始状态 PLANNING。 */
    public static DailyPlan create(Long userId, LocalDate date) {
        DailyPlan plan = new DailyPlan();
        plan.userId = userId;
        plan.date = date;
        plan.status = PlanStatus.PLANNING;
        return plan;
    }

    /** 开始执行计划：PLANNING → ONGOING。 */
    public void start() {
        if (this.status != PlanStatus.PLANNING) {
            throw new IllegalStateException("Only PLANNING plan can be started, current: " + this.status);
        }
        this.status = PlanStatus.ONGOING;
    }

    /** 完成计划：ONGOING → COMPLETED。 */
    public void complete() {
        if (this.status != PlanStatus.ONGOING) {
            throw new IllegalStateException("Only ONGOING plan can be completed, current: " + this.status);
        }
        this.status = PlanStatus.COMPLETED;
    }

    /** 取消计划：PLANNING / ONGOING → CANCELLED。 */
    public void cancel() {
        if (this.status == PlanStatus.COMPLETED || this.status == PlanStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a " + this.status + " plan");
        }
        this.status = PlanStatus.CANCELLED;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public PlanStatus getStatus() {
        return status;
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

    void setUserId(Long userId) {
        this.userId = userId;
    }

    void setDate(LocalDate date) {
        this.date = date;
    }

    void setStatus(PlanStatus status) {
        this.status = status;
    }
}
