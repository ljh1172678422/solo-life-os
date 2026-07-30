package com.sololifeos.today.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.repository.DailyPlanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 今日领域服务 (CODE_RULES §3.1 Domain Service)。
 * <p>
 * 职责：封装 Today Module 业务规则（计划创建校验、活动添加规则、状态变更规则）。
 * 不负责持久化（save 归 Application Service，TASK-0203）与事务边界。
 * 通过 Repository 做查询校验，返回领域对象交由上层持久化。
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>一个用户一天一个计划（业务唯一性，DB 层不强制）</li>
 *   <li>计划状态变更必须按状态机：PLANNING → ONGOING → COMPLETED / CANCELLED</li>
 *   <li>已取消 / 已完成的计划不可添加或修改活动</li>
 * </ul>
 */
@Service
public class TodayDomainService {

    private final DailyPlanRepository dailyPlanRepository;

    public TodayDomainService(DailyPlanRepository dailyPlanRepository) {
        this.dailyPlanRepository = dailyPlanRepository;
    }

    /**
     * 创建新计划。校验当日无已存在计划后返回 PLANNING 状态 DailyPlan。
     *
     * @param userId 用户 ID
     * @param date   计划日期
     * @return 未持久化的 PLANNING 状态 DailyPlan
     */
    public DailyPlan createPlan(Long userId, LocalDate date) {
        if (userId == null) {
            throw new BusinessException("用户 ID 不可为空");
        }
        if (date == null) {
            throw new BusinessException("计划日期不可为空");
        }
        if (dailyPlanRepository.existsByUserIdAndDate(userId, date)) {
            throw new BusinessException("该日期已存在计划: " + date);
        }
        return DailyPlan.create(userId, date);
    }

    /**
     * 在计划下添加活动。校验计划未关闭后创建活动并关联到计划。
     *
     * @param plan       所属计划（必须未 COMPLETED / CANCELLED）
     * @param title      活动标题
     * @param type       活动类型
     * @param startTime  开始时间
     * @return 未持久化的 Activity（已设置 dailyPlanId）
     */
    public Activity addActivityToPlan(DailyPlan plan, String title, ActivityType type, LocalDateTime startTime) {
        if (plan == null) {
            throw new BusinessException("所属计划不可为空");
        }
        if (plan.getStatus() == PlanStatus.COMPLETED || plan.getStatus() == PlanStatus.CANCELLED) {
            throw new BusinessException("已" + plan.getStatus() + "的计划不可添加活动");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException("活动标题不可为空");
        }
        if (startTime == null) {
            throw new BusinessException("活动开始时间不可为空");
        }
        ActivityType resolvedType = type != null ? type : ActivityType.OTHER;
        return Activity.create(plan.getId(), title, resolvedType, startTime);
    }

    /** 开始执行计划：PLANNING → ONGOING。 */
    public void startPlan(DailyPlan plan) {
        if (plan == null) {
            throw new BusinessException("计划不可为空");
        }
        plan.start();
    }

    /** 完成计划：ONGOING → COMPLETED。 */
    public void completePlan(DailyPlan plan) {
        if (plan == null) {
            throw new BusinessException("计划不可为空");
        }
        plan.complete();
    }

    /** 取消计划：PLANNING / ONGOING → CANCELLED。 */
    public void cancelPlan(DailyPlan plan) {
        if (plan == null) {
            throw new BusinessException("计划不可为空");
        }
        plan.cancel();
    }

    /**
     * 修改活动信息。校验所属计划未关闭后委托给 Activity。
     */
    public void updateActivity(DailyPlan plan, Activity activity, String title, ActivityType type,
                               LocalDateTime startTime, LocalDateTime endTime) {
        if (plan == null || activity == null) {
            throw new BusinessException("计划与活动不可为空");
        }
        if (plan.getStatus() == PlanStatus.COMPLETED || plan.getStatus() == PlanStatus.CANCELLED) {
            throw new BusinessException("已" + plan.getStatus() + "的计划不可修改活动");
        }
        activity.update(title, type, startTime, endTime);
    }
}
