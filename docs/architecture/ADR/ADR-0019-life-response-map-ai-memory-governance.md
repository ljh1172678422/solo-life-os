# ADR-0019: LifeResponseMap / ai_memory Ownership and Data Governance

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.1、§十一、§十三 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §6.1、§12、§13.5 决策。
> 产品宪法 §十.1：「偏好记忆应允许用户查看、修改和删除。」「Solo 不能把一次行为永久解释为用户偏好。」
> 产品宪法 §十一：LifeResponseMap 是「个人生活反应地图」，长期价值不是积累兴趣标签。

---

## Decision

确立 `LifeResponseMap`（个人生活反应地图）与 `ai_memory` 的所有权、来源证明、数据治理规则。**一次行为不得永久固化为偏好**；用户拥有查看、修改、删除、撤回的完整控制权；撤回后派生数据必须清理；明确各类记忆的保留期限。Owner 为 AI Platform（数据治理层），用户拥有控制权。

## Ownership

| 数据对象 | Owner 模块 | 存储 | 用户控制权 | 来源证明 |
|---|---|---|---|---|
| **LifeResponseMap**（个人生活反应地图） | AI Platform | `ai_memory`（memory_category=PREFERENCE，聚合视图） | 查看 / 修改 / 删除 / 撤回 | 来源（主动表达 / 行为推断）+ 时间 |
| **偏好记忆（PREFERENCE）** | AI Platform | `ai_memory`（memory_category=PREFERENCE） | 查看 / 修改 / 删除 / 撤回 | 来源（主动表达 / 行为推断）+ 时间 |
| **推断记忆（INFERENCE）** | AI Platform | `ai_memory`（memory_category=INFERENCE） | 查看 / 删除 / 撤回 | inference_basis / probability / inferred_at |
| **事实记忆（FACT）** | AI Platform | `ai_memory`（memory_category=FACT，引用 external_fact） | 查看 / 删除 | 引用 external_fact.id（SSOT 为 external_fact，ADR-0015） |

> 注：LifeResponseMap 不是单独的表，是 ai_memory 中 PREFERENCE 类记忆的**聚合视图**，反映用户的「生活反应地图」。这样避免双重 SSOT（ai_memory 与独立 life_response_map 表并存）。

## 一次行为不得永久固化为偏好

产品宪法 §十.1 明确：「Solo 不能把一次行为永久解释为用户偏好。」

### 行为到偏好的转化规则

| 行为类型 | 是否可形成偏好 | 转化条件 | 置信度 |
|---|---|---|---|
| 用户主动表达偏好（如「我不喜欢吵闹」） | 是 | 直接存储为 PREFERENCE，source=USER_STATED | 高（1.0） |
| 单次行为反馈（如拒绝一次提案） | **否**（单次） | 单次行为不形成偏好，仅记录为事件 | 不形成偏好 |
| 多次行为模式（如连续 3 次拒绝同类提案） | 是（需累积） | 累积 N 次（N≥3）同方向行为，转化为 INFERENCE（非 PREFERENCE） | 中（0.5-0.7） |
| 推断的偏好（如「用户可能喜欢安静」） | 是（作为 INFERENCE） | 存为 INFERENCE，不直接升为 PREFERENCE | 中（0.5-0.7） |

### 关键约束

1. **单次行为不固化**：单次行为反馈（接受 / 拒绝 / 忽略）不得直接写入 PREFERENCE，仅记录为 ExperienceFeedback 事件
2. **累积才推断**：多次（N≥3）同方向行为可形成 INFERENCE（概率性），不可直接升为 PREFERENCE
3. **推断与偏好分离**：INFERENCE 表达概率与不确定性，PREFERENCE 是用户主动表达或经用户确认的偏好（对齐 ADR-0015 三类分离）
4. **推断可降级**：INFERENCE 长期未被行为验证时，confidence 随时间衰减；用户主动否定时立即删除

## 数据治理：用户控制权

### 查看（Read）

