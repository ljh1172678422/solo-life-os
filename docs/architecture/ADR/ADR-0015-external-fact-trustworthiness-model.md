# ADR-0015: External Fact Trustworthiness Model

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.1 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §10、§12 决策。

---

## Decision

建立外部事实可信度模型，**真正分离事实（Fact）、推断（Inference）和偏好记忆（Preference Memory）三类信息**，各自独立存储、独立溯源、独立计算可信度。不同事实类型采用不同的时效衰减规则。提案依赖多事实时采用聚合公式计算综合可信度。

## Three-Category Separation

### 分类定义（产品宪法 §十.1）

| 类别 | 含义 | 示例 | Owner | 存储 | 溯源要求 |
|---|---|---|---|---|---|
| **事实（Fact）** | 可追溯至来源与更新时间的客观信息 | 天气、距离、营业时间、活动场次、花期、库存 | Explore Module（地点相关）/ AI Platform（天气等） | `external_fact` 表 | source / source_type / fetched_at / valid_until / confidence |
| **推断（Inference）** | 表达概率与不确定性的主观判断 | 「人流可能较少」「适合安静阅读」 | AI Platform | `ai_memory`（memory_category=INFERENCE） | inference_basis / probability / inferred_at / 撤回机制 |
| **偏好记忆（Preference）** | 用户主动表达或行为反馈的偏好 | 「不喜欢吵闹的地方」 | AI Platform（数据治理见 ADR-0019） | `ai_memory`（memory_category=PREFERENCE） | 来源（主动表达/行为推断）/ 时间 / 可撤回 / 可删除 |

### 关键约束

1. **事实与推断不混存**：事实存 `external_fact`，推断存 `ai_memory`（INFERENCE），禁止 `external_fact.source_type=INFERRED`
2. **偏好记忆独立**：偏好记忆存 `ai_memory`（PREFERENCE），不与事实/推断混存
3. **数据治理**：推断与偏好记忆的查看/修改/删除/撤回/派生数据清理/保留期限由 ADR-0019 决定

## Data Model

### 新增表：`external_fact`（仅存事实，不存推断）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| fact_type | VARCHAR(50) | WEATHER / BUSINESS_HOURS / EVENT_SCHEDULE / DISTANCE / INVENTORY / SEASONAL / OTHER |
| source | VARCHAR(200) | 数据来源（API 名称、商家自报、用户反馈等） |
| source_type | VARCHAR(20) | OFFICIAL_API / MERCHANT_REPORTED / USER_REPORTED（**不含 INFERRED**） |
| fetched_at | TIMESTAMP | 采集时间 |
| valid_until | TIMESTAMP | 有效期（过期后不可作为高置信度依据） |
| confidence | DECIMAL(3,2) | 来源可信度 0.00-1.00（基于 source_type，见 §Source Weight） |
| payload_json | JSONB | 事实内容（结构化，如 {open:09:00, close:22:00}） |
| location_id | BIGINT | 关联地点（逻辑关联，**无物理 FK**，nullable，天气类可关联城市） |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

### location 表扩展（SSOT 定义）

现有 location 表新增字段，**明确 SSOT 关系**：

| 字段 | 类型 | SSOT 角色 | 说明 |
|---|---|---|---|
| business_hours | JSONB | **投影缓存**（非 SSOT） | 营业时间的缓存投影，SSOT 为 external_fact（fact_type=BUSINESS_HOURS）；缓存过期后从 external_fact 刷新 |
| verified_at | TIMESTAMP | 元数据 | business_hours 缓存最后刷新时间 |

**SSOT 规则**：
- `business_hours` 在 location 表是投影缓存，供快速查询使用
- 真实营业时间的 SSOT 是 `external_fact`（fact_type=BUSINESS_HOURS）
- 缓存与 SSOT 不一致时，以 `external_fact` 为准
- 缓存通过定时任务或查询时按需刷新

### ai_memory 表扩展（Sprint 5 落地，数据治理见 ADR-0019）

ai_memory 表（待建）区分三类记忆：

| memory_category | 含义 | 示例 | 与 external_fact 关系 |
|---|---|---|---|
| FACT | 事实记忆（引用 external_fact） | 「该咖啡馆 22:00 关门」 | 引用 external_fact.id，不重复存储事实 |
| INFERENCE | 推断记忆 | 「用户偏好安静环境」「人流可能较少」 | 独立存储，不混入 external_fact |
| PREFERENCE | 偏好记忆（用户主动表达） | 「我不喜欢人多」 | 独立存储，可撤回/可删除 |

## Source Weight（仅反映来源可信度，不重复计算）

`external_fact.confidence` 字段基于 `source_type` 设定（**仅反映来源可信度，不与 fact_confidence 重复计算**）：

| source_type | confidence | 说明 |
|---|---|---|
| OFFICIAL_API | 1.00 | 官方 API（如天气 API、商家官方 API） |
| MERCHANT_REPORTED | 0.80 | 商家自报（如商家在后台录入营业时间） |
| USER_REPORTED | 0.60 | 用户反馈（如用户报告营业时间有误） |

## Time Decay（按事实类型差异化）

不同事实类型采用不同的时效衰减规则（**不统一用「过期 1 小时降 0.5」**）：

