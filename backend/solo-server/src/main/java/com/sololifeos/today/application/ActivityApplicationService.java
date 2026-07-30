package com.sololifeos.today.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.service.TodayDomainService;
import com.sololifeos.today.repository.ActivityRepository;
import com.sololifeos.today.repository.DailyPlanRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：活动用例协调 / 事务边界。活动归属于 {@link DailyPlan}，写操作需先加载所属计划
 * 经 {@link TodayDomainService} 校验后持久化。
 * 入参用原始类型，出参用 Domain Entity（DTO 转换归 Controller 层，TASK-0204）。
 * <p>
 * PR #21 Review 改进：
 * <ul>
 *   <li>抽取 {@link #requirePlan(Long)} / {@link #requireActivity(Long)} 私有方法消除重复查询 + 异常处理</li>
 *   <li>endActivity / locateActivity 业务校验下沉到 Domain Service，本层只负责事务 / 加载聚合 / 持久化</li>
 *   <li>写操作捕获 {@link DataIntegrityViolationException}（DB 层 CHECK / 唯一约束兜底）转为 BusinessException，
 *       避免并发或边界场景返回 500</li>
 * </ul>
 */
@Service
public class ActivityApplicationService {

    private final TodayDomainService todayDomainService;
    private final ActivityRepository activityRepository;
    private final DailyPlanRepository dailyPlanRepository;

    public ActivityApplicationService(TodayDomainService todayDomainService,
                                      ActivityRepository activityRepository,
                                      DailyPlanRepository dailyPlanRepository) {
        this.todayDomainService = todayDomainService;
        this.activityRepository = activityRepository;
        this.dailyPlanRepository = dailyPlanRepository;
    }

    /**
     * 在计划下添加活动。校验计划已持久化 + 未关闭后创建并持久化。
     *
     * @param planId    所属计划 ID（必须已持久化）
     * @param title     活动标题
     * @param type      活动类型（可空，null → OTHER）
     * @param startTime 开始时间
     * @return 已持久化的 Activity
     */
    @Transactional
    public Activity addActivity(Long planId, String title, ActivityType type, LocalDateTime startTime) {
        DailyPlan plan = requirePlan(planId);
        Activity activity = todayDomainService.addActivityToPlan(plan, title, type, startTime);
        try {
            return activityRepository.save(activity);
        } catch (DataIntegrityViolationException e) {
            // DB 层 CHECK 约束（chk_activity_type）兜底：枚举值非法时返回业务异常而非 500
            throw new BusinessException("活动数据不合法（类型 / 字段约束）: " + e.getMostSpecificCause().getMessage());
        }
    }

    /**
     * 按活动 ID 查询。
     */
    @Transactional(readOnly = true)
    public Activity getActivity(Long activityId) {
        return requireActivity(activityId);
    }

    /**
     * 按计划 ID 查活动列表（按开始时间正序）。
     */
    @Transactional(readOnly = true)
    public List<Activity> listActivitiesByPlan(Long planId) {
        return activityRepository.findByDailyPlanIdOrderByStartTimeAsc(planId);
    }

    /**
     * 按多个计划 ID 批量查活动（周 / 月视图聚合）。
     */
    @Transactional(readOnly = true)
    public List<Activity> listActivitiesByPlans(List<Long> planIds) {
        return activityRepository.findByDailyPlanIdInOrderByStartTimeAsc(planIds);
    }

    /**
     * 按地点查活动（Explore Module 复用，按开始时间倒序）。
     */
    @Transactional(readOnly = true)
    public List<Activity> listActivitiesByLocation(Long locationId) {
        return activityRepository.findByLocationIdOrderByStartTimeDesc(locationId);
    }

    /**
     * 按时间范围查活动（日历视图）。
     */
    @Transactional(readOnly = true)
    public List<Activity> listActivitiesByTimeRange(LocalDateTime start, LocalDateTime end) {
        return activityRepository.findByStartTimeBetweenOrderByStartTimeAsc(start, end);
    }

    /**
     * 修改活动信息。校验所属计划未关闭 + 活动归属后委托给 Domain Service。
     *
     * @param planId     所属计划 ID
     * @param activityId 活动 ID
     */
    @Transactional
    public Activity updateActivity(Long planId, Long activityId, String title, ActivityType type,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        DailyPlan plan = requirePlan(planId);
        Activity activity = requireActivity(activityId);
        requireActivityBelongsToPlan(activity, plan);
        todayDomainService.updateActivity(plan, activity, title, type, startTime, endTime);
        try {
            return activityRepository.save(activity);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("活动数据不合法（类型 / 字段约束）: " + e.getMostSpecificCause().getMessage());
        }
    }

    /**
     * 设置活动结束时间。校验所属计划未关闭后委托给 Domain Service。
     */
    @Transactional
    public Activity endActivity(Long activityId, LocalDateTime endTime) {
        Activity activity = requireActivity(activityId);
        DailyPlan plan = requirePlan(activity.getDailyPlanId());
        todayDomainService.endActivity(plan, activity, endTime);
        return activityRepository.save(activity);
    }

    /**
     * 绑定活动地点（Explore Module, Sprint 3 后可用）。校验所属计划未关闭后委托给 Domain Service。
     */
    @Transactional
    public Activity locateActivity(Long activityId, Long locationId) {
        Activity activity = requireActivity(activityId);
        DailyPlan plan = requirePlan(activity.getDailyPlanId());
        todayDomainService.locateActivity(plan, activity, locationId);
        return activityRepository.save(activity);
    }

    // --- 私有加载方法（PR #21 Review 改进：消除重复查询 + 异常处理） ---

    /** 加载计划，不存在抛 BusinessException。 */
    private DailyPlan requirePlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("计划 ID 不可为空");
        }
        return dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("计划不存在: id=" + planId));
    }

    /** 加载活动，不存在抛 BusinessException。 */
    private Activity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new BusinessException("活动 ID 不可为空");
        }
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在: id=" + activityId));
    }

    /** 校验活动归属计划，防跨计划修改。 */
    private void requireActivityBelongsToPlan(Activity activity, DailyPlan plan) {
        if (!plan.getId().equals(activity.getDailyPlanId())) {
            throw new BusinessException("活动不属于该计划: activityId=" + activity.getId() + ", planId=" + plan.getId());
        }
    }
}
