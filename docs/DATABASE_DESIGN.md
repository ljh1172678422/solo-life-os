# Solo Life OS 数据库设计

Version: 2.1

Status: Planning

Last Update: 2026-07-28


> 本文档定义 Solo Life OS 的数据基线，所有 Entity / Repository / Migration 必须遵守。
> 与 ARCHITECTURE §3 Shared Domain、§22 Data Ownership、§15 NFR 完全对齐。


---

# 1. Design Principles


- 先设计数据模型，再开发页面（PROJECT_CONTEXT §14 原则 1）
- 所有模块共享核心实体，禁止重复定义（ARCHITECTURE §3）
- 每张表有唯一 Owner 模块，跨模块访问经 Domain API（ARCHITECTURE §22）
- 统一审计字段，优先软删除（CODE_RULES.md）
- 逻辑关联，不建立数据库 FK（详见 §9）
- 枚举禁止自由字符串，必须显式定义（详见 §7）


---

# 2. Naming Convention


| 项 | 规范 | 示例 |
|----|------|------|
| 表名 | snake_case 单数 | user / goal / mood_record |
| 字段名 | snake_case | created_time / user_id |
| 主键 | id，bigint，自增 | id |
| 外键 | `<表>_id` | user_id / location_id |
| 时间字段 | `_time` 后缀 | created_time / updated_time |
| 布尔字段 | `is_` 前缀 | is_deleted / is_public |
| 枚举字段 | 字符串而非数字 | status / type |


---

# 3. Shared Entities


所有模块共享以下核心 Entity，与 ARCHITECTURE §3 一致：


| Entity | 说明 | Owner 模块 |
|--------|------|-----------|
| User | 用户身份 | User |
| Preference | 用户偏好 | User |
| Activity | 生活事件 | Today |
| Goal | 成长目标 | Growth |
| Mood | 情绪状态 | Mood |
| Memory | AI 长期记忆 | AI |
| Location | 地理位置 | Explore |
| Tag | 通用标签 | User |


---

# 4. Entity Ownership


每张表有唯一 Owner 模块，跨模块写操作必须经 Owner 的 Domain API（ARCHITECTURE §22）：


| Table | Owner Module | 其他模块访问方式 |
|-------|------------|---------------|
| user | User | Domain API |
| user_preference | User | Domain API |
| goal | Growth | Domain API |
| mood_record | Mood | 事件订阅 + Domain API |
| activity | Today | 事件订阅 + Domain API |
| daily_plan | Today | Domain API |
| favorite | User | Domain API |
| ai_memory | AI | Domain API |
| ai_conversation | AI | Domain API |
| location | Explore | Domain API |
| tag | User | Domain API |
| community_event | Community | Domain API |
| registration | Community | Domain API |


---

# 5. ER Diagram


```
                User
                 │
       ┌─────────┼─────────┐
       │         │         │
       ▼         ▼         ▼
  Preference  Favorite   DailyPlan
                            │
                            ▼
                         Activity
                            │
                            ▼
                         Location

   Goal          Mood          Tag
    │              │
    └──────┬───────┘
           ▼
        Memory (AI)
```


---

# 6. Table Design


## 6.1 user

Owner: User Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK, 自增 |
| nickname | varchar(50) | N | - | 用户昵称 |
| avatar | varchar(500) | Y | - | 头像 OSS 地址 |
| email | varchar(100) | Y | - | 邮箱（登录凭证之一） |
| phone | varchar(20) | Y | - | 手机号（登录凭证之一） |
| city | varchar(100) | Y | - | 当前城市 |
| status | varchar(20) | N | 'ACTIVE' | 用户状态，见 §7 USER_STATUS |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间，NULL 表示未删除 |


## 6.2 user_preference

Owner: User Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| interest | varchar(500) | Y | - | 兴趣标签，逗号分隔 |
| budget | varchar(20) | Y | 'MEDIUM' | 预算等级，见 §7 BUDGET_LEVEL |
| lifestyle | varchar(500) | Y | - | 生活方式描述 |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |


## 6.3 daily_plan

Owner: Today Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| date | date | N | - | 计划日期 |
| status | varchar(20) | N | 'PLANNING' | 计划状态，见 §7 PLAN_STATUS |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间 |