| fact_type | valid_until 建议有效期 | 过期后 time_decay | 理由 |
|---|---|---|---|
| WEATHER | 3 小时 | 过期即 0.0（不可用） | 天气变化快，过期数据无意义 |
| BUSINESS_HOURS | 30 天 | 过期 7 天内 0.5，超 7 天 0.0 | 营业时间较稳定，但季节性调整 |
| EVENT_SCHEDULE | 活动开始前 | 活动开始后 0.0 | 活动结束即无效 |
| DISTANCE | 1 天 | 过期即 0.8（距离变化慢） | 距离基本不变，轻微衰减 |
| INVENTORY | 1 小时 | 过期即 0.3 | 库存变化快，过期低置信 |
| SEASONAL | 季节周期 | 季节内 1.0，季节外 0.0 | 花期等季节性事实 |

## Confidence Aggregation（多事实聚合）

一份提案依赖多个事实时，采用**加权最小值聚合**（保守策略，任一低置信度事实拉低整体）：

```
proposal_fact_confidence = min(fact_confidence_i × time_decay_i) × aggregation_weight

aggregation_weight: 0.9（多事实聚合时的轻微折扣，反映聚合不确定性）
```

**理由**：采用 min 而非 avg，因为产品宪法 §九要求「克制」，任一关键事实低置信度时整个提案应降级。

### 示例

提案「去 XX 咖啡馆」依赖：
- BUSINESS_HOURS（confidence=0.80, time_decay=1.0）→ 0.80
- WEATHER（confidence=1.00, time_decay=1.0）→ 1.00
- DISTANCE（confidence=1.00, time_decay=0.8）→ 0.80

`proposal_fact_confidence = min(0.80, 1.00, 0.80) × 0.9 = 0.72`

Life Curator 基于 proposal_fact_confidence + Motivation Engine 评分决定置信度门控（ADR-0014）。

## Reason

- **产品驱动**：产品宪法 §十.1 明确要求「事实可追溯至来源与更新时间」「推断应表达概率与不确定性」；混淆事实与推断会破坏可解释性与用户信任
- **架构约束**：PROJECT_CONTEXT v1.3 §12 已区分三类数据对象；external_fact 仅存事实，推断与偏好归 AI 侧 ai_memory（ADR-0019 数据治理）
- **演进约束**：不同事实类型时效差异显著（天气 3 小时 vs 营业时间 30 天），统一衰减规则不合理

## SSOT 方案比较

针对 `business_hours` 存储位置的方案比较：

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. location 表为 SSOT** | business_hours 存 location 表，external_fact 不存 | 查询简单 | 无法记录来源/采集时间/有效期；无法支撑可信度计算 | 不需要可信度时 |
| **B. external_fact 为 SSOT，location 为缓存** | business_hours SSOT 在 external_fact，location 存投影缓存 | 可追溯来源；支撑可信度；缓存提升查询性能 | 需维护缓存一致性 | 需要可信度模型时（采用） |
| **C. 仅 external_fact 存储** | business_hours 仅存 external_fact，location 不缓存 | 无一致性问题 | 每次查询需 JOIN external_fact，性能差 | 数据量极小时 |

**采用方案 B**：external_fact 为 SSOT，location.business_hours 为投影缓存，缓存通过定时任务或查询时按需刷新。

## Impact

### 影响模块

- Explore：location 表扩展（投影缓存）、external_fact 表关联
- AI Platform：Opportunity Discovery 依赖 external_fact 计算 可信度；推断与偏好记忆通过 Memory Layer 读写 ai_memory
- Today：ExperienceProposal 输出区分 fact/inference/preference 字段

### 需要修改的文档

- DATABASE_DESIGN.md：新增 external_fact 表、location 表扩展、ai_memory 表 memory_category 字段（第 4 步）
- ARCHITECTURE.md：§18 外部集成（天气/营业时间等数据源）（第 4 步）
- PROJECT_CONTEXT.md：§12 数据资产新增 ExternalFact（已在 v1.3 完成）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_external_fact_table.sql`
- 新建 migration：`V20260807_xxx__alter_location_add_business_hours.sql`
- 新建 ExternalFact Entity / Repository / Service
- location Entity 新增 businessHours（投影缓存）/ verifiedAt 字段
- 缓存刷新逻辑（定时任务或查询时按需）
- Sprint 5 AI Platform：Opportunity Discovery 可信度计算逻辑（含差异化 time_decay + 多事实聚合）

### 是否影响现有数据

- location 表：已有数据，新增字段需设 nullable，不破坏现有数据
- external_fact 表：新建，无影响

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 external_fact 表设计、location 表扩展（明确 SSOT 与缓存关系）
2. 第 7 步：新建 migration + Entity + Repository + 缓存刷新逻辑
3. Sprint 5：Opportunity Discovery 可信度计算逻辑（含差异化 time_decay + 多事实聚合）

### Follow-up ADR

- 推断与偏好记忆的数据治理（查看/修改/删除/撤回/派生数据清理/保留期限）由 ADR-0019 决定
- 未来若需引入新数据源，无需新 ADR，仅扩展 external_fact.fact_type 枚举

### 验证方式

- external_fact 表 migration 执行成功
- external_fact.source_type 不含 INFERRED（约束检查）
- location 表新增字段 nullable，现有数据不受影响
- business_hours 缓存与 external_fact SSOT 一致性测试通过
- 差异化 time_decay 各 fact_type 测试通过
- 多事实聚合公式测试通过（min × aggregation_weight）
- Opportunity Discovery 可信度计算逻辑实现并测试
- ExperienceProposal 输出区分 fact/inference/preference 字段
