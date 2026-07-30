package com.sololifeos.user.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.user.application.AuthService;
import com.sololifeos.user.dto.LoginRequest;
import com.sololifeos.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 (ADR-0006 JWT Authentication, CODE_RULES §3.1 Controller)。
 * <p>
 * 仅提供 {@code POST /api/auth/login} 登录端点。注册复用 {@link UserController#register}，
 * 注册时同时设置密码（ADR-0006 Impact）。
 * <p>
 * 路径 {@code /api/auth/login} 已加入 JwtAuthFilter 白名单，无需 token 即可访问。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录：账号（邮箱 / 手机号）+ 密码 -> JWT token。
     * <p>
     * 成功返回 200 + ApiResponse{data: {token, userId, nickname}}。
     * 失败返回 401 + ApiResponse{code: 1003, message: "账号或密码错误"}。
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
