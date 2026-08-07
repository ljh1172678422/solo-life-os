# ADR-0016: Passive Sensing Consent Boundary

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.2 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §13 决策。
> 修正 DATABASE_DESIGN §9「禁止物理 FK」约束：本 ADR 的所有表间关联采用逻辑关联（无物理 FK），与现有约束一致。

---

## Decision

建立场景化数据授权边界，约束被动感知数据的采集与使用。每类敏感数据必须按**场景（scenario）+ 目的（purpose）**独立授权，用户可关闭、删除、改手动输入，禁止为「懂用户」默认持续采集。Mood 主动输入不属于权限授权范畴，单独管理。

## Data Categories and Consent Rules

### 被动感知数据（需场景化授权）

| 数据类别 | data_type | 默认状态 | 场景（scenario）示例 | 用户控制 | 持续采集限制 |
|---|---|---|---|---|---|
| **位置** | LOCATION | 关闭 | EXPLORE_BROWSE（浏览 Explore 时按需定位）/ PROPOSAL_GENERATION（为生成提案而定位）/ NEARBY_SEARCH（附近搜索） | 可关闭/改手动输入城市/删除历史 | 禁止持续定位；仅按场景按需获取 |
| **活动记录** | ACTIVITY_RECORD | 关闭 | EXPERIENCE_CONFIRM（确认体验发生）/ BEHAVIOR_LEARNING（行为学习） | 可关闭/删除历史 | 不持续采集；不用于「证明用户完成体验」 |
| **日程** | CALENDAR | 关闭 | CALENDAR_SYNC（用户主动同步日历）/ FREE_TIME_INFERENCE（空闲时间推断） | 可关闭/撤销授权/删除同步数据 | 仅用户主动同步时读取，不持续监听 |
| **健康** | HEALTH | 关闭 | MOOD_CORRELATION（健康与状态关联分析，需用户明确授权） | 可关闭/删除历史 | 不持续采集；仅在用户明确授权场景下使用 |
| **设备数据** | DEVICE_DATA | 关闭 | APP_USAGE（应用使用情况，需用户明确授权） | 可关闭/删除历史 | 不持续采集 |

### 关键约束（产品宪法 §十.2）

1. **禁止隐性监控**：不为了证明用户真的完成了体验而进行持续定位或隐性监控
2. **场景化授权**：位置、活动记录、日程、健康、设备数据必须按 scenario + purpose 单独授权
3. **可降级**：用户可关闭某类数据采集，改手动输入（如手动选择城市而非定位）
4. **可删除**：用户可删除某类数据的历史记录
5. **不用监控换确认**：体验是否发生（ExperienceOccurrence）仅由用户自愿确认，不通过监控证明

### Explore 定位的两个独立场景

- **EXPLORE_BROWSE**：用户主动打开 Explore 浏览时按需获取位置（用于附近搜索/地图展示）
- **PROPOSAL_GENERATION**：为生成位置敏感的主动提案而获取位置（需单独授权）

**产品后果声明**：若用户仅授权 EXPLORE_BROWSE 而未授权 PROPOSAL_GENERATION，则系统无法做位置敏感的主动建议，主动提案将不包含基于当前位置的机会（仅基于用户手动输入的城市或已知偏好）。这是用户可控性的必要代价，产品接受此限制。

### Mood 主动输入（不属于权限授权）

Mood 是用户主动提供的「此刻状态」输入，**不需要权限授权**（用户主动提交即视为同意使用），但需提供：

- 可选：用户可不输入
- 可撤回：用户可撤回某次输入
- 可删除：用户可删除历史输入

Mood 输入的存储与数据治理由 ADR-0019 决定（与 ai_memory 统一管理），不纳入 data_consent 表。

## Data Model

### 新增表：`data_consent`（场景化授权）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | 关联 user（逻辑关联，**无物理 FK**） |
| data_type | VARCHAR(30) | LOCATION / ACTIVITY_RECORD / CALENDAR / HEALTH / DEVICE_DATA |
| scenario | VARCHAR(50) | 场景（如 EXPLORE_BROWSE / PROPOSAL_GENERATION / NEARBY_SEARCH / CALENDAR_SYNC 等） |
| purpose | VARCHAR(200) | 授权目的说明（如「为生成基于当前位置的体验提案」） |
| granted_at | TIMESTAMP | 授权时间 |
| revoked_at | TIMESTAMP | 撤销时间（nullable，null 表示当前有效） |
| scope | VARCHAR(100) | 授权范围（如 LOCATION=city_only / precise；CALENDAR=read_only） |
| timezone | VARCHAR(50) | 用户时区（用于静默时段计算，如 Asia/Shanghai） |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

**唯一约束**：(user_id, data_type, scenario) 唯一，同一用户同一数据类型同一场景仅一条有效授权记录

### 新增表：`notification_preference`（通知偏好，含类别/场景/时区）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | 关联 user（逻辑关联，**无物理 FK**） |
| notification_category | VARCHAR(30) | 通知类别（PROPOSAL_PUSH / SAFETY_SUPPORT / SYSTEM 等） |
| scenario | VARCHAR(50) | 场景（如 HIGH_CONFIDENCE_PROPOSAL / LOW_BURDEN_QUESTION 等） |
| quiet_hours_start | TIME | 静默开始时间（用户本地时间） |
| quiet_hours_end | TIME | 静默结束时间（用户本地时间） |
| frequency_cap_per_day | INT | 每日频率上限（默认 1） |
| timezone | VARCHAR(50) | 用户时区（如 Asia/Shanghai） |
| enabled | BOOLEAN | 是否启用该类该场景通知 |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

