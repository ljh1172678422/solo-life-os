package com.sololifeos.today.repository;

import com.sololifeos.today.domain.model.DailyPlan;
import com.sololifeos.today.domain.model.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日计划仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * Spring Data JPA 代理实现，不手写 RepositoryImpl（框架约定）。
 * 查询方法对齐 DATABASE_DESIGN §8 索引：idx_daily_plan_user_date (user_id, date)。
 * <p>
 * 软删除过滤由 Entity 上的 {@code @SQLRestriction} 自动处理，无需在此声明。
 */
@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {

    /** 按用户 + 日期查询计划（登录后取今日计划，§8 idx_daily_plan_user_date 索引）。 */
    Optional<DailyPlan> findByUserIdAndDate(Long userId, LocalDate date);

    /** 按用户查询所有计划（按日期倒序）。 */
    List<DailyPlan> findByUserIdOrderByDateDesc(Long userId);

    /** 按用户 + 状态筛选（活跃计划列表）。 */
    List<DailyPlan> findByUserIdAndStatus(Long userId, PlanStatus status);

    /** 是否已存在某日计划（业务唯一性校验，DB 层不强制唯一约束）。 */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    /** 按日期范围查询用户计划（周 / 月视图）。 */
    List<DailyPlan> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate startDate, LocalDate endDate);
}
