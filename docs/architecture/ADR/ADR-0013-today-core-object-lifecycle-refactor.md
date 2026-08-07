# ADR-0013: Today Core Object Lifecycle Refactor

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §六 P3、P5、P7、§八、§九 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §7.1 Today 决策。
> 替代 ADR-0011 的 Activity Ownership / Explore 引用条款（ADR-0011 已标记 Deprecated）。

---

## Decision

将 Today Module 核心对象从 `DailyPlan / Activity` 重构为体验提案生命周期 6 阶段对象：

| 阶段 | 核心对象 | 含义 | 用户动作 | Location 关联 |
|---|---|---|---|---|
| 发现现实机会 | `ExperienceOpportunity` | 真实、时效性的候选机会（含事实可信度，ADR-0015） | 无（系统发现） | nullable（居家体验可为空） |
| 向用户提出建议 | `ExperienceProposal` | 五要素提案（真实机会/个人理由/恰当时机/低阻力路径/退出许可） | 接收/拒绝/忽略 | nullable（居家体验可为空） |
| 用户接受或拒绝 | `ProposalDecision` | 接受/拒绝/忽略，可撤回 | 主动决策 | nullable |
| 体验是否发生 | `ExperienceOccurrence` | 用户自愿确认已开始或已体验（不强制，可跳过） | 自愿确认 | nullable |
| 用户是否觉得值得 | `ExperienceFeedback` | 十秒反馈，可跳过 | 自愿反馈 | nullable |
| 长期学习 | `LifeResponseMap` | 形成个人生活反应地图（存于 AI 侧，ADR-0019） | 无（系统学习） | 无 |

### 关键约束

1. **一次一份提案**：同一时刻最多向用户提出一份 ExperienceProposal（产品宪法 §六 P5）
2. **no_proposal 是正常结果**：无足够好建议时返回 no_proposal，不是算法失败（产品宪法 §九）
3. **体验没有完成义务**：用户接受提案后不强制确认发生，不设置失败惩罚（产品宪法 §六 P7）
4. **居家与外出同等价值**：提案不偏向城市地点或消费体验（产品宪法 §六 P3）；**Location 关联可为空**，避免数据库设计重新变成地点中心模型

## Reason

- **产品驱动**：产品宪法 §六 P5「一次一份提案」与 DailyPlan（一天计划）语义冲突；§八明确推荐单位为体验提案（五要素）；§六 P7「体验没有完成义务」与 Activity 完成语义冲突
- **架构约束**：PROJECT_CONTEXT v1.3 §7.1 已确立 Today 核心对象为 6 阶段 Experience 系列；ADR-0011 的 Activity Ownership 条款需由本 ADR 替代
- **演进约束**：产品宪法 §六 P3「居家与外出同等价值」要求 Location 不再是提案的必填关联

## Migration Strategy Comparison

针对现有 `daily_plan` / `activity` 表（已建 migration V20260730_002/003，有测试数据）的迁移方式，比较三种方案：

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. 原位演进** | 在 daily_plan/activity 表上增加字段/改语义，逐步演变为 Experience 系列 | 迁移成本低；保持表结构连续性 | 语义混乱（表名 daily_plan 存储 ExperienceProposal）；历史 migration 难以追溯真实意图 | 表结构与目标语义接近时 |
| **B. 增量迁移** | 新建 experience_* 系列表，daily_plan/activity 保留兼容，双写过渡，最终下线旧表 | 语义清晰；可回滚；过渡期不影响现有功能 | 短期复杂度增加（双表+双 API）；需写数据同步逻辑 | 有生产数据或需零停机时 |
| **C. 版本化替换** | 新建 experience_* 系列表，直接下线 daily_plan/activity（新增 drop migration），不保留兼容 | 语义最清晰；无技术债；代码最简洁 | 无法回滚到旧 API；需一次性完成迁移；需前提全部成立 | 无生产数据、无外部消费者、测试库可重建时 |

## Decision on Migration Strategy

**采用方案 C（版本化替换）**，但需满足以下 Preconditions。

### Preconditions（须由人工产品负责人确认）

方案 C 仅在以下前提**全部成立**时采用：

1. 从未部署生产环境
2. 无生产数据
3. 无外部 API 消费者
4. 测试/演示数据库可安全重建
5. 前后端可协调切换

### Go/No-Go Gate

- **Go**：人工产品负责人确认上述 5 项前提全部成立 → 采用方案 C
- **No-Go**：任一前提不成立 → 自动改用方案 B（增量迁移，保留兼容 + 双写过渡）

### 方案 C 具体执行约束

