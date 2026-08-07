# ADR-0019: LifeResponseMap / ai_memory Ownership and Data Governance

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.1、§十一、§十三 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §6.1、§12、§13.5 决策。
> 产品宪法 §十.1：「偏好记忆应允许用户查看、修改和删除。」「Solo 不能把一次行为永久解释为用户偏好。」
> 产品宪法 §十一：LifeResponseMap 是「什么事情，在什么状态、时间、天气、地点和行动成本下，用户更愿意开始，并在事后认为值得。」

---

## Decision

确立 `LifeResponseMap`（个人生活反应地图）与 `ai_memory` 的所有权、数据模型、数据治理规则。

**LifeResponseMap 是基于 ProposalDecision、ExperienceOccurrence、ExperienceFeedback 和上下文快照形成的派生读模型**，不是 ai_memory 中 PREFERENCE 的聚合视图。ai_memory 保存经确认的偏好和推断，但**不等同于** LifeResponseMap。一次行为不得永久固化为偏好；用户拥有查看、修改、删除、撤回的完整控制权；撤回后派生数据必须基于数据血缘清理；明确保留期限含硬删除/匿名化。

## LifeResponseMap 与 ai_memory 的关系

### LifeResponseMap（派生读模型，不是 PREFERENCE 聚合）

产品宪法 §十一定义 LifeResponseMap 为：

> 什么事情，在什么状态、时间、天气、地点和行动成本下，用户更愿意开始，并在事后认为值得。

这是一组「上下文 — 提案 — 决策 — 发生 — 反馈」的关系模型，包含以下维度：

| 维度 | 来源 | 说明 |
|---|---|---|
| 上下文（状态/时间/天气/地点/行动成本） | 上下文快照（提案生成时的上下文） | 提案发生时的用户状态与环境 |
| 提案 | ExperienceProposal | 提出的体验 |
| 决策 | ProposalDecision | 接受/拒绝/忽略 |
| 是否发生 | ExperienceOccurrence | 用户自愿确认 |
| 是否值得 | ExperienceFeedback | 十秒反馈 |
| 学习结果 | 派生 | 上述维度的聚合关系（如「低精力时，X 类体验更容易开始且值得」） |

> LifeResponseMap **不等于** ai_memory PREFERENCE。PREFERENCE 是用户主动表达或经确认的偏好标签（如「不喜欢吵闹」），丢失了上下文—提案—决策—发生—反馈的关系维度。LifeResponseMap 是基于完整关系链的派生读模型。

### ai_memory（偏好与推断存储）

ai_memory 保存：
- **PREFERENCE**：用户主动表达或经确认的偏好（如「不喜欢吵闹」），可被推荐排序直接使用
- **INFERENCE**：基于行为模式推断的概率性判断（如「用户可能偏好安静」），不可直接升为 PREFERENCE
- **FACT**：引用 external_fact 的事实记忆（SSOT 为 external_fact，ADR-0015）

ai_memory 不等于 LifeResponseMap；ai_memory 中的 PREFERENCE/INFERENCE 可被 LifeResponseMap 的派生逻辑引用，但 LifeResponseMap 的完整关系维度不存在于 ai_memory 中。

## Ownership

| 数据对象 | Owner 模块 | 存储 | 用户控制权 | 来源证明 |
|---|---|---|---|---|
| **LifeResponseMap**（派生读模型） | AI Platform | 派生读模型（基于 ProposalDecision/Occurrence/Feedback + 上下文快照） | 查看（只读派生）/ 删除（删除源数据后派生失效） | 源数据为 ProposalDecision/Occurrence/Feedback |
| **PREFERENCE** | AI Platform | `ai_memory`（memory_category=PREFERENCE） | 查看 / 修改 / 删除 / 撤回 | source + source_data_type + 数据血缘 |
| **INFERENCE** | AI Platform | `ai_memory`（memory_category=INFERENCE） | 查看 / 删除 / 撤回 | inference_basis + source_data_type + 数据血缘 |
| **FACT** | AI Platform | `ai_memory`（memory_category=FACT，引用 external_fact） | 查看 / 删除 | external_fact.id（SSOT 为 external_fact） |

## 一次行为不得永久固化为偏好

产品宪法 §十.1 明确：「Solo 不能把一次行为永久解释为用户偏好。」

### 行为到偏好的转化规则

