package com.sololifeos.user.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.BudgetLevel;
import com.sololifeos.user.domain.model.UserPreference;
import com.sololifeos.user.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

/**
 * 用户偏好领域服务 (CODE_RULES §3.1 Domain Service)。
 * <p>
 * 职责：偏好创建去重校验、更新规则。持久化归 Application Service (TASK-0103)。
 */
@Service
public class UserPreferenceDomainService {

    private final UserPreferenceRepository preferenceRepository;

    public UserPreferenceDomainService(UserPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * 为新用户创建默认偏好。校验一用户一偏好（uk_user_preference_user_id）。
     *
     * @return 未持久化的默认偏好（MEDIUM 预算）
     */
    public UserPreference createDefault(Long userId) {
        if (userId == null) {
            throw new BusinessException("userId 不可为空");
        }
        if (preferenceRepository.existsByUserId(userId)) {
            throw new BusinessException("用户偏好已存在: userId=" + userId);
        }
        return UserPreference.defaultFor(userId);
    }

    /**
     * 更新偏好内容。
     */
    public void update(UserPreference preference, String interest, BudgetLevel budget, String lifestyle) {
        preference.update(interest, budget, lifestyle);
    }
}
