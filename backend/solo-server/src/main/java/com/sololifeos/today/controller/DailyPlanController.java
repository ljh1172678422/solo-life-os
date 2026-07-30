package com.sololifeos.today.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.today.application.DailyPlanApplicationService;
import com.sololifeos.today.application.TodayAssembler;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.dto.DailyPlanCreateRequest;
import com.sololifeos.today.dto.DailyPlanResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日计划控制器 (CODE_RULES §3.1 Controller: 接收请求 / 参数校验 / 返回封装)。
 * <p>
 * 路由设计：
 * <ul>
 *   <li>{@code /api/users/{userId}/plans} — 用户维度的计划列表 / 创建（userId 在路径，资源归属清晰）</li>
 *   <li>{@code /api/plans/{planId}} — 单计划操作（获取 / 开始 / 完成 / 取消）</li>
 * </ul>
 * <p>
 * 权限：所有端点需 JWT 认证（JwtAuthFilter），userId 校验归后续中间件 / ADR 任务（本 Sprint 不实现）。
 */
@RestController
@RequestMapping("/api")
public class DailyPlanController {

    private final DailyPlanApplicationService planApplicationService;

    public DailyPlanController(DailyPlanApplicationService planApplicationService) {
        this.planApplicationService = planApplicationService;
    }

    /**
     * 创建每日计划。一个用户一天一个计划（业务校验 + DB uk 兜底）。
     */
    @PostMapping("/users/{userId}/plans")
    public ApiResponse<DailyPlanResponse> create(@PathVariable Long userId,
                                                 @Valid @RequestBody DailyPlanCreateRequest request) {
        DailyPlan plan = planApplicationService.createPlan(userId, request.date());
        return ApiResponse.success(TodayAssembler.toResponse(plan));
    }

    /**
     * 按用户 + 日期查询今日计划（登录后取今日计划）。不存在时返回 200 + data=null
     * （用户尚未创建当日计划属正常状态，非错误，故不用 404）。
     */
    @GetMapping("/users/{userId}/plans/today")
    public ApiResponse<DailyPlanResponse> getToday(@PathVariable Long userId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return planApplicationService.getPlanByUserAndDate(userId, date)
                .map(plan -> ApiResponse.success(TodayAssembler.toResponse(plan)))
                .orElseGet(() -> ApiResponse.success(null));
    }

    /**
     * 查询用户全部计划（按日期倒序）。支持按日期范围 {@code ?startDate=&endDate=} 筛选（周 / 月视图）。
     */
    @GetMapping("/users/{userId}/plans")
    public ApiResponse<List<DailyPlanResponse>> list(@PathVariable Long userId,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                     @RequestParam(required = false) PlanStatus status) {
        List<DailyPlan> plans;
        if (startDate != null && endDate != null) {
            plans = planApplicationService.listPlansByDateRange(userId, startDate, endDate);
        } else if (status != null) {
            plans = planApplicationService.listPlansByStatus(userId, status);
        } else {
            plans = planApplicationService.listUserPlans(userId);
        }
        return ApiResponse.success(TodayAssembler.toPlanResponseList(plans));
    }

    /**
     * 按计划 ID 查询。
     */
    @GetMapping("/plans/{planId}")
    public ApiResponse<DailyPlanResponse> getById(@PathVariable Long planId) {
        DailyPlan plan = planApplicationService.getPlanById(planId);
        return ApiResponse.success(TodayAssembler.toResponse(plan));
    }

    /**
     * 开始执行计划：PLANNING → ONGOING。
     */
    @PostMapping("/plans/{planId}/start")
    public ApiResponse<DailyPlanResponse> start(@PathVariable Long planId) {
        DailyPlan plan = planApplicationService.startPlan(planId);
        return ApiResponse.success(TodayAssembler.toResponse(plan));
    }

    /**
     * 完成计划：ONGOING → COMPLETED。
     */
    @PostMapping("/plans/{planId}/complete")
    public ApiResponse<DailyPlanResponse> complete(@PathVariable Long planId) {
        DailyPlan plan = planApplicationService.completePlan(planId);
        return ApiResponse.success(TodayAssembler.toResponse(plan));
    }

    /**
     * 取消计划：PLANNING / ONGOING → CANCELLED。
     */
    @PostMapping("/plans/{planId}/cancel")
    public ApiResponse<DailyPlanResponse> cancel(@PathVariable Long planId) {
        DailyPlan plan = planApplicationService.cancelPlan(planId);
        return ApiResponse.success(TodayAssembler.toResponse(plan));
    }
}
