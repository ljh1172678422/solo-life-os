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

import java.time.LocalDateTime;

/**
 * 收藏 Entity (DATABASE_DESIGN §6.7, Owner: User Module)。
 * <p>
 * Favorite 表 Owner 为 User Module（§6.7），故 Entity 定义在 user 包下。
 * Explore Module 经 Domain API 调用（ARCHITECTURE §22）。
 * <p>
 * 收藏为硬删除（取消收藏即物理删除），无 deleted_time 字段。
 * 唯一约束：(user_id, target_type, target_id) 防重复收藏（§8 uk_favorite_user_target）。
 */
@Entity
@Table(name = "favorite")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FavoriteTarget targetType;

    @Column(nullable = false)
    private Long targetId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    protected Favorite() {
        // JPA 规范要求无参构造
    }

    /**
     * 业务构造：创建新收藏。
     *
     * @param userId     用户 ID（非空）
     * @param targetType 收藏目标类型（非空）
     * @param targetId   收藏目标 ID（非空）
     * @return 合法状态的 Favorite
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public static Favorite create(Long userId, FavoriteTarget targetType, Long targetId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户 ID 不可为空");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("收藏目标类型不可为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("收藏目标 ID 不可为空");
        }
        Favorite favorite = new Favorite();
        favorite.userId = userId;
        favorite.targetType = targetType;
        favorite.targetId = targetId;
        return favorite;
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public FavoriteTarget getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setUserId(Long userId) {
        this.userId = userId;
    }

    void setTargetType(FavoriteTarget targetType) {
        this.targetType = targetType;
    }

    void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
