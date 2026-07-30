package com.sololifeos.today.domain.model;

/**
 * 每日计划状态枚举 (DATABASE_DESIGN §7 PLAN_STATUS)。
 * <p>
 * 数据库以 varchar 存储，禁止自由字符串 (DATABASE_DESIGN §7)。
 */
public enum PlanStatus {

    /** 规划中 */
    PLANNING,

    /** 进行中 */
    ONGOING,

    /** 已完成 */
    COMPLETED,

    /** 已取消 */
    CANCELLED
}
