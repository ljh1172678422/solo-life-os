-- TASK-0107 Authentication (ADR-0006 JWT)
-- Add password column to user table
-- Owner: User Module
--
-- ADR-0006: BCrypt 哈希密码，nullable 兼容存量数据
-- 不加索引（密码不用于查询，登录按 email/phone 查用户后再校验密码）

ALTER TABLE "user"
    ADD COLUMN IF NOT EXISTS password VARCHAR(100);

COMMENT ON COLUMN "user".password IS 'BCrypt 哈希密码（ADR-0006），注册时写入，登录时校验';
