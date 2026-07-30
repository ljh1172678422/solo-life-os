package com.sololifeos.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户偏好 Entity (DATABASE_DESIGN §6.2, Owner: User Module)。
 * <p>
 * 一用户一偏好（uk_user_preference_user_id）。与 user 为逻辑关联，不建物理 FK
 * (DATABASE_DESIGN §9)。
 */
@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** 兴趣标签，逗号分隔。 */
    @Column(length = 500)
    private String interest;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private BudgetLevel budget = BudgetLevel.MEDIUM;

    /** 生活方式描述。 */
    @Column(length = 500)
    private String lifestyle;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    protected UserPreference() {
        // JPA 规范要求无参构造
    }

    /** 业务构造：为新用户创建默认偏好。 */
    public static UserPreference defaultFor(Long userId) {
        UserPreference pref = new UserPreference();
        pref.userId = userId;
        pref.budget = BudgetLevel.MEDIUM;
        return pref;
    }

    public void update(String interest, BudgetLevel budget, String lifestyle) {
        this.interest = interest;
        this.budget = budget;
        this.lifestyle = lifestyle;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getInterest() {
        return interest;
    }

    public BudgetLevel getBudget() {
        return budget;
    }

    public String getLifestyle() {
        return lifestyle;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setUserId(Long userId) {
        this.userId = userId;
    }
}