| 行为类型 | 是否可形成偏好 | 转化条件 | 结果 |
|---|---|---|---|
| 用户主动表达偏好（如「我不喜欢吵闹」） | 是 | 直接存储为 PREFERENCE，source=USER_STATED | PREFERENCE |
| 单次行为反馈（如拒绝一次提案） | **否**（单次） | 单次行为不形成偏好，仅记录为 ExperienceFeedback 事件 | 无（仅事件） |
| 多次行为模式（如连续 3 次拒绝同类提案） | 是（需累积） | 累积 N 次（N≥3）同方向行为 | **INFERENCE**（非 PREFERENCE） |
| 推断的偏好（如「用户可能喜欢安静」） | 是（作为 INFERENCE） | 存为 INFERENCE | INFERENCE |
| 用户确认的推断 | 是 | 用户主动确认 INFERENCE | 升为 PREFERENCE（source=CONFIRMED） |

### 关键约束

1. **单次行为不固化**：单次行为反馈（接受 / 拒绝 / 忽略）不得直接写入 PREFERENCE，仅记录为 ExperienceFeedback 事件
2. **行为模式只能形成 INFERENCE**：多次（N≥3）同方向行为形成 INFERENCE（概率性），**不可直接升为 PREFERENCE**
3. **INFERENCE 须经用户确认才转为 PREFERENCE**：用户主动确认后，INFERENCE 升为 PREFERENCE（source=CONFIRMED）
4. **推断与偏好分离**：INFERENCE 表达概率与不确定性，PREFERENCE 是用户主动表达或经用户确认的偏好（对齐 ADR-0015 三类分离）
5. **推断可降级**：INFERENCE 长期未被行为验证时，confidence 随时间衰减；用户主动否定时立即删除

## 数据治理：用户控制权

### 查看（Read）

- 用户可查看自己的 ai_memory 全部记录（PREFERENCE / INFERENCE / FACT）
- 展示时区分「你主动告诉我的」（USER_STATED）与「我从你的行为推断的」（INFERRED），不混淆
- LifeResponseMap 以派生读模型展示「你的生活反应地图」（只读，不可直接修改派生结果）

### 修改（Update）

- **PREFERENCE**：用户可修改偏好内容（如「不喜欢吵闹」改为「不喜欢非常吵闹」）
- **INFERENCE**：用户不可直接修改推断内容，但可否定（删除）或确认（升为 PREFERENCE）
- **FACT**：用户不可修改（SSOT 为 external_fact，ADR-0015）
- **LifeResponseMap**：用户不可直接修改派生读模型（通过修改/删除源数据间接影响）

### 删除（Delete）

- 用户可删除任意 ai_memory 记录（PREFERENCE / INFERENCE / FACT 引用）
- 删除 PREFERENCE：立即从推荐排序移除
- 删除 INFERENCE：立即从推断模型移除
- 删除 FACT 引用：仅解除 ai_memory 引用，external_fact 事实本身由 ADR-0015 治理
- 删除 LifeResponseMap 源数据（ProposalDecision/Occurrence/Feedback）：派生读模型同步失效

### 撤回（Withdraw）

用户可撤回授权（如撤回位置授权），撤回后**基于数据血缘清理派生数据**：

- 撤回位置授权 → 查找 `source_data_type` 含 LOCATION 的 ai_memory 记录 → 删除所有匹配的 INFERENCE
- 撤回活动记录授权 → 查找 `source_data_type` 含 ACTIVITY_RECORD 的 ai_memory 记录 → 删除所有匹配的 INFERENCE
- 撤回清理范围：派生 INFERENCE 必须删除；USER_STATED PREFERENCE 保留（用户主动表达的，与授权无关）；LifeResponseMap 派生读模型中基于已删 INFERENCE 的部分同步失效

> 数据血缘是撤回清理可执行的前提，见 §Data Model 的血缘字段。

## 保留期限

| 记忆类型 | 软删除 | 硬删除/匿名化 | 理由 |
|---|---|---|---|
| **PREFERENCE（USER_STATED）** | 用户主动删除时 | 软删除后 30 天硬删除 | 用户主动表达 |
| **PREFERENCE（CONFIRMED）** | 用户主动删除时 | 软删除后 30 天硬删除 | 用户已确认 |
| **INFERENCE** | 180 天过期 / 用户删除时 | 软删除后 30 天硬删除/匿名化 | 推断有不确定性，长期未验证应过期 |
| **FACT 引用** | 跟随 external_fact | external_fact 失效后引用删除 | SSOT 为 external_fact |
| **safety_event_log** | 不软删除 | 365 天后硬删除（须经专业评审，ADR-0018） | 审计日志，不作为偏好 |
| **LifeResponseMap 源数据** | 跟随各自源表 | 跟随各自源表 | 派生读模型随源数据失效 |

