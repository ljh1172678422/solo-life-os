package com.sololifeos.user.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.user.application.UserAssembler;
import com.sololifeos.user.application.UserPreferenceApplicationService;
import com.sololifeos.user.domain.model.UserPreference;
import com.sololifeos.user.dto.UserPreferenceResponse;
import com.sololifeos.user.dto.UserPreferenceUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户偏好控制器。
 */
@RestController
@RequestMapping("/api/users/{userId}/preference")
public class UserPreferenceController {

    private final UserPreferenceApplicationService preferenceApplicationService;

    public UserPreferenceController(UserPreferenceApplicationService preferenceApplicationService) {
        this.preferenceApplicationService = preferenceApplicationService;
    }

    /**
     * 获取用户偏好。
     */
    @GetMapping
    public ApiResponse<UserPreferenceResponse> get(@PathVariable Long userId) {
        UserPreference preference = preferenceApplicationService.getByUserId(userId);
        return ApiResponse.success(UserAssembler.toResponse(preference));
    }

    /**
     * 更新用户偏好（兴趣 / 预算 / 生活方式）。
     */
    @PutMapping
    public ApiResponse<UserPreferenceResponse> update(@PathVariable Long userId,
                                                      @Valid @RequestBody UserPreferenceUpdateRequest request) {
        UserPreference preference = preferenceApplicationService.update(
                userId, request.interest(), request.budget(), request.lifestyle());
        return ApiResponse.success(UserAssembler.toResponse(preference));
    }
}
