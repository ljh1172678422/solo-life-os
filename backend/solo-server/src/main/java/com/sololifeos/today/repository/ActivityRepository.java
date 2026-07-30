package com.sololifeos.today.repository;

import com.sololifeos.today.domain.model.Activity;
import com.sololifeos.today.domain.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * Spring Data JPA 代理实现，不手写 RepositoryImpl（框架约定）。
 * 查询方法对齐 DATABASE_DESIGN §8 索引：
 * <ul>
 *   <li>idx_activity_daily_plan (daily_plan_id) - 按计划查活动</li>
 *   <li>idx_activity_location (location_id) - 按地点查活动</li>
 *   <li>idx_activity_start_time (start_time) - 时间范围查询</li>
 * </ul>
 * <p>
 * 软删除过滤由 Entity 上的 {@code @SQLRestriction} 自动处理，无需在此声明。
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /** 按计划 ID 查活动列表（§8 idx_activity_daily_plan 索引，按开始时间正序）。 */
    List<Activity> findByDailyPlanIdOrderByStartTimeAsc(Long dailyPlanId);

    /** 按多个计划 ID 批量查活动（周/月视图聚合）。 */
    List<Activity> findByDailyPlanIdInOrderByStartTimeAsc(List<Long> dailyPlanIds);

    /** 按地点查活动（§8 idx_activity_location 索引，Explore Module 复用）。 */
    List<Activity> findByLocationIdOrderByStartTimeDesc(Long locationId);

    /** 按时间范围查活动（§8 idx_activity_start_time 索引，日历视图）。 */
    List<Activity> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    /** 按计划 ID + 类型筛选（统计：某日工作 / 休闲时长）。 */
    List<Activity> findByDailyPlanIdAndType(Long dailyPlanId, ActivityType type);
}