### 硬删除/匿名化任务

- 软删除后 30 天：定时任务执行硬删除（物理删除记录）或匿名化（移除 user_id 关联，保留聚合统计）
- INFERENCE 180 天过期：定时任务将 confidence 衰减至 0.0 并软删除，30 天后硬删除
- safety_event_log 365 天：定时任务硬删除（须经专业评审确认保留期，ADR-0018）

## Data Model

### ai_memory 表（Sprint 5 落地，扩展 ADR-0015 的 memory_category）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | 用户 ID（逻辑关联，无物理 FK） |
| memory_category | VARCHAR(20) | FACT / INFERENCE / PREFERENCE（ADR-0015） |
| source | VARCHAR(20) | USER_STATED / INFERRED / CONFIRMED / FACT_REF |
| content | TEXT | 记忆内容（偏好 / 推断 / 事实引用） |
| **source_data_type** | VARCHAR(30) | 数据来源类别（LOCATION / ACTIVITY_RECORD / MOOD_INPUT / EXTERNAL_FACT / USER_DIRECT 等，用于撤回清理） |
| **source_record_refs** | JSONB | 原始记录引用（如 {feedback_ids:[1,2,3], decision_ids:[5,6]}） |
| **consent_scenario** | VARCHAR(30) | 授权场景（ADR-0016 的 scenario，如 EXPLORE_BROWSE / PROPOSAL_GENERATION） |
| **derived_from_memory_ids** | JSONB | 派生自哪些 ai_memory 记录（如 INFERENCE 派生自多个行为事件） |
| **provenance_json** | JSONB | 完整数据血缘（来源链路、派生过程、时间戳） |
| inference_basis | JSONB | 推断依据（INFERENCE 时必填，如 {behavior_count:3, last_at:...}） |
| probability | DECIMAL(3,2) | 推断概率（INFERENCE 时必填，0.00-1.00） |
| external_fact_id | BIGINT | 引用 external_fact（FACT 时必填，逻辑关联） |
| confidence | DECIMAL(3,2) | 记忆置信度（随时间衰减） |
| valid_until | TIMESTAMP | 有效期（INFERENCE 默认 180 天，nullable 表示永久） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted_at | TIMESTAMP | 软删除时间（nullable） |
| **hard_deleted_at** | TIMESTAMP | 硬删除时间（nullable，硬删除/匿名化任务填充） |

> 注：禁止物理 FK（对齐 DATABASE_DESIGN §9）。加粗字段为本 ADR 新增的数据血缘字段，使撤回清理可执行。

### 数据血缘字段说明

| 字段 | 用途 | 撤回清理中的作用 |
|---|---|---|
| source_data_type | 记录数据来源类别 | 撤回位置授权时查找 source_data_type=LOCATION 的记录 |
| source_record_refs | 引用原始记录 | 确认派生数据基于哪些原始记录 |
| consent_scenario | 授权场景 | 确认派生数据基于哪个授权场景 |
| derived_from_memory_ids | 派生自哪些记忆 | 级联清理（删除源记忆时清理派生） |
| provenance_json | 完整血缘 | 审计 + 复杂清理场景 |

### LifeResponseMap 派生读模型

LifeResponseMap **不单独建表**，是基于以下源数据形成的派生读模型：

```
LifeResponseMap = f(
  ExperienceProposal（上下文快照：状态/时间/天气/地点/行动成本）
  × ProposalDecision（接受/拒绝/忽略）
  × ExperienceOccurrence（是否发生）
  × ExperienceFeedback（是否值得）
  × ai_memory PREFERENCE/INFERENCE（偏好与推断引用）
)
```

- 源数据 SSOT：ExperienceProposal / ProposalDecision / ExperienceOccurrence / ExperienceFeedback（各自 Owner: Today）
- 派生读模型 Owner：AI Platform（派生逻辑）
- 用户删除源数据 → 派生读模型同步失效
- ai_memory PREFERENCE/INFERENCE 被引用但不是 LifeResponseMap 的全部

## Reason

