package com.sololifeos.today.application;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import com.sololifeos.today.domain.service.TodayDomainService;
import com.sololifeos.today.repository.DailyPlanRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日计划应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：计划生命周期用例协调 / 事务边界。调用 {@link TodayDomainService} 做业务规则校验，
 * 调用 {@link DailyPlanRepository} 做持久化。入参用原始类型，出参用 Domain Entity
 * （DTO 转换归 Controller 层，TASK-0204）。
 * <p>
 * 唯一性保证：{@link #createPlan} 由 Domain Service 校验为第一道防线，
 * DB 层 uk_daily_plan_user_date 兜底（PR #19 Review 改进）。
 * 并发场景下两个事务同时通过 Application 校验，commit 时其中一个触发 uk 唯一约束，
 * 由 {@link DataIntegrityViolationException} 捕获转为 BusinessException（PR #21 Review 改进）。
 * <p>
 * PR #21 Review 改进：抽取 {@link #requirePlan(Long)} 私有方法消除重复查询 + 异常处理。
 */
@Service
public class DailyPlanApplicationService {

    private final TodayDomainService todayDomainService;
    private final DailyPlanRepository dailyPlanRepository;

    public DailyPlanApplicationService(TodayDomainService todayDomainService,
                                       DailyPlanRepository dailyPlanRepository) {
        this.todayDomainService = todayDomainService;
        this.dailyPlanRepository = dailyPlanRepository;
    }

    /**
     * 创建每日计划。校验当日无已存在计划后持久化。
     * <p>
     * 并发安全：两个事务同时通过 existsByUserIdAndDate 校验时，DB 层 uk_daily_plan_user_date
     * 拒绝第二个 commit，捕获 DataIntegrityViolationException 转为 BusinessException，
     * 避免返回 500（PR #21 Review 改进）。
     *
     * @param userId 用户 ID
     * @param date   计划日期
     * @return 已持久化的 PLANNING 状态 DailyPlan
     */
    @Transactional
    public DailyPlan createPlan(Long userId, LocalDate date) {
        DailyPlan plan = todayDomainService.createPlan(userId, date);
        try {
            return dailyPlanRepository.save(plan);
        } catch (DataIntegrityViolationException e) {
            // 并发下两个事务同时通过业务校验，DB uk_daily_plan_user_date 兜底拒绝
            throw new BusinessException("该日期已存在计划（并发创建冲突）: userId=" + userId + ", date=" + date);
        }
    }

    /**
     * 按计划 ID 查询。
     */
    @Transactional(readOnly = true)
    public DailyPlan getPlanById(Long planId) {
        return requirePlan(planId);
    }

    /**
     * 按用户 + 日期查询计划（登录后取今日计划）。
     *
     * @return 计划 Optional（可能不存在）
     */
    @Transactional(readOnly = true)
    public Optional<DailyPlan> getPlanByUserAndDate(Long userId, LocalDate date) {
        return dailyPlanRepository.findByUserIdAndDate(userId, date);
    }

    /**
     * 查询用户全部计划（按日期倒序）。
     */
    @Transactional(readOnly = true)
    public List<DailyPlan> listUserPlans(Long userId) {
        return dailyPlanRepository.findByUserIdOrderByDateDesc(userId);
    }

    /**
     * 按日期范围查询用户计划（周 / 月视图）。
     */
    @Transactional(readOnly = true)
    public List<DailyPlan> listPlansByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return dailyPlanRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
    }

    /**
     * 按状态筛选用户计划（活跃计划列表）。
     */
    @Transactional(readOnly = true)
    public List<DailyPlan> listPlansByStatus(Long userId, PlanStatus status) {
        return dailyPlanRepository.findByUserIdAndStatus(userId, status);
    }

    /** 开始执行计划：PLANNING → ONGOING。 */
    @Transactional
    public DailyPlan startPlan(Long planId) {
        DailyPlan plan = requirePlan(planId);
        todayDomainService.startPlan(plan);
        return dailyPlanRepository.save(plan);
    }

    /** 完成计划：ONGOING → COMPLETED。 */
    @Transactional
    public DailyPlan completePlan(Long planId) {
        DailyPlan plan = requirePlan(planId);
        todayDomainService.completePlan(plan);
        return dailyPlanRepository.save(plan);
    }

    /** 取消计划：PLANNING / ONGOING → CANCELLED。 */
    @Transactional
    public DailyPlan cancelPlan(Long planId) {
        DailyPlan plan = requirePlan(planId);
        todayDomainService.cancelPlan(plan);
        return dailyPlanRepository.save(plan);
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
}
