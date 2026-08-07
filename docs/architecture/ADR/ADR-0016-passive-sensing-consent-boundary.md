# ADR-0016: Passive Sensing Consent Boundary

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.2 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §13 决策。

---

## Decision

建立场景化数据授权边界，约束被动感知数据的采集与使用。每类敏感数据必须独立授权，用户可关闭、删除、改手动输入，禁止为「懂用户」默认持续采集。

## Data Categories and Consent Rules

| 数据类别 | 默认状态 | 授权粒度 | 用户控制 | 持续采集限制 |
|---|---|---|---|---|
| **位置** | 关闭 | 单独授权 | 可关闭/改手动输入/删除历史 | 禁止持续定位；仅在用户主动打开 Explore 时按需获取 |
| **日历** | 关闭 | 单独授权 | 可关闭/撤销授权/删除同步数据 | 仅用户主动同步时读取，不持续监听 |
| **运动量** | 关闭 | 单独授权 | 可关闭/删除历史 | 不持续采集；不用于「证明用户完成体验」 |
| **应用使用情况** | 关闭 | 单独授权 | 可关闭/删除历史 | 不持续采集 |
| **此刻状态输入（Mood）** | 关闭 | 不需单独授权（用户主动输入） | 可选/可撤回/可删除 | 不主动采集，仅用户主动提交 |

### 关键约束（产品宪法 §十.2）

1. **禁止隐性监控**：不为了证明用户真的完成了体验而进行持续定位或隐性监控
2. **场景化授权**：位置、日历、运动量、应用使用情况必须按场景单独授权
3. **可降级**：用户可关闭某类数据采集，改手动输入（如手动选择城市而非定位）
4. **可删除**：用户可删除某类数据的历史记录
5. **不用监控换确认**：体验是否发生（ExperienceOccurrence）仅由用户自愿确认，不通过监控证明

## Data Model

### 新增表：`data_consent`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK | 关联 user |
| data_type | VARCHAR(30) | LOCATION / CALENDAR / ACTIVITY / APP_USAGE / MOOD_INPUT |
| granted_at | TIMESTAMP | 授权时间 |
| revoked_at | TIMESTAMP | 撤销时间（nullable，null 表示当前有效） |
| scope | VARCHAR(100) | 授权范围（如 LOCATION=city_only / precise） |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

### 新增表：`notification_preference`

（与 ADR-0014 主动通知边界配合）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| channel | VARCHAR(20) | PUSH / IN_APP |
| quiet_hours_start | TIME | 静默开始时间 |
| quiet_hours_end | TIME | 静默结束时间 |
| frequency_cap_per_day | INT | 每日频率上限（默认 1） |
| enabled | BOOLEAN | 是否启用主动通知 |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

## User Module Responsibility

User Module 新增数据授权管理职责（PROJECT_CONTEXT v1.3 §7）：

- `DataConsentService`：管理 data_consent 表，提供授权/撤销/查询接口
- `NotificationPreferenceService`：管理 notification_preference 表
- 授权状态查询 API：其他模块使用敏感数据前必须查询授权状态

### API 示例

```
POST   /api/users/{userId}/data-consents              # 授权某类数据
DELETE /api/users/{userId}/data-consents/{dataType}    # 撤销授权
GET    /api/users/{userId}/data-consents              # 查询授权状态
DELETE /api/users/{userId}/data/{dataType}             # 删除某类数据历史

GET    /api/users/{userId}/notification-preference     # 查询通知偏好
PUT    /api/users/{userId}/notification-preference     # 更新通知偏好
```

## Impact

### 影响模块

- User：新增 DataConsentService / NotificationPreferenceService
- Explore：使用位置前需查询 LOCATION 授权状态
- Today：ExperienceOccurrence 不依赖监控，仅用户自愿确认
- Mood：不需单独授权（用户主动输入），但需提供删除接口
- AI Platform：State Understanding 仅接收用户主动输入，不读取被动感知数据

### 需要修改的文档

- DATABASE_DESIGN.md：新增 data_consent / notification_preference 表（第 4 步）
- ARCHITECTURE.md：§17 Authorization 新增场景化授权（第 4 步）
- PROJECT_CONTEXT.md：§13 隐私边界已在 v1.3 完成

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_data_consent_table.sql`
- 新建 migration：`V20260807_xxx__create_notification_preference_table.sql`
- 新建 DataConsent Entity / Repository / Service / Controller
- 新建 NotificationPreference Entity / Repository / Service / Controller
- Explore LocationController 使用位置前查询授权状态

### 是否影响现有数据

- 新建表，无影响
- 现有 user 表无变化

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 data_consent / notification_preference 表
2. 第 7 步：新建 migration + Entity + Repository + Service + Controller
3. Explore 现有代码：LocationController 加授权查询前置

### Follow-up ADR

- 无

### 验证方式

- data_consent / notification_preference 表 migration 执行成功
- DataConsentService 授权/撤销/查询接口测试通过
- NotificationPreferenceService 偏好管理接口测试通过
- Explore 使用位置前正确查询 LOCATION 授权状态
- 用户可删除某类数据历史
