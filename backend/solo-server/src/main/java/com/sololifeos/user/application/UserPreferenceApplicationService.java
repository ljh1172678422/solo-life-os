package com.sololifeos.user.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.BudgetLevel;
import com.sololifeos.user.domain.model.UserPreference;
import com.sololifeos.user.domain.service.UserPreferenceDomainService;
import com.sololifeos.user.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户偏好应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：偏好读写用例协调 / 事务边界。默认偏好在注册时由
 * {@link UserApplicationService#register} 创建。
 */
@Service
public class UserPreferenceApplicationService {

    private final UserPreferenceDomainService preferenceDomainService;
    private final UserPreferenceRepository preferenceRepository;

    public UserPreferenceApplicationService(UserPreferenceDomainService preferenceDomainService,
                                            UserPreferenceRepository preferenceRepository) {
        this.preferenceDomainService = preferenceDomainService;
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * 获取用户偏好。
     */
    @Transactional(readOnly = true)
    public UserPreference getByUserId(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户偏好不存在: userId=" + userId));
    }

    /**
     * 更新用户偏好（兴趣 / 预算 / 生活方式）。
     */
    @Transactional
    public UserPreference update(Long userId, String interest, BudgetLevel budget, String lifestyle) {
        UserPreference preference = getByUserId(userId);
        preferenceDomainService.update(preference, interest, budget, lifestyle);
        return preferenceRepository.save(preference);
    }
}
