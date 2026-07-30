package com.sololifeos.user.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.user.application.UserApplicationService;
import com.sololifeos.user.application.UserAssembler;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.dto.UserRegisterRequest;
import com.sololifeos.user.dto.UserResponse;
import com.sololifeos.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器 (CODE_RULES §3.1 Controller: 接收请求 / 参数校验 / 返回封装)。
 * <p>
 * 注意：login 端点归 Auth 任务 (ADR-0006 JWT)，本控制器不涉及认证逻辑。
 * activate / ban 涉及权限控制，同样归 Auth 任务。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 注册新用户。事务内创建用户（含 BCrypt 哈希密码） + 默认偏好。
     * 路径 {@code POST /api/users} 已加入 JwtAuthFilter 白名单（MVP 开放注册）。
     */
    @PostMapping
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = userApplicationService.register(
                request.nickname(), request.email(), request.phone(), request.password());
        return ApiResponse.success(UserAssembler.toResponse(user));
    }

    /**
     * 获取用户资料。
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        User user = userApplicationService.getById(id);
        return ApiResponse.success(UserAssembler.toResponse(user));
    }

    /**
     * 更新用户资料（昵称 / 头像 / 城市）。
     */
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody UserUpdateRequest request) {
        User user = userApplicationService.updateProfile(id, request.nickname(), request.avatar(), request.city());
        return ApiResponse.success(UserAssembler.toResponse(user));
    }
}
