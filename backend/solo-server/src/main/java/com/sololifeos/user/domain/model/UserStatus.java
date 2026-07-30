package com.sololifeos.user.domain.model;

/**
 * 用户状态枚举 (DATABASE_DESIGN §7 USER_STATUS)。
 * <p>
 * 数据库以 varchar 存储，禁止自由字符串 (DATABASE_DESIGN §7)。
 */
public enum UserStatus {

    /** 正常 */
    ACTIVE,

    /** 未激活 */
    INACTIVE,

    /** 封禁 */
    BANNED
}