1. **不删除历史 Migration 文件**：V20260730_002/003 保留在仓库供追溯，仅新增 create/drop Migration
2. **新增 Migration 顺序**：
   - `V20260807_xxx__create_experience_proposal_series.sql`（创建 5 张表）
   - `V20260807_xxx__drop_daily_plan_activity.sql`（drop 旧表）
3. **drop 前备份或确认环境可重建**：drop migration 执行前确认测试库可重建（无生产数据）
4. **协调切换顺序**：Schema（migration）→ 后端 API（Entity/Service/Controller）→ 前端（4 页面重写）→ 测试（60+ 用例重写）
5. **LifeResponseMap 不单独建表**：存于 AI 侧 ai_memory（待 Sprint 5 AI Platform 实现，数据治理见 ADR-0019）

### 失败回退方式

- Migration 失败：回滚 migration（Flyway baseline 回退到 V20260807 之前）
- 后端 API 失败：Git revert 后端 PR，保留 migration 已执行状态（drop migration 未执行则无影响）
- 若已执行 drop migration 后发现需回退：改用方案 B，新建 daily_plan/activity 兼容表（代价较高，因此 drop 前需充分测试）

## Activity Disposition

现有 `Activity` 实体（与 ADR-0011 相关）的处置：

- **不再作为目标产品核心概念**：Activity 含义过宽（既表示计划项，又表示体验发生），拆分为 ExperienceOccurrence（体验发生）等具体对象
- **ADR-0011 处置**：本 ADR Accepted 后，ADR-0011 的「Activity 归 Today」「Explore 只读引用 Activity Domain API」条款由本 ADR 替代。ADR-0011 已标记为 Deprecated（见 [ADR-0011](./ADR-0011-activity-ownership.md)）
- **Explore 引用**：Explore 不再引用 Activity Domain API；如需关联体验发生记录，通过 ExperienceOccurrence 的 location_id 反查（location_id 可为空，居家体验无地点关联）
- **架构/代码迁移状态**：Implementation Pending（待第 7 步代码迁移评估）。在代码迁移完成前，现有 daily_plan/activity 表与代码继续运行，但新开发必须遵循本 ADR

## Impact

### 影响模块

- Today：全部重构（Entity / Service / Controller / 测试 / 前端）
- Explore：移除对 Activity Domain API 的引用（如有）；如需关联体验发生记录，通过 ExperienceOccurrence 反查
- AI Platform：Proposal Composer / Motivation Engine / Life Curator 依赖 ExperienceProposal（ADR-0014）

### 需要修改的文档

- ARCHITECTURE.md：§6 Today 核心对象、§8 AI 链路、§22 Data Ownership（第 4 步）
- DATABASE_DESIGN.md：§6.3 daily_plan、§6.4 activity、§7 PLAN_STATUS/ACTIVITY_TYPE 枚举（第 4 步）
- SPRINT_PLAN.md：Sprint 2 标注废弃 + 新增重构任务（第 5 步）
- TASK_BOARD.md：新增 Today 重构任务卡（第 5 步）

### 需要新增/修改的代码

- 新建 5 张 experience_* 表 migration（不删除历史 migration 文件）
- 新建 ExperienceOpportunity/ExperienceProposal/ProposalDecision/ExperienceOccurrence/ExperienceFeedback Entity
- 重写 Today Module Domain/Application/Controller 层
- 新增 drop migration 删除 daily_plan / activity 表（drop 前确认前提成立）
- 重写 60+ 测试用例
- 重写前端 today 4 页面

### 是否影响现有数据

- daily_plan / activity 表：有测试数据无生产数据（需人工确认 Preconditions），直接 drop
- 测试数据：可重建，无保留价值

## Migration / Follow-up

### Migration 步骤

1. **人工确认 Preconditions**：人工产品负责人确认 5 项前提全部成立
2. 第 4 步：更新 DATABASE_DESIGN.md，新增 5 张表设计，标注 daily_plan/activity 废弃
3. 第 7 步：执行代码迁移
   - 新建 experience_* 表 migration（不删历史 migration 文件）
   - 新建 Entity / Repository / Service / Controller
   - 重写测试
   - 重写前端
   - drop migration 删除 daily_plan / activity 表（drop 前确认前提成立 + 测试通过）
4. 每个步骤独立 PR，遵循分支+PR 流程

### Follow-up ADR

- 无（本 ADR 完整定义迁移方式 + 前提 Gate + 回退方式）

### 验证方式

- Preconditions 由人工产品负责人确认（记录在 PR 审核）
- 5 张 experience_* 表 migration 执行成功
- Today Module 测试全部通过
- daily_plan / activity 表已删除（drop migration 执行成功）
- 前端 today 页面语义改为 proposal
- ExperienceProposal 的 location_id 可为空（居家体验）
- no_proposal 响应正确返回（ADR-0014 AI 角色落地后）
