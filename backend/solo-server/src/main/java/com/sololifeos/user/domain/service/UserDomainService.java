package com.sololifeos.user.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserStatus;
import com.sololifeos.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务 (CODE_RULES §3.1 Domain Service)。
 * <p>
 * 职责：封装用户业务规则（注册校验、状态变更规则、资料更新规则）。
 * 不负责持久化（save 归 Application Service，TASK-0103）与事务边界。
 * 通过 Repository 做查询校验，返回领域对象交由上层持久化。
 */
@Service
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 注册新用户。校验邮箱/手机号唯一后创建未激活用户对象。
     *
     * @param nickname 昵称（必填）
     * @param email    邮箱（可空，非空时需唯一）
     * @param phone    手机号（可空，非空时需唯一）
     * @return 未持久化的 INACTIVE 状态 User
     */
    public User register(String nickname, String email, String phone) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException("用户昵称不可为空");
        }
        validateContactUnique(email, phone);
        return User.register(nickname, email, phone);
    }

    private void validateContactUnique(String email, String phone) {
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已被注册: " + email);
        }
        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new BusinessException("手机号已被注册: " + phone);
        }
    }

    /**
     * 激活用户。封禁用户不可激活。
     */
    public void activate(User user) {
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("封禁用户不可激活");
        }
        user.activate();
    }

    /**
     * 封禁用户。
     */
    public void ban(User user) {
        user.ban();
    }

    /**
     * 更新用户资料（昵称 / 头像 / 城市）。
     */
    public void updateProfile(User user, String nickname, String avatar, String city) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException("用户昵称不可为空");
        }
        user.updateProfile(nickname, avatar, city);
    }
}