- 用户可查看自己的 ai_memory 全部记录（PREFERENCE / INFERENCE / FACT）
- 展示时区分「你主动告诉我的」（USER_STATED）与「我从你的行为推断的」（INFERRED），不混淆
- LifeResponseMap 以聚合视图展示「你的生活反应地图」

### 修改（Update）

- **PREFERENCE**：用户可修改偏好内容（如「不喜欢吵闹」改为「不喜欢非常吵闹」）
- **INFERENCE**：用户不可直接修改推断内容，但可否定（删除）或确认（升为 PREFERENCE）
- **FACT**：用户不可修改（SSOT 为 external_fact，ADR-0015）

### 删除（Delete）

- 用户可删除任意 ai_memory 记录（PREFERENCE / INFERENCE / FACT 引用）
- 删除 PREFERENCE：立即从推荐排序移除
- 删除 INFERENCE：立即从推断模型移除
- 删除 FACT 引用：仅解除 ai_memory 引用，external_fact 事实本身由 ADR-0015 治理

### 撤回（Withdraw）

- 用户可撤回授权（如撤回位置授权），撤回后**派生数据必须清理**：
  - 撤回位置授权 → 删除所有基于位置推断的 INFERENCE（source 含 LOCATION）
  - 撤回活动记录授权 → 删除所有基于活动记录推断的 INFERENCE（source 含 ACTIVITY）
- 撤回清理范围：派生 INFERENCE 必须删除；USER_STATED PREFERENCE 保留（用户主动表达的，与授权无关）

## 保留期限

| 记忆类型 | 保留期限 | 过期处理 | 理由 |
|---|---|---|---|
| **PREFERENCE（USER_STATED）** | 永久（用户主动表达） | 用户主动删除时删除 | 用户主动表达，无需过期 |
| **PREFERENCE（CONFIRMED）** | 永久（用户确认的推断） | 用户主动删除时删除 | 用户已确认，等同主动表达 |
| **INFERENCE** | 180 天 | 过期后 confidence 衰减至 0.0 并软删除 | 推断有不确定性，长期未验证应过期 |
| **FACT 引用** | 跟随 external_fact（ADR-0015） | external_fact 过期后引用失效 | SSOT 为 external_fact |
| **safety_event_log** | 365 天（审计） | 过期后硬删除 | ADR-0018 审计日志，不作为偏好 |

### 派生数据清理

- 用户删除 PREFERENCE / INFERENCE → 检查是否有派生记录（如基于该 PREFERENCE 的聚合视图）→ 一并清理
- 用户撤回授权 → 按撤回清理范围删除派生 INFERENCE
- INFERENCE 过期软删除 → 派生聚合视图同步更新

## Data Model

### ai_memory 表（Sprint 5 落地，扩展 ADR-0015 的 memory_category）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | 用户 ID（逻辑关联，无物理 FK） |
| memory_category | VARCHAR(20) | FACT / INFERENCE / PREFERENCE（ADR-0015） |
| source | VARCHAR(20) | USER_STATED / INFERRED / CONFIRMED / FACT_REF |
| content | TEXT | 记忆内容（偏好 / 推断 / 事实引用） |
| inference_basis | JSONB | 推断依据（INFERENCE 时必填，如 {behavior_count:3, last_at:...}） |
| probability | DECIMAL(3,2) | 推断概率（INFERENCE 时必填，0.00-1.00） |
| external_fact_id | BIGINT | 引用 external_fact（FACT 时必填，逻辑关联） |
| confidence | DECIMAL(3,2) | 记忆置信度（随时间衰减） |
| valid_until | TIMESTAMP | 有效期（INFERENCE 默认 180 天，nullable 表示永久） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted_at | TIMESTAMP | 软删除时间（nullable） |

> 注：禁止物理 FK（对齐 DATABASE_DESIGN §9）。user_id / external_fact_id 均为逻辑关联。

### 双重 SSOT 避免规则

- LifeResponseMap **不单独建表**，是 ai_memory（PREFERENCE）的聚合视图 → 避免与 ai_memory 双重 SSOT
- FACT 记忆引用 external_fact.id，不重复存储事实内容 → SSOT 为 external_fact（ADR-0015）
- safety_event_log 独立于 ai_memory → 不作为偏好存储（ADR-0018）

