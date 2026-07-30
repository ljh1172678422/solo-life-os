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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户主表 Entity (DATABASE_DESIGN §6.1, Owner: User Module)。
 * <p>
 * 表名 {@code "user"} 在 PostgreSQL 中为保留字，迁移脚本已用双引号建表，
 * 因此 {@code @Table} 名称需带引号。
 * <p>
 * 软删除：{@code deleted_time} 非空表示已删除 (DATABASE_DESIGN §9)。
 * 查询自动过滤已删除记录（{@code @SQLRestriction}），删除走 UPDATE 而非物理删除。
 * <p>
 * 注意：{@code password} 字段未包含在 Entity 中 —— 登录凭证归
 * Authentication 任务 (ADR-0006 JWT)，与认证逻辑同期落地，避免在 ADR 未定时
 * 过早锁定 schema (TASK-0101 Migration Review 结论)。
 */
@Entity
@Table(name = "\"user\"")
@SQLDelete(sql = "UPDATE \"user\" SET deleted_time = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_time IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    /** 软删除时间，NULL 表示未删除。不参与常规查询（见 @SQLRestriction）。 */
    @Column(insertable = false)
    private LocalDateTime deletedTime;

    protected User() {
        // JPA 规范要求无参构造
    }

    public User(String nickname, String email, String phone) {
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.status = UserStatus.ACTIVE;
    }

    /** 业务构造：注册新用户（无邮箱/手机号场景传 null）。 */
    public static User register(String nickname, String email, String phone) {
        User user = new User();
        user.nickname = nickname;
        user.email = email;
        user.phone = phone;
        user.status = UserStatus.INACTIVE;
        return user;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void updateProfile(String nickname, String avatar, String city) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.city = city;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public UserStatus getStatus() {
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

    void setNickname(String nickname) {
        this.nickname = nickname;
    }

    void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    void setCity(String city) {
        this.city = city;
    }

    void setStatus(UserStatus status) {
        this.status = status;
    }
}