## 6.4 activity

Owner: Today Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| title | varchar(200) | N | - | 活动标题 |
| type | varchar(20) | N | 'OTHER' | 活动类型，见 §7 ACTIVITY_TYPE |
| location_id | bigint | Y | - | 关联 location.id（原 location 字段已替换） |
| start_time | datetime | N | - | 开始时间 |
| end_time | datetime | Y | - | 结束时间，可空 |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间 |


## 6.5 mood_record

Owner: Mood Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| mood | varchar(20) | N | 'NORMAL' | 心情，见 §7 MOOD_TYPE |
| tags | varchar(500) | Y | - | 情绪标签，逗号分隔 |
| note | text | Y | - | 手动备注 |
| created_time | datetime | N | now() | 创建时间 |


注意：情绪记录不可编辑，不可软删除，保留原始数据用于趋势分析。


## 6.6 goal

Owner: Growth Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| name | varchar(200) | N | - | 目标名称 |
| type | varchar(20) | N | 'OTHER' | 目标类型，见 §7 GOAL_TYPE |
| progress | int | N | 0 | 进度 0-100 |
| status | varchar(20) | N | 'ACTIVE' | 目标状态，见 §7 GOAL_STATUS |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间 |


## 6.7 favorite

Owner: User Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| target_type | varchar(20) | N | - | 收藏目标类型，见 §7 FAVORITE_TARGET |
| target_id | bigint | N | - | 收藏目标 ID |
| created_time | datetime | N | now() | 创建时间 |


约束：UNIQUE(user_id, target_type, target_id)，防止重复收藏


## 6.8 ai_memory

Owner: AI Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| memory_type | varchar(20) | N | 'GENERAL' | 记忆类型，见 §7 MEMORY_TYPE |
| source | varchar(20) | N | 'SYSTEM' | 来源，见 §7 MEMORY_SOURCE |
| summary | varchar(500) | N | - | 摘要（用于检索展示） |
| content | text | N | - | 完整内容 |
| importance | int | N | 50 | 重要性 0-100 |
| embedding_id | varchar(100) | Y | - | Vector DB 中的向量 ID |
| visibility | varchar(20) | N | 'PRIVATE' | 可见性，见 §7 MEMORY_VISIBILITY |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间 |


注意：embedding_id 用于关联 Vector DB，向量本身不入 PostgreSQL（ARCHITECTURE §10）


## 6.9 location

Owner: Explore Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| name | varchar(200) | N | - | 地点名称 |
| address | varchar(500) | Y | - | 详细地址 |
| city | varchar(100) | N | - | 所属城市 |
| latitude | decimal(10,7) | N | - | 纬度 |
| longitude | decimal(10,7) | N | - | 经度 |
| type | varchar(20) | Y | 'OTHER' | 地点类型，见 §7 LOCATION_TYPE |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |


## 6.10 tag

Owner: User Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| name | varchar(50) | N | - | 标签名 |
| type | varchar(20) | N | 'GENERAL' | 标签类型，见 §7 TAG_TYPE |
| created_time | datetime | N | now() | 创建时间 |


约束：UNIQUE(user_id, name, type)


## 6.11 community_event

Owner: Community Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| title | varchar(200) | N | - | 活动标题 |
| description | text | Y | - | 活动描述 |
| location_id | bigint | Y | - | 关联 location.id（复用 Explore） |
| organizer_id | bigint | N | - | 组织者 user.id |
| start_time | datetime | N | - | 开始时间 |
| end_time | datetime | Y | - | 结束时间 |
| capacity | int | Y | - | 容量上限 |
| status | varchar(20) | N | 'DRAFT' | 活动状态，见 §7 COMMUNITY_EVENT_STATUS |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |
| deleted_time | datetime | Y | - | 软删除时间 |


注意：community_event 是社会活动，与 activity（生活事件）是不同领域实体，禁止复用 activity 表（ADR-0011）。


## 6.12 registration

Owner: Community Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| event_id | bigint | N | - | 关联 community_event.id |
| user_id | bigint | N | - | 报名用户 user.id |
| status | varchar(20) | N | 'REGISTERED' | 报名状态，见 §7 REGISTRATION_STATUS |
| created_time | datetime | N | now() | 创建时间 |
| updated_time | datetime | N | now() | 更新时间 |


