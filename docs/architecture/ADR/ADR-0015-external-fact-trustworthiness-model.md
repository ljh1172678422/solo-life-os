# ADR-0015: External Fact Trustworthiness Model

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.1 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §10、§12 决策。

---

## Decision

建立外部事实可信度模型，记录事实来源、采集时间、有效期和可信度，支撑 Opportunity Discovery 和 Proposal Composer 的置信度判断。

### 事实类型分类（产品宪法 §十.1）

| 类别 | 含义 | 示例 | 存储要求 |
|---|---|---|---|
| **事实（Fact）** | 可追溯至来源与更新时间的客观信息 | 天气、距离、营业时间、活动场次、花期、库存 | 记录来源、采集时间、有效期、可信度 |
| **推断（Inference）** | 表达概率与不确定性的主观判断 | 「人流可能较少」「适合安静阅读」 | 记录推断依据、概率/置信度、推断时间 |
| **偏好记忆（Preference Memory）** | 用户主动表达或行为反馈的偏好 | 「不喜欢吵闹的地方」 | 记录来源（主动表达/行为推断）、时间、可撤回 |

## Data Model

### 新增表：`external_fact`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| fact_type | VARCHAR(50) | WEATHER / BUSINESS_HOURS / EVENT_SCHEDULE / DISTANCE / INVENTORY / SEASONAL / OTHER |
| source | VARCHAR(200) | 数据来源（API 名称、商家自报、用户反馈等） |
| source_type | VARCHAR(20) | OFFICIAL_API / MERCHANT_REPORTED / USER_REPORTED / INFERRED |
| fetched_at | TIMESTAMP | 采集时间 |
| valid_until | TIMESTAMP | 有效期（过期后不可作为高置信度依据） |
| confidence | DECIMAL(3,2) | 可信度 0.00-1.00 |
| payload_json | JSONB | 事实内容（结构化，如 {open:09:00, close:22:00}） |
| location_id | BIGINT FK | 关联地点（nullable，天气类可关联城市） |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

### location 表扩展

现有 location 表新增字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| business_hours | JSONB | 营业时间（结构化，如 {mon:{open:09:00,close:22:00}}） |
| verified_at | TIMESTAMP | 营业时间/地址最后核验时间 |

### ai_memory 表区分（Sprint 5 落地）

ai_memory 表（待建）需区分三类记忆：

| memory_category | 含义 | 示例 |
|---|---|---|
| FACT | 事实记忆（来自 external_fact 的引用） | 「该咖啡馆 22:00 关门」 |
| INFERENCE | 推断记忆 | 「用户偏好安静环境」 |
| PREFERENCE | 偏好记忆（用户主动表达） | 「我不喜欢人多」 |

## Confidence Calculation

Opportunity Discovery 的候选机会可信度计算：

```
opportunity_confidence = fact_confidence × time_decay × source_weight

fact_confidence: external_fact.confidence 字段
time_decay: 1.0（valid_until 内）→ 0.5（过期 1 小时内）→ 0.0（过期超 1 小时）
source_weight: OFFICIAL_API=1.0, MERCHANT_REPORTED=0.8, USER_REPORTED=0.6, INFERRED=0.4
```

Life Curator 基于候选机会可信度 + Motivation Engine 评分决定置信度门控（ADR-0014）。

## Impact

### 影响模块

- Explore：location 表扩展、external_fact 表关联
- AI Platform：Opportunity Discovery 依赖 external_fact 计算 可信度
- Today：ExperienceProposal 输出区分 fact/inference/preference 字段

### 需要修改的文档

- DATABASE_DESIGN.md：新增 external_fact 表、location 表扩展、ai_memory 表 memory_category 字段（第 4 步）
- ARCHITECTURE.md：§18 外部集成（天气/营业时间等数据源）（第 4 步）
- PROJECT_CONTEXT.md：§12 数据资产新增 ExternalFact（已在 v1.3 完成）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_external_fact_table.sql`
- 新建 migration：`V20260807_xxx__alter_location_add_business_hours.sql`
- 新建 ExternalFact Entity / Repository / Service
- location Entity 新增 businessHours / verifiedAt 字段
- Sprint 5 AI Platform：Opportunity Discovery 可信度计算逻辑

### 是否影响现有数据

- location 表：已有数据，新增字段需设 nullable，不破坏现有数据
- external_fact 表：新建，无影响

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 external_fact 表设计、location 表扩展
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：Opportunity Discovery 可信度计算逻辑

### Follow-up ADR

- 无（本 ADR 完整定义事实可信度模型）
- 未来若需引入新数据源，无需新 ADR，仅扩展 external_fact.fact_type 枚举

### 验证方式

- external_fact 表 migration 执行成功
- location 表新增字段 nullable，现有数据不受影响
- Opportunity Discovery 可信度计算逻辑实现并测试
- ExperienceProposal 输出区分 fact/inference/preference 字段
