# ADR-0013: Today Core Object Lifecycle Refactor

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §六 P5、§八、§九 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §7.1 Today 决策。
> 涉及对 Accepted ADR-0011 Activity Ownership 条款的最终处置。

---

## Decision

将 Today Module 核心对象从 `DailyPlan / Activity` 重构为体验提案生命周期 6 阶段对象：

| 阶段 | 核心对象 | 含义 | 用户动作 |
|---|---|---|---|
| 发现现实机会 | `ExperienceOpportunity` | 真实、时效性的候选机会（含事实可信度，ADR-0015） | 无（系统发现） |
| 向用户提出建议 | `ExperienceProposal` | 五要素提案（真实机会/个人理由/恰当时机/低阻力路径/退出许可） | 接收/拒绝/忽略 |
| 用户接受或拒绝 | `ProposalDecision` | 接受/拒绝/忽略，可撤回 | 主动决策 |
| 体验是否发生 | `ExperienceOccurrence` | 用户自愿确认已开始或已体验（不强制，可跳过） | 自愿确认 |
| 用户是否觉得值得 | `ExperienceFeedback` | 十秒反馈，可跳过 | 自愿反馈 |
| 长期学习 | `LifeResponseMap` | 形成个人生活反应地图（存于 AI 侧） | 无（系统学习） |

### 关键约束

1. **一次一份提案**：同一时刻最多向用户提出一份 ExperienceProposal（产品宪法 §六 P5）
2. **no_proposal 是正常结果**：无足够好建议时返回 no_proposal，不是算法失败（产品宪法 §九）
3. **体验没有完成义务**：用户接受提案后不强制确认发生，不设置失败惩罚（产品宪法 §六 P7）
4. **居家与外出同等价值**：提案不偏向城市地点或消费体验（产品宪法 §六 P3）

## Migration Strategy Comparison

针对现有 `daily_plan` / `activity` 表（已建 migration V20260730_002/003，有测试数据无生产数据）的迁移方式，比较三种方案：

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. 原位演进** | 在 daily_plan/activity 表上增加字段/改语义，逐步演变为 Experience 系列 | 迁移成本低；保持表结构连续性 | 语义混乱（表名 daily_plan 存储 ExperienceProposal）；历史 migration 难以追溯真实意图 | 表结构与目标语义接近时 |
| **B. 增量迁移** | 新建 experience_* 系列表，daily_plan/activity 保留兼容，双写过渡，最终下线旧表 | 语义清晰；可回滚；过渡期不影响现有功能 | 短期复杂度增加（双表+双 API）；需写数据同步逻辑 | 有生产数据或需零停机时 |
| **C. 版本化替换** | 新建 experience_* 系列表，直接下线 daily_plan/activity（删除或归档），不保留兼容 | 语义最清晰；无技术债；代码最简洁 | 无法回滚到旧 API；需一次性完成迁移 | 无生产数据时 |

## Decision on Migration Strategy

**采用方案 C（版本化替换）**，理由：

1. **无生产数据**：项目处于 Sprint 3，daily_plan/activity 仅有测试数据，无真实用户数据
2. **语义差距大**：DailyPlan（一天计划）与 ExperienceProposal（单个体提案）语义差异显著，原位演进会造成表名与语义长期不一致
3. **技术债最小**：直接新建语义清晰的表 + 下线旧表，避免双写过渡的复杂度
4. **测试数据可重建**：现有 60+ 测试用例需重写，但测试数据本身无保留价值

### 具体执行

1. 新建 migration：`V20260807_xxx__create_experience_proposal_series.sql`，创建 5 张表（experience_opportunity / experience_proposal / proposal_decision / experience_occurrence / experience_feedback）
2. LifeResponseMap 不单独建表，存于 AI 侧 ai_memory（待 Sprint 5 AI Platform 实现）
3. 新建 migration：`V20260807_xxx__drop_daily_plan_activity.sql`，删除 daily_plan / activity 表
4. 写 ADR 记录删除决策（本 ADR 即记录）
5. 重写 Today Module 代码：Entity / Repository / Domain Service / Application Service / Controller / 测试
6. 重写前端 today 4 页面：index / plan-detail / replan / summary 改为 proposal / proposal-detail / decision / feedback 语义

## Activity Disposition

现有 `Activity` 实体（与 ADR-0011 相关）的处置：

- **不再作为目标产品核心概念**：Activity 含义过宽（既表示计划项，又表示体验发生），拆分为 ExperienceOccurrence（体验发生）等具体对象
- **ADR-0011 处置**：本 ADR Accepted 后，ADR-0011 中「Activity 归 Today」「Explore 只读引用 Activity Domain API」条款不再有效，由本 ADR 替代。新建 ADR-0018 标记 ADR-0011 为 Deprecated（或在本 ADR 中直接声明替代，待 AGENTS §8 流程确认）
- **Explore 引用**：Explore 不再引用 Activity Domain API；如需关联体验发生记录，通过 ExperienceOccurrence 的 location_id 反查

## Impact

### 影响模块

- Today：全部重构（Entity / Service / Controller / 测试 / 前端）
- Explore：移除对 Activity Domain API 的引用（如有）
- AI Platform：Proposal Composer / Motivation Engine / Life Curator 依赖 ExperienceProposal

### 需要修改的文档

- ARCHITECTURE.md：§6 Today 核心对象、§8 AI 链路（第 4 步）
- DATABASE_DESIGN.md：§6.3 daily_plan、§6.4 activity、§7 PLAN_STATUS/ACTIVITY_TYPE 枚举（第 4 步）
- SPRINT_PLAN.md：Sprint 2 标注废弃 + 新增重构任务（第 5 步）
- TASK_BOARD.md：新增 Today 重构任务卡（第 5 步）

### 需要新增/修改的代码

- 新建 5 张 experience_* 表 migration
- 新建 ExperienceOpportunity/ExperienceProposal/ProposalDecision/ExperienceOccurrence/ExperienceFeedback Entity
- 重写 Today Module Domain/Application/Controller 层
- 删除 DailyPlan/Activity Entity/Repository/Service/Controller
- 重写 60+ 测试用例
- 重写前端 today 4 页面

### 是否影响现有数据

- daily_plan / activity 表：有测试数据无生产数据，直接 drop
- 测试数据：可重建，无保留价值

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 5 张表设计，标注 daily_plan/activity 废弃
2. 第 7 步：执行代码迁移
   - 新建 experience_* 表 migration
   - 新建 Entity / Repository / Service / Controller
   - 删除 DailyPlan / Activity 相关代码
   - 删除 daily_plan / activity 表 migration
   - 重写测试
   - 重写前端
3. 每个步骤独立 PR，遵循分支+PR 流程

### Follow-up ADR

- 无（本 ADR 完整定义迁移方式）

### 验证方式

- 5 张 experience_* 表 migration 执行成功
- Today Module 测试全部通过
- daily_plan / activity 表已删除
- 前端 today 页面语义改为 proposal
- no_proposal 响应正确返回（ADR-0014 AI 角色落地后）