约束：UNIQUE(event_id, user_id)，防止重复报名


## 6.13 ai_conversation

Owner: AI Module

| 字段 | 类型 | Nullable | 默认值 | 描述 |
|------|------|---------|--------|------|
| id | bigint | N | - | PK |
| user_id | bigint | N | - | 关联 user.id |
| agent_type | varchar(20) | N | - | Agent 类型，见 §7 AGENT_TYPE |
| role | varchar(20) | N | - | 对话角色，见 §7 CONVERSATION_ROLE |
| content | text | N | - | 消息内容 |
| token_count | int | Y | - | Token 消耗（仅 Assistant 消息） |
| created_time | datetime | N | now() | 创建时间 |


注意：ai_conversation 记录短期对话上下文，与 ai_memory（长期记忆）互补。Memory 是提炼后的持久知识，Conversation 是原始交互流。


---

# 7. Enum Definition


所有枚举禁止使用自由字符串，必须在此处显式定义。


## USER_STATUS

- ACTIVE 正常
- INACTIVE 未激活
- BANNED 封禁


## BUDGET_LEVEL

- LOW 低预算
- MEDIUM 中等
- HIGH 高预算


## PLAN_STATUS

- PLANNING 规划中
- ONGOING 进行中
- COMPLETED 已完成
- CANCELLED 已取消


## ACTIVITY_TYPE

- WORK 工作
- LEISURE 休闲
- SPORT 运动
- STUDY 学习
- SOCIAL 社交
- EXPLORE 探索
- REST 休息
- OTHER 其他


## MOOD_TYPE

- HAPPY 开心
- NORMAL 平静
- SAD 低落
- ANXIOUS 焦虑
- EXCITED 兴奋
- CALM 平和
- ANGRY 愤怒
- TIRED 疲惫


## GOAL_TYPE

- LEARNING 学习
- FITNESS 健身
- READING 阅读
- CAREER 职业
- LIFESTYLE 生活方式
- SOCIAL 社交
- OTHER 其他


## GOAL_STATUS

- ACTIVE 进行中
- PAUSED 暂停
- COMPLETED 已完成
- ARCHIVED 归档


## FAVORITE_TARGET

- LOCATION 地点
- ACTIVITY 活动
- ROUTE 路线


## MEMORY_TYPE

- PREFERENCE 偏好
- BEHAVIOR 行为
- EMOTION 情绪
- GOAL 目标
- EVENT 事件
- GENERAL 通用


## MEMORY_SOURCE

- AI AI 推断
- USER 用户明确告知
- SYSTEM 系统记录


## MEMORY_VISIBILITY

- PRIVATE 仅用户自己可见
- PUBLIC 可用于匿名聚合分析


## LOCATION_TYPE

- RESTAURANT 餐饮
- CAFE 咖啡
- PARK 公园
- MUSEUM 博物馆
- SHOPPING 购物
- SPORT 运动
- ENTERTAINMENT 娱乐
- SCENIC 景点
- OTHER 其他


## TAG_TYPE

- INTEREST 兴趣
- SKILL 技能
- MOOD 情绪
- GENERAL 通用


## COMMUNITY_EVENT_STATUS

- DRAFT 草稿
- OPEN 报名中
- CLOSED 报名截止
- CANCELLED 已取消
- COMPLETED 已结束


## REGISTRATION_STATUS

- REGISTERED 已报名
- CANCELLED 已取消
- ATTENDED 已签到


## AGENT_TYPE

- PLANNER 规划
- RECOMMENDATION 推荐
- EMOTION 情绪
- STORY 故事
- ASSISTANT 通用助手


## CONVERSATION_ROLE

- USER 用户消息
- ASSISTANT AI 回复
- SYSTEM 系统消息


---

# 8. Index Strategy


按 §15 NFR 性能指标设计索引。命名：`idx_<表>_<字段>`。


