# User Module Migration Review

Task: TASK-0101

Date: 2026-07-29

Reviewer: Architecture Agent

Status: Approved (无增量 Migration)


> 审查 Sprint 0（TASK-0004）创建的 user / user_preference / tag 三张表是否符合 User Module Domain Design（DATABASE_DESIGN v2.1 §6.1 / §6.2 / §6.10 + §7 枚举 + §8 索引 + §9 外键策略）。
> 规则（TASK_BOARD）：如需字段扩展，通过增量 Migration 修改，禁止重复创建表。


---

# 1. 审查范围

| 表 | Migration | 设计基线 | Owner |
|----|-----------|----------|-------|
| user | V20260728_001__create_user_table.sql | DATABASE_DESIGN §6.1 | User Module |
| user_preference | V20260728_002__create_user_preference_table.sql | DATABASE_DESIGN §6.2 | User Module |
| tag | V20260728_003__create_tag_table.sql | DATABASE_DESIGN §6.10 | User Module（物理创建归 TASK-0004；Owner 归属待 ADR-0010 Accepted 定稿） |


---

# 2. 字段逐项核对


## 2.1 user 表（§6.1）

| 字段 | 设计类型 | Migration 类型 | Nullable | 默认值 | 结论 |
|------|----------|----------------|----------|--------|------|
| id | bigint | BIGINT IDENTITY PK | N | - | ✅ |
| nickname | varchar(50) | VARCHAR(50) | N | - | ✅ |
| avatar | varchar(500) | VARCHAR(500) | Y | - | ✅ |
| email | varchar(100) | VARCHAR(100) | Y | - | ✅ |
| phone | varchar(20) | VARCHAR(20) | Y | - | ✅ |
| city | varchar(100) | VARCHAR(100) | Y | - | ✅ |
| status | varchar(20) | VARCHAR(20) | N | 'ACTIVE' | ✅ |
| created_time | datetime | TIMESTAMP | N | now() | ✅ |
| updated_time | datetime | TIMESTAMP | N | now() | ✅ |
| deleted_time | datetime | TIMESTAMP | Y | - | ✅ |

字段结论：10/10 全部对齐。


## 2.2 user_preference 表（§6.2）

| 字段 | 设计类型 | Migration 类型 | Nullable | 默认值 | 结论 |
|------|----------|----------------|----------|--------|------|
| id | bigint | BIGINT IDENTITY PK | N | - | ✅ |
| user_id | bigint | BIGINT | N | - | ✅ |
| interest | varchar(500) | VARCHAR(500) | Y | - | ✅ |
| budget | varchar(20) | VARCHAR(20) | Y | 'MEDIUM' | ✅ |
| lifestyle | varchar(500) | VARCHAR(500) | Y | - | ✅ |
| created_time | datetime | TIMESTAMP | N | now() | ✅ |
| updated_time | datetime | TIMESTAMP | N | now() | ✅ |

字段结论：7/7 全部对齐。


## 2.3 tag 表（§6.10）

| 字段 | 设计类型 | Migration 类型 | Nullable | 默认值 | 结论 |
|------|----------|----------------|----------|--------|------|
| id | bigint | BIGINT IDENTITY PK | N | - | ✅ |
| user_id | bigint | BIGINT | N | - | ✅ |
| name | varchar(50) | VARCHAR(50) | N | - | ✅ |
| type | varchar(20) | VARCHAR(20) | N | 'GENERAL' | ✅ |
| created_time | datetime | TIMESTAMP | N | now() | ✅ |

字段结论：5/5 全部对齐。


---

# 3. 索引核对（§8）

| 索引名 | 设计要求 | Migration 实现 | 结论 |
|--------|----------|----------------|------|
| uk_user_email | email 唯一（允许 NULL） | partial unique WHERE email IS NOT NULL | ✅ |
| uk_user_phone | phone 唯一（允许 NULL） | partial unique WHERE phone IS NOT NULL | ✅ |
| idx_user_status | status 索引 | CREATE INDEX | ✅ |
| uk_user_preference_user_id | user_id 唯一 | UNIQUE INDEX | ✅ |
| uk_tag_user_name_type | (user_id,name,type) 唯一 | UNIQUE INDEX | ✅ |

索引结论：5/5 全部对齐。


---

# 4. 枚举核对（§7）

| 枚举 | 设计值 | Migration 默认值 | 结论 |
|------|--------|------------------|------|
| USER_STATUS | ACTIVE / INACTIVE / BANNED | 'ACTIVE' | ✅ |
| BUDGET_LEVEL | LOW / MEDIUM / HIGH | 'MEDIUM' | ✅ |
| TAG_TYPE | INTEREST / SKILL / MOOD / GENERAL | 'GENERAL' | ✅ |

枚举结论：3/3 全部对齐。


---

# 5. 外键策略核对（§9）

- 设计要求：逻辑关联不建物理 FK
- Migration 实现：user_preference.user_id 与 tag.user_id 均未建 FOREIGN KEY 约束
- 结论：✅ 对齐


---

# 6. Gap 分析


## 6.1 password 字段缺失（待 Auth 任务处理）

user 表当前无 `password` 字段。用户登录（TASK-0102 Authentication，ADR-0006 JWT）需要密码哈希存储。

处理决策：

- password 字段的添加与 ADR-0006 JWT 认证策略耦合（哈希算法选择、是否引入 OAuth 字段等属认证决策范畴）
- 因此 password 字段的增量 Migration 归 Auth 任务，与 ADR-0006 同期落地
- TASK-0101 不在此处添加，避免在认证 ADR 尚未确定时过早锁定 schema


## 6.2 last_login_time / last_login_ip（暂不需要）

当前 DATABASE_DESIGN §6.1 未定义登录审计字段。如 Sprint 1 Auth 需要登录审计，由 Auth 任务评估是否纳入 ADR-0006 影响。TASK-0101 不扩展。


---

# 7. 审查结论

| 项 | 结论 |
|----|------|
| 字段对齐 | ✅ 22/22 全部对齐（user 10 + user_preference 7 + tag 5） |
| 索引对齐 | ✅ 5/5 全部对齐 |
| 枚举对齐 | ✅ 3/3 全部对齐 |
| 外键策略 | ✅ 逻辑关联，无物理 FK |
| 重复建表 | ✅ 无（TASK-0004 已创建，本任务未重建） |
| 增量 Migration | ❌ 本任务无需增量 Migration（password 归 Auth 任务） |

最终结论：Sprint 0 创建的三张表完全符合 User Module Domain Design，User Domain Layer / Application Service / Controller 可直接基于现有 schema 开发。password 字段由 Auth 任务（ADR-0006）落地。


---

# 8. 后续任务依赖关系

```
TASK-0101 Migration Review (本任务，Done)
   |
   +-- TASK-0102 User Domain Layer（基于现有 user / user_preference schema）
   |
   +-- Authentication（ADR-0006 JWT，增量添加 password 字段）
   |
   +-- TASK-0103 Application Service
   +-- TASK-0104 Controller + DTO
   +-- TASK-0105 Frontend
   +-- TASK-0106 Test Suite
```
