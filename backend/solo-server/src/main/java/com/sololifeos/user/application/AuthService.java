package com.sololifeos.user.application;

import com.sololifeos.common.exception.AuthException;
import com.sololifeos.common.security.JwtService;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserStatus;
import com.sololifeos.user.dto.LoginRequest;
import com.sololifeos.user.dto.LoginResponse;
import com.sololifeos.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 认证应用服务 (ADR-0006 JWT Authentication, CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：登录用例编排 —— 按账号（邮箱 / 手机号）查询用户、BCrypt 校验密码、
 * 签发 JWT token。不负责请求拦截（归 {@code JwtAuthFilter}）。
 * <p>
 * 安全规则：
 * <ul>
 *   <li>密码明文不入日志、不入库、不出现在异常 message 中</li>
 *   <li>账号不存在与密码错误返回相同 message，防止账号枚举</li>
 *   <li>封禁用户禁止登录</li>
 * </ul>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** 登录失败的统一 message，防止区分"账号不存在"与"密码错误"导致账号枚举。 */
    private static final String LOGIN_FAILED_MESSAGE = "账号或密码错误";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 登录：账号（邮箱 / 手机号）+ 密码 -> JWT token。
     *
     * @param request 登录请求（account / password）
     * @return JWT token + 基础用户信息
     * @throws AuthException 账号不存在 / 密码错误 / 用户被封禁
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String account = request.account();
        Optional<User> userOpt = findByAccount(account);

        // 账号不存在：抛 AuthException（message 统一，防枚举）
        if (userOpt.isEmpty()) {
            log.warn("登录失败-账号不存在: account={}", maskAccount(account));
            throw new AuthException(LOGIN_FAILED_MESSAGE);
        }

        User user = userOpt.get();

        // 密码校验（BCrypt）
        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("登录失败-密码错误: userId={}, account={}", user.getId(), maskAccount(account));
            throw new AuthException(LOGIN_FAILED_MESSAGE);
        }

        // 封禁用户禁止登录
        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("登录失败-用户已封禁: userId={}", user.getId());
            throw new AuthException("账号已被封禁");
        }

        // 签发 token
        String token = jwtService.generateToken(user.getId(), user.getNickname());
        log.info("登录成功: userId={}, nickname={}", user.getId(), user.getNickname());
        return new LoginResponse(token, user.getId(), user.getNickname());
    }

    /**
     * 按账号查询用户：先按邮箱查，未命中再按手机号查。
     * account 可能是邮箱也可能是手机号，二者格式不同不会冲突。
     */
    private Optional<User> findByAccount(String account) {
        if (account == null || account.isBlank()) {
            return Optional.empty();
        }
        // 含 @ 视为邮箱
        if (account.contains("@")) {
            return userRepository.findByEmail(account);
        }
        return userRepository.findByPhone(account);
    }

    /**
     * 账号脱敏：日志中只保留前 2 位 + *** + 后 1 位，防止日志泄漏完整账号。
     */
    private String maskAccount(String account) {
        if (account == null || account.length() <= 3) {
            return "***";
        }
        return account.substring(0, 2) + "***" + account.charAt(account.length() - 1);
    }
}