**唯一约束**：(user_id, notification_category, scenario) 唯一

## User Module Responsibility

User Module 新增数据授权管理职责（PROJECT_CONTEXT v1.3 §7）：

- `DataConsentService`：管理 data_consent 表，提供授权/撤销/查询接口
- `NotificationPreferenceService`：管理 notification_preference 表
- 授权状态查询 API：其他模块使用敏感数据前必须查询授权状态

### API 示例

```
POST   /api/users/{userId}/data-consents              # 授权某类数据某场景
DELETE /api/users/{userId}/data-consents/{data_type}   # 撤销某类数据全部场景授权（按 data_type 撤销）
GET    /api/users/{userId}/data-consents              # 查询授权状态（含所有场景）
GET    /api/users/{userId}/data-consents?data_type=LOCATION&scenario=PROPOSAL_GENERATION  # 查询特定场景授权
DELETE /api/users/{userId}/data/{data_type}            # 删除某类数据历史

GET    /api/users/{userId}/notification-preferences    # 查询所有通知偏好
PUT    /api/users/{userId}/notification-preferences/{category}/{scenario}  # 更新特定通知偏好
```

## Reason

- **产品驱动**：产品宪法 §十.2 明确要求「位置、活动记录、日程、健康或设备数据必须按场景单独授权」「不应为了证明用户真的完成了体验而进行持续定位或隐性监控」
- **架构约束**：PROJECT_CONTEXT v1.3 §13 已确立场景化授权边界；DATABASE_DESIGN §9「禁止物理 FK」要求表间关联采用逻辑关联
- **演进约束**：上游要求覆盖位置/活动记录/日程/健康/设备数据五类，且需 purpose/scenario 粒度，不能仅按 data_type 授权

## Data Category Coverage 方案比较

针对被动感知数据类别的方案比较：

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. 仅 data_type 授权** | 按 LOCATION/CALENDAR 等类别授权，不区分场景 | 实现简单 | 无法区分「浏览时定位」与「提案生成时定位」；无法实现「关闭主动建议定位但仍可浏览」 | 数据使用场景单一时 |
| **B. data_type + scenario 授权** | 每类数据按场景独立授权 | 精细控制；符合产品宪法「按场景单独授权」 | 授权记录数增加；UI 需清晰展示 | 数据使用场景多样时（采用） |
| **C. 全局授权** | 一次授权所有数据 | 用户操作少 | 违反「按场景单独授权」；违反产品宪法 | 不适用 |

**采用方案 B**：data_type + scenario + purpose 三维度授权，符合产品宪法「按场景单独授权」要求。

## Impact

### 影响模块

- User：新增 DataConsentService / NotificationPreferenceService
- Explore：使用位置前需查询 LOCATION + 对应 scenario 授权状态（EXPLORE_BROWSE vs PROPOSAL_GENERATION）
- Today：ExperienceOccurrence 不依赖监控，仅用户自愿确认
- Mood：不需单独授权（用户主动输入），但需提供删除接口（数据治理见 ADR-0019）
- AI Platform：State Understanding 仅接收用户主动输入，不读取被动感知数据；Proposal Composer 生成位置敏感提案前需查询 PROPOSAL_GENERATION 授权

### 需要修改的文档

- DATABASE_DESIGN.md：新增 data_consent / notification_preference 表（逻辑关联，无物理 FK）（第 4 步）
- ARCHITECTURE.md：§17 Authorization 新增场景化授权（第 4 步）
- PROJECT_CONTEXT.md：§13 隐私边界已在 v1.3 完成

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_data_consent_table.sql`
- 新建 migration：`V20260807_xxx__create_notification_preference_table.sql`
- 新建 DataConsent Entity / Repository / Service / Controller
- 新建 NotificationPreference Entity / Repository / Service / Controller
- Explore LocationController 使用位置前查询授权状态（按 scenario 查询）
- 通知服务发送前查询 notification_preference（按 category + scenario 查询，含时区/静默/频率校验）

### 是否影响现有数据

- 新建表，无影响
- 现有 user 表无变化

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 data_consent / notification_preference 表（逻辑关联）
2. 第 7 步：新建 migration + Entity + Repository + Service + Controller
3. Explore 现有代码：LocationController 加授权查询前置（按 scenario）
4. 通知服务：发送前查询 notification_preference

### Follow-up ADR

- Mood 主动输入的数据治理（查看/撤回/删除/保留期限）由 ADR-0019 决定
- 推断与偏好记忆的数据治理由 ADR-0019 决定

### 验证方式

- data_consent / notification_preference 表 migration 执行成功（无物理 FK）
- DataConsentService 授权/撤销/查询接口测试通过（含 scenario + purpose）
- NotificationPreferenceService 偏好管理接口测试通过（含 category + scenario + timezone）
- Explore 使用位置前正确查询 LOCATION + EXPLORE_BROWSE 授权状态
- Proposal Composer 生成位置敏感提案前查询 LOCATION + PROPOSAL_GENERATION 授权
- 用户仅授权 EXPLORE_BROWSE 时，主动提案不包含基于当前位置的机会（测试覆盖）
- 通知服务发送前正确查询 notification_preference（含时区/静默/频率校验）
- 用户可删除某类数据历史
