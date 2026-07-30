package com.sololifeos.today.domain.model;

/**
 * 活动类型枚举 (DATABASE_DESIGN §7 ACTIVITY_TYPE)。
 * <p>
 * 数据库以 varchar 存储，禁止自由字符串 (DATABASE_DESIGN §7)。
 */
public enum ActivityType {

    /** 工作 */
    WORK,

    /** 休闲 */
    LEISURE,

    /** 运动 */
    SPORT,

    /** 学习 */
    STUDY,

    /** 社交 */
    SOCIAL,

    /** 探索 */
    EXPLORE,

    /** 休息 */
    REST,

    /** 其他 */
    OTHER
}
