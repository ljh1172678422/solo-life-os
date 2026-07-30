package com.sololifeos.user.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserPreference;
import com.sololifeos.user.domain.service.UserDomainService;
import com.sololifeos.user.domain.service.UserPreferenceDomainService;
import com.sololifeos.user.repository.UserPreferenceRepository;
import com.sololifeos.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：协调用例 / 事务边界。调用 Domain Service 做业务规则校验，
 * 调用 Repository 做持久化。入参用原始类型，出参用 Domain Entity
 * （DTO 转换归 Controller 层，TASK-0104）。
 */
@Service
public class UserApplicationService {

    private final UserDomainService userDomainService;
    private final UserPreferenceDomainService preferenceDomainService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public UserApplicationService(UserDomainService userDomainService,
                                  UserPreferenceDomainService preferenceDomainService,
                                  UserRepository userRepository,
                                  UserPreferenceRepository preferenceRepository) {
        this.userDomainService = userDomainService;
        this.preferenceDomainService = preferenceDomainService;
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * 注册新用户。事务内创建用户 + 默认偏好（注册闭环）。
     */
    @Transactional
    public User register(String nickname, String email, String phone) {
        User user = userDomainService.register(nickname, email, phone);
        userRepository.save(user);
        // 注册时自动创建默认偏好（SPRINT_PLAN: 注册→登录→设置偏好闭环）
        UserPreference preference = preferenceDomainService.createDefault(user.getId());
        preferenceRepository.save(preference);
        return user;
    }

    /**
     * 获取用户资料。
     */
    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: id=" + userId));
    }

    /**
     * 按邮箱查询（登录用）。
     */
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在: email=" + email));
    }

    /**
     * 按手机号查询（登录用）。
     */
    @Transactional(readOnly = true)
    public User getByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在: phone=" + phone));
    }

    /**
     * 更新用户资料（昵称 / 头像 / 城市）。
     */
    @Transactional
    public User updateProfile(Long userId, String nickname, String avatar, String city) {
        User user = getById(userId);
        userDomainService.updateProfile(user, nickname, avatar, city);
        return userRepository.save(user);
    }

    /**
     * 激活用户。
     */
    @Transactional
    public void activate(Long userId) {
        User user = getById(userId);
        userDomainService.activate(user);
        userRepository.save(user);
    }

    /**
     * 封禁用户。
     */
    @Transactional
    public void ban(Long userId) {
        User user = getById(userId);
        userDomainService.ban(user);
        userRepository.save(user);
    }
}
