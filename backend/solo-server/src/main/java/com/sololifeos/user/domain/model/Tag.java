package com.sololifeos.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 标签 Entity (DATABASE_DESIGN §6.10)。
 * <p>
 * Owner 待 ADR-0010 定稿（决策方向：Shared Kernel）。物理表已由 TASK-0004 创建，
 * 本 Entity 归 User Module 管理，ADR-0010 Accepted 后可迁移归属。
 * <p>
 * 唯一约束：(user_id, name, type) 防重复标签 (§8 uk_tag_user_name_type)。
 * 标签不可编辑、不软删除、无 updated_time（保留原始记录）。
 */
@Entity
@Table(name = "tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tag_user_name_type", columnNames = {"user_id", "name", "type"})
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TagType type = TagType.GENERAL;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    protected Tag() {
        // JPA 规范要求无参构造
    }

    public Tag(Long userId, String name, TagType type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public TagType getType() {
        return type;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}
