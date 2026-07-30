package com.sololifeos.today.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.today.application.ActivityApplicationService;
import com.sololifeos.today.application.TodayAssembler;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.dto.ActivityCreateRequest;
import com.sololifeos.today.dto.ActivityResponse;
import com.sololifeos.today.dto.ActivityUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动控制器 (CODE_RULES §3.1 Controller: 接收请求 / 参数校验 / 返回封装)。
 * <p>
 * 路由设计：
 * <ul>
 *   <li>{@code /api/plans/{planId}/activities} — 计划维度：添加活动 / 列出计划下活动</li>
 *   <li>{@code /api/activities/{activityId}} — 单活动操作（获取 / 修改 / 结束 / 绑定地点）</li>
 * </ul>
 * <p>
 * 权限：所有端点需 JWT 认证（JwtAuthFilter）。
 */
@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ActivityApplicationService activityApplicationService;

    public ActivityController(ActivityApplicationService activityApplicationService) {
        this.activityApplicationService = activityApplicationService;
    }

    /**
     * 在计划下添加活动。
     */
    @PostMapping("/plans/{planId}/activities")
    public ApiResponse<ActivityResponse> create(@PathVariable Long planId,
                                                @Valid @RequestBody ActivityCreateRequest request) {
        Activity activity = activityApplicationService.addActivity(
                planId, request.title(), request.type(), request.startTime());
        return ApiResponse.success(TodayAssembler.toResponse(activity));
    }

    /**
     * 列出计划下所有活动（按开始时间正序）。
     */
    @GetMapping("/plans/{planId}/activities")
    public ApiResponse<List<ActivityResponse>> listByPlan(@PathVariable Long planId) {
        List<Activity> activities = activityApplicationService.listActivitiesByPlan(planId);
        return ApiResponse.success(TodayAssembler.toActivityResponseList(activities));
    }

    /**
     * 按活动 ID 查询。
     */
    @GetMapping("/activities/{activityId}")
    public ApiResponse<ActivityResponse> getById(@PathVariable Long activityId) {
        Activity activity = activityApplicationService.getActivity(activityId);
        return ApiResponse.success(TodayAssembler.toResponse(activity));
    }

    /**
     * 修改活动信息（整体替换语义）。
     */
    @PutMapping("/plans/{planId}/activities/{activityId}")
    public ApiResponse<ActivityResponse> update(@PathVariable Long planId,
                                                @PathVariable Long activityId,
                                                @Valid @RequestBody ActivityUpdateRequest request) {
        Activity activity = activityApplicationService.updateActivity(
                planId, activityId, request.title(), request.type(), request.startTime(), request.endTime());
        return ApiResponse.success(TodayAssembler.toResponse(activity));
    }

    /**
     * 设置活动结束时间。
     */
    @PostMapping("/activities/{activityId}/end")
    public ApiResponse<ActivityResponse> end(@PathVariable Long activityId,
                                             @RequestBody EndActivityRequest request) {
        Activity activity = activityApplicationService.endActivity(activityId, request.endTime());
        return ApiResponse.success(TodayAssembler.toResponse(activity));
    }

    /**
     * 绑定活动地点（Explore Module, Sprint 3 后可用）。
     */
    @PostMapping("/activities/{activityId}/locate")
    public ApiResponse<ActivityResponse> locate(@PathVariable Long activityId,
                                                @RequestBody LocateActivityRequest request) {
        Activity activity = activityApplicationService.locateActivity(activityId, request.locationId());
        return ApiResponse.success(TodayAssembler.toResponse(activity));
    }

    /** end 端点请求体（仅 endTime，可空表示清除结束时间）。 */
    public record EndActivityRequest(java.time.LocalDateTime endTime) {
    }

    /** locate 端点请求体（locationId，Sprint 3 后非空）。 */
    public record LocateActivityRequest(Long locationId) {
    }
}
