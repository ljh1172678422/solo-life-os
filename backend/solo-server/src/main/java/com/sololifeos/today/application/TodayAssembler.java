package com.sololifeos.today.application;

import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.dto.ActivityResponse;
import com.sololifeos.today.dto.DailyPlanResponse;

import java.util.List;

/**
 * Today Module DTO 转换器 (CODE_RULES §5: Controller 与 Application Service 之间必须经 DTO 转换)。
 * <p>
 * 将 Domain Entity 转换为 Response DTO，禁止反向转换（入参由 record 直接承载）。
 * 与 User Module UserAssembler 模式一致（Sprint 1 TASK-0104）。
 */
public final class TodayAssembler {

    private TodayAssembler() {
    }

    public static DailyPlanResponse toResponse(DailyPlan plan) {
        return new DailyPlanResponse(
                plan.getId(),
                plan.getUserId(),
                plan.getDate(),
                plan.getStatus() != null ? plan.getStatus().name() : null,
                plan.getCreatedTime(),
                plan.getUpdatedTime()
        );
    }

    public static ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getDailyPlanId(),
                activity.getTitle(),
                activity.getType() != null ? activity.getType().name() : null,
                activity.getLocationId(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getCreatedTime(),
                activity.getUpdatedTime()
        );
    }

    public static List<DailyPlanResponse> toPlanResponseList(List<DailyPlan> plans) {
        return plans.stream().map(TodayAssembler::toResponse).toList();
    }

    public static List<ActivityResponse> toActivityResponseList(List<Activity> activities) {
        return activities.stream().map(TodayAssembler::toResponse).toList();
    }
}
