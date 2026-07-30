-- TASK-0201 Review 改进（PR #19 Reviewer 反馈）
-- Refine daily_plan + activity schema: unique index + CHECK constraints
-- Owner: Today Module
--
-- Reviewer 反馈处理：
--   1. daily_plan(user_id, date) 升级为唯一索引 → 保证每用户每天仅一份计划
--   2. status / type 增加 CHECK 约束 → DB 层约束枚举合法性（Application 层校验为第一道防线）
--   3. updated_time 维护策略：应用层 Hibernate @UpdateTimestamp 自动维护，无 DB Trigger
--      （已在 Entity 注释明确，本 Migration 不涉及 Trigger）
--
-- 注意：唯一索引带 WHERE deleted_time IS NULL，允许已软删除记录不被唯一约束限制
-- （同一用户同一天可有多个已删除计划，符合软删除语义）

-- 1. daily_plan(user_id, date) 升级为唯一索引
DROP INDEX IF EXISTS idx_daily_plan_user_date;
CREATE UNIQUE INDEX IF NOT EXISTS uk_daily_plan_user_date
    ON daily_plan (user_id, date)
    WHERE deleted_time IS NULL;

-- 2. daily_plan.status CHECK 约束（DATABASE_DESIGN §7 PLAN_STATUS）
ALTER TABLE daily_plan
    DROP CONSTRAINT IF EXISTS chk_daily_plan_status;
ALTER TABLE daily_plan
    ADD CONSTRAINT chk_daily_plan_status
    CHECK (status IN ('PLANNING', 'ONGOING', 'COMPLETED', 'CANCELLED'));

-- 3. activity.type CHECK 约束（DATABASE_DESIGN §7 ACTIVITY_TYPE）
ALTER TABLE activity
    DROP CONSTRAINT IF EXISTS chk_activity_type;
ALTER TABLE activity
    ADD CONSTRAINT chk_activity_type
    CHECK (type IN ('WORK', 'LEISURE', 'SPORT', 'STUDY', 'SOCIAL', 'EXPLORE', 'REST', 'OTHER'));

COMMENT ON INDEX uk_daily_plan_user_date IS '每用户每天唯一计划（PR #19 Review 改进，软删除记录不受约束）';