| 表 | 索引 | 说明 |
|----|------|------|
| user | uk_email | UNIQUE，登录查询 |
| user | uk_phone | UNIQUE，登录查询 |
| user | idx_status | 用户列表筛选 |
| user_preference | uk_user_id | UNIQUE，一用户一偏好 |
| daily_plan | idx_user_date | (user_id, date)，查询用户某日计划 |
| activity | idx_location | location_id，按地点查活动 |
| activity | idx_start_time | start_time，时间范围查询 |
| mood_record | idx_user_created | (user_id, created_time)，情绪趋势 |
| goal | idx_user_status | (user_id, status)，用户活跃目标 |
| favorite | uk_user_target | UNIQUE(user_id, target_type, target_id) |
| ai_memory | idx_user_type | (user_id, memory_type) |
| ai_memory | idx_user_importance | (user_id, importance DESC) |
| location | idx_city | city，按城市查询 |
| location | idx_lat_lng | (latitude, longitude)，地理范围 |
| tag | uk_user_name_type | UNIQUE(user_id, name, type) |
| community_event | idx_organizer | organizer_id，组织者活动列表 |
| community_event | idx_status_start | (status, start_time)，按状态查进行中活动 |
| community_event | idx_location | location_id，按地点查活动 |
| registration | uk_event_user | UNIQUE(event_id, user_id)，防重复报名 |
| registration | idx_user | user_id，用户报名记录 |
| ai_conversation | idx_user_agent | (user_id, agent_type, created_time)，按 Agent 查对话 |
| ai_conversation | idx_user_created | (user_id, created_time)，按时间查对话 |


---

# 9. Constraint Strategy


## 外键策略


采用**逻辑关联，不建立数据库 FK**：


- 所有 `*_id` 字段在应用层校验关联
- 原因：互联网项目常需分库分表，物理 FK 阻碍迁移
- 删除策略：父表软删除，子表保留 `user_id` 等引用，查询时 JOIN 过滤


禁止：


- 在数据库层建立 FOREIGN KEY 约束
- 在应用层跳过关联校验


## 唯一约束


| 表 | 唯一约束 |
|----|---------|
| user | email（允许 NULL，非 NULL 时唯一） |
| user | phone（同上） |
| user_preference | user_id |
| favorite | (user_id, target_type, target_id) |
| tag | (user_id, name, type) |
| registration | (event_id, user_id) |


## 非空约束


所有 `id` / `user_id` / `created_time` / 关键业务字段必须 NOT NULL。


---

# 10. Migration Rule


数据库迁移由 Backend Agent 执行（AGENTS.md §3）。


命名规范：


```
V<日期>_<序号>__<描述>.sql

示例：
V20260728_001__create_user_table.sql
V20260728_002__create_daily_plan_table.sql
V20260729_001__add_avatar_to_user.sql
```


存放目录：`database/migrations/`


规则：


- 一次迁移一个文件，禁止合并多个不相关变更
- 迁移必须幂等（可重复执行不报错）
- 破坏性变更（删字段 / 改类型）必须先写 ADR
- 禁止直接修改已发布 migration，新增变更写新文件


---

# 11. Version History


| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-28 | 初始版本，定义 8 张核心表 |
| v2.0 | 2026-07-28 | 全量升级：增加 Owner / ER 图 / 字段说明 / 枚举 / 索引 / 约束 / 迁移规则；修正 Activity.location → location_id；Favorite 增加 UNIQUE；Memory 扩展 5 字段 |
| v2.1 | 2026-07-28 | 修正 Activity Owner Today/Explore → Today（解决唯一 Owner 冲突）；新增 community_event / registration / ai_conversation 三表；新增 5 类枚举 + 7 个索引 + registration 唯一约束 |


---

# 12. 与其他文档的对齐


| 文档 | 对齐点 |
|------|--------|
| PROJECT_CONTEXT §12 | 数据资产战略（User/Preference/Activity/Mood/Goal/Memory） |
| ARCHITECTURE §3 | Shared Domain 8 个核心 Entity |
| ARCHITECTURE §10 | PostgreSQL / Vector DB / OSS 存储边界 |
| ARCHITECTURE §15 | NFR 性能指标决定索引策略 |
| ARCHITECTURE §22 | Data Ownership 决定表归属 |
| CODE_RULES.md | 软删除优先、统一审计字段 |
| AGENTS.md §3 | Backend Agent 负责 database/migrations/ |