- **产品驱动**：产品宪法 §十一定义 LifeResponseMap 为「上下文—提案—决策—发生—反馈」关系模型，不是 PREFERENCE 标签集合；将 LifeResponseMap 错误收缩为 PREFERENCE 聚合会丢失状态/时间/天气/地点/阻力/发生/值得维度
- **架构约束**：ADR-0013 决定 LifeResponseMap 存入 AI 侧；ADR-0015 定义 memory_category 分类；本 ADR 补全 Owner、数据血缘、控制权、撤回清理、保留期限
- **隐私驱动**：撤回授权后派生数据必须基于数据血缘清理，否则无法可靠找到需要清理的记录
- **一次行为不固化**：产品宪法 §十.1 要求行为模式只能形成 INFERENCE，经用户确认才转为 PREFERENCE

## Impact

### 影响模块

- AI Platform：ai_memory 表实现（含数据血缘字段）+ LifeResponseMap 派生读模型 + 数据治理
- Today：ExperienceFeedback 不直接写 PREFERENCE（单次行为不固化）；ProposalDecision/Occurrence/Feedback 作为 LifeResponseMap 源数据
- Explore：撤回位置授权时基于 source_data_type 触发派生 INFERENCE 清理
- Mood：State Understanding 接收的用户输入可形成 PREFERENCE（USER_STATED）

### 需要修改的文档

- DATABASE_DESIGN.md：新增 ai_memory 表完整设计（含数据血缘字段）+ 保留期限规则（第 4 步）
- ARCHITECTURE.md：§7 AI Platform Memory Layer 新增数据治理规则 + LifeResponseMap 派生读模型；§22 Data Ownership 标注 ai_memory Owner: AI Platform（第 4 步）
- ADR-0015：同步修正「行为反馈可作为 PREFERENCE」→「行为模式只能形成 INFERENCE，经用户确认才转为 PREFERENCE」
- CODE_RULES.md：§9 API 规范新增 ai_memory 查看 / 修改 / 删除 / 撤回接口要求（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_ai_memory_table.sql`（含数据血缘字段）
- 新建 AiMemory Entity / Repository / Service
- Sprint 5 AI Platform：Memory Layer 实现（含 memory_category + 数据血缘 + 置信度衰减 + 保留期限 + 硬删除/匿名化任务）
- 派生数据清理逻辑（撤回授权时基于 source_data_type 触发）
- LifeResponseMap 派生读模型（基于 ProposalDecision/Occurrence/Feedback + 上下文快照）
- 测试：单次行为不固化、累积形成 INFERENCE、确认升 PREFERENCE、查看/修改/删除/撤回、血缘清理、保留期限、硬删除

### 是否影响现有数据

- ai_memory 表：Sprint 5 新建，无影响
- 现有 MockMemoryService（Sprint 2）：Sprint 5 替换为正式实现，Mock 保留供测试

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 ai_memory 表完整设计（含数据血缘字段）+ 保留期限规则
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：Memory Layer 实现 + 血缘清理 + LifeResponseMap 派生读模型 + 硬删除/匿名化任务 + 测试

### Follow-up ADR

- 无（本 ADR 完整定义 ai_memory 所有权与数据治理）
- 未来若需引入向量检索（Vector DB）存储 ai_memory 语义 embedding，由 ADR-0005（Proposed）决定 Provider/Adapter

### 验证方式

- ai_memory 表 migration 执行成功（含数据血缘字段）
- 单次行为反馈不写入 PREFERENCE（测试用例）
- 累积 N≥3 次同方向行为形成 INFERENCE（测试用例）
- INFERENCE 经用户确认升为 PREFERENCE（source=CONFIRMED）（测试用例）
- 用户可查看 / 修改 / 删除 ai_memory 记录（接口测试）
- 撤回位置授权后，基于 source_data_type=LOCATION 的 INFERENCE 被清理（血缘清理测试）
- USER_STATED PREFERENCE 在撤回位置授权后保留（不误删测试）
- INFERENCE 180 天过期软删除，30 天后硬删除/匿名化（保留期限测试）
- LifeResponseMap 派生读模型正确反映上下文—提案—决策—发生—反馈关系（派生测试）
- LifeResponseMap 不等于 ai_memory PREFERENCE 聚合（维度完整性测试）
- safety_event_log 不作为 ai_memory PREFERENCE / INFERENCE 存储（与 ADR-0018 一致性测试）
- ai_memory 无双重 SSOT（与 external_fact 一致性，FACT 引用不重复存储）