## Reason

- **产品驱动**：产品宪法 §十.1「偏好记忆应允许用户查看、修改和删除」「不能把一次行为永久解释为用户偏好」；§十一 LifeResponseMap 是长期价值，不是兴趣标签积累
- **架构约束**：ADR-0013 决定 LifeResponseMap 存入 ai_memory；ADR-0015 定义 memory_category 分类；本 ADR 补全 Owner、来源证明、控制权、撤回清理、保留期限
- **隐私驱动**：产品宪法 §十三 + PROJECT_CONTEXT §13.5 用户数据控制权；撤回授权后派生数据必须清理，否则等于变相保留
- **避免双重 SSOT**：LifeResponseMap 作为 ai_memory 聚合视图，不单独建表，避免记忆数据分散在两处

## Impact

### 影响模块

- AI Platform：ai_memory 表实现 + 数据治理（查看 / 修改 / 删除 / 撤回 / 派生清理 / 保留期限）
- Today：ExperienceFeedback 不直接写 PREFERENCE（单次行为不固化）
- Explore：撤回位置授权时触发派生 INFERENCE 清理
- Mood：State Understanding 接收的用户输入可形成 PREFERENCE（USER_STATED）

### 需要修改的文档

- DATABASE_DESIGN.md：新增 ai_memory 表完整设计 + 保留期限规则（第 4 步）
- ARCHITECTURE.md：§7 AI Platform Memory Layer 新增数据治理规则；§22 Data Ownership 标注 ai_memory Owner: AI Platform（第 4 步）
- PROJECT_CONTEXT.md：§12 数据资产 LifeResponseMap 治理已在本 ADR 落地
- CODE_RULES.md：§9 API 规范新增 ai_memory 查看 / 修改 / 删除 / 撤回接口要求（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_ai_memory_table.sql`
- 新建 AiMemory Entity / Repository / Service
- Sprint 5 AI Platform：Memory Layer 实现（含 memory_category 分类 + 来源证明 + 置信度衰减 + 保留期限 + 软删除）
- 派生数据清理逻辑（撤回授权时触发）
- LifeResponseMap 聚合视图（基于 ai_memory PREFERENCE）
- 测试：单次行为不固化、累积推断、查看 / 修改 / 删除 / 撤回、派生清理、保留期限

### 是否影响现有数据

- ai_memory 表：Sprint 5 新建，无影响
- 现有 MockMemoryService（Sprint 2）：Sprint 5 替换为正式实现，Mock 保留供测试

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 ai_memory 表完整设计 + 保留期限规则
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：Memory Layer 实现 + 派生清理 + LifeResponseMap 聚合视图 + 测试

### Follow-up ADR

- 无（本 ADR 完整定义 ai_memory 所有权与数据治理）
- 未来若需引入向量检索（Vector DB）存储 ai_memory 语义 embedding，由 ADR-0005（Proposed）决定 Provider/Adapter，本 ADR 不锁定存储引擎

### 验证方式

- ai_memory 表 migration 执行成功
- 单次行为反馈不写入 PREFERENCE（测试用例：拒绝一次提案后 ai_memory 无新 PREFERENCE）
- 累积 N≥3 次同方向行为形成 INFERENCE（测试用例）
- 用户可查看 / 修改 / 删除 ai_memory 记录（接口测试）
- 撤回位置授权后，基于位置的 INFERENCE 被清理（派生清理测试）
- USER_STATED PREFERENCE 在撤回位置授权后保留（不误删测试）
- INFERENCE 180 天过期后 confidence 衰减至 0.0 并软删除（保留期限测试）
- LifeResponseMap 聚合视图正确反映 PREFERENCE（视图测试）
- safety_event_log 不作为 ai_memory PREFERENCE 存储（与 ADR-0018 一致性测试）
- ai_memory 无双重 SSOT（与 external_fact 一致性，FACT 引用不重复存储）
