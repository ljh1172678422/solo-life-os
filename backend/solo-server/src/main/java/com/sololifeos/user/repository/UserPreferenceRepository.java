package com.sololifeos.user.repository;

import com.sololifeos.user.domain.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户偏好仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * 查询方法对齐 DATABASE_DESIGN §8 uk_user_preference_user_id 唯一索引（一用户一偏好）。
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /** 按 user_id 查询偏好（§8 uk_user_preference_user_id 唯一索引）。 */
    Optional<UserPreference> findByUserId(Long userId);

    /** 判断某用户是否已有偏好记录。 */
    boolean existsByUserId(Long userId);
}
