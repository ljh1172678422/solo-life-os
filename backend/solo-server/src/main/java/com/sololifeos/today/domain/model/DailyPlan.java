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
 * 一个用户一天一个计划，按 (user_id, date) 唯一索引约束（PR #19 Review 改进，
 * Migration V20260730_004 升级 idx → uk_daily_plan_user_date，软删除记录不受约束）。
 * 业务层 Application Service 做唯一性校验作为第一道防线，DB 唯一索引作为兜底。
 * 计划包含多个 {@link Activity}，通过 activity.daily_plan_id 逻辑关联（L1 决策补充，不建物理 FK）。
 * <p>
 * 软删除：{@code deleted_time} 非空表示已删除 (DATABASE_DESIGN §9)，查询自动过滤（@SQLRestriction）。
 * <p>
 * 时间维护策略（PR #19 Review 改进明确）：
 * {@code created_time} / {@code updated_time} 由 Hibernate {@code @CreationTimestamp} /
 * {@code @UpdateTimestamp} 在应用层自动维护，不使用 DB Trigger。
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

    /** 软删除时间，NULL 表示未删除。不参与常规查询（见 @SQLRestriction）。由 DB 维护（@SQLDelete）。 */
    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedTime;

    protected DailyPlan() {
        // JPA 规范要求无参构造
    }

    public DailyPlan(Long userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
        this.status = PlanStatus.PLANNING;
    }

    /**
     * 业务构造：创建新计划，初始状态 PLANNING。
     * <p>
     * 参数合法性由本方法强制保证，确保 Entity 始终处于合法状态（PR #20 Review 反馈，
     * 与 Activity.create 对齐）：userId / date 非空。
     *
     * @param userId 用户 ID（非空）
     * @param date   计划日期（非空）
     * @return 合法状态的 DailyPlan
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static DailyPlan create(Long userId, LocalDate date) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不可为空");
        }
        if (date == null) {
            throw new IllegalArgumentException("计划日期不可为空");
        }
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

    /**
     * 取消计划：PLANNING / ONGOING → CANCELLED。
     * <p>
     * PR #22 Review 改进：收敛使用 {@link #isClosed()}，消除重复状态判断，
     * 保证「已关闭计划」语义单一来源。
     */
    public void cancel() {
        if (isClosed()) {
            throw new IllegalStateException("Cannot cancel a " + this.status + " plan");
        }
        this.status = PlanStatus.CANCELLED;
    }

    /**
     * 计划是否已关闭（不可再添加 / 修改活动 / 取消）。
     * <p>
     * PR #20 Review 反馈：抽取重复状态判断，减少 COMPLETED / CANCELLED 散落。
     * PR #22 Review 改进：cancel() 也收敛到此方法，「已关闭」语义单一来源。
     *
     * @return status == COMPLETED || status == CANCELLED
     */
    public boolean isClosed() {
        return this.status == PlanStatus.COMPLETED || this.status == PlanStatus.CANCELLED;
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
