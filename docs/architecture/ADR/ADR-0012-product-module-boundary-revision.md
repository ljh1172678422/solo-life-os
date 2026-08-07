# ADR-0012: Product Module Boundary Revision

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 决策。
> 涉及对 Accepted ADR-0011 的部分修订，详见 §Relation to ADR-0011。

---

## Decision

将目标产品业务模块边界从「8 模块 + AI Platform」收敛为「4 业务模块 + AI Platform」：

- **保留并重新定义范围**：User、Today、Explore、Mood、AI Platform
- **从目标产品范围移除**：Growth、Community
- **暂缓**：Story

Growth / Community / Story 的现有代码、实体、表处置方式，本 ADR 不锁定，需基于代码核查结果在第 7 步代码迁移评估中决定（保留兼容 / 重构 / 移除）。

## Reason

- **产品驱动**：产品宪法 §三明确「不是社交平台或搭子社区」「不是习惯养成工具」；§十三明确「不鼓励连续打卡，不设置失败惩罚」「不设置关注、粉丝、动态社区和排行榜」。Growth 的目标/习惯/打卡/成长统计、Community 的交流/关系链/社区概念，与产品宪法直接冲突。
- **架构约束**：PROJECT_CONTEXT v1.3 §7 已确立目标产品范围为 4 模块 + AI Platform。本 ADR 将该产品决策转化为架构事实。
- **演进约束**：产品宪法 §十四规定演进顺序为「一件事 → 一段连接 → 一个下午 → Day Curation」，Growth/Community/Story 属于远期或不属于目标产品。

## Module Scope After Revision

### 保留模块（重新定义范围）

| 模块 | 新范围 | 与旧版差异 |
|---|---|---|
| User | 用户身份、偏好、数据授权 | 新增数据授权职责（ADR-0016） |
| Today | 体验提案生命周期（6 阶段对象） | DailyPlan 不再是核心对象；核心对象实现方式由 ADR-0013 决定 |
| Explore | 地点发现、收藏、有限主动探索 | 不得演化为无限信息流；活动信息归属及关联方式待代码核查 + ADR 决定 |
| Mood | 用户主动提供的「此刻状态」输入 | 不作心理诊断、趋势监控、成长评分；具体表名/字段待数据库设计 |
| AI Platform | 6 角色（Opportunity Discovery / Proposal Composer / Motivation Engine / Life Curator / State Understanding / Assistant） | 角色拆分粒度由 ADR-0014 决定 |

### 移除模块（从目标产品范围）

| 模块 | 处置 | 现有代码/表 |
|---|---|---|
| Growth | 从目标产品范围移除 | goal 表未建 migration（已核查）；Entity/Repository/Service 待代码核查；处置方式待第 7 步 |
| Community | 从目标产品范围移除 | community_event / registration 表未建 migration；无代码；仅文档清理 |

### 暂缓模块

| 模块 | 处置 | 说明 |
|---|---|---|
| Story | 暂缓，未来作为用户主动触发的回顾 | 不承担留存任务；不进入近期 Sprint |

## Relation to ADR-0011

ADR-0011（Activity Ownership）当前仍 Accepted，规定：

- Activity 归 Today
- Explore 只读引用 Activity Domain API
- CommunityEvent 是独立领域实体

本 ADR 对 ADR-0011 的影响：

| ADR-0011 条款 | 本 ADR 影响 | 处置 |
|---|---|---|
| Activity 归 Today | 部分修订：Today 核心对象改为 Experience 系列，Activity 是否拆分由 ADR-0013 决定 | ADR-0013 落地后，新建 ADR 替代 ADR-0011 的 Activity 相关条款 |
| Explore 只读引用 Activity Domain API | 待 ADR-0013 决定 Activity 拆分方式后重新评估 | 暂保留 |
| CommunityEvent 是独立领域实体 | 修订：Community 从目标产品范围移除，CommunityEvent 不再作为目标产品核心概念 | 现有架构事实（无表、无代码）继续有效，无需迁移 |

**在 ADR-0013 落地前，ADR-0011 继续有效。**

## Impact

### 影响模块

- User：新增数据授权职责（ADR-0016）
- Today：核心对象重构（ADR-0013）
- Explore：信息流上限约束 + 活动归属待定
- Mood：语义收敛
- AI Platform：6 角色定义（ADR-0014）
- Growth / Community / Story：从目标产品范围移除或暂缓

### 需要修改的文档

- ARCHITECTURE.md：§3 Shared Domain、§4 模块依赖、§6 模块设计、§22 Data Ownership（第 4 步）
- DATABASE_DESIGN.md：§3 Shared Entities、§4 Entity Ownership、§6.6 goal、§6.11 community_event、§6.12 registration、§7 相关枚举（第 4 步）
- CODE_RULES.md：§4 包结构、§12.2 scope 列表（第 6 步）
- SPRINT_PLAN.md：Sprint 6 Growth、Sprint 7 Community、Sprint 8 Story（第 5 步）
- TASK_BOARD.md：相关任务卡状态（第 5 步）

### 需要新增/修改的代码

- 现有 growth / community / story 包代码（待代码核查后决定）
- 无 production 数据，可考虑直接清理，但需第 7 步评估确认

### 是否影响现有数据

- goal / community_event / registration 表：未建 migration，无数据
- daily_plan / activity 表：已建 migration，有测试数据，由 ADR-0013 决定迁移方式

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 ARCHITECTURE.md 和 DATABASE_DESIGN.md，标注 Growth/Community 移除、Story 暂缓
2. 第 7 步：代码核查 growth/community/story 包实际内容，决定保留兼容/重构/移除
3. 若决定移除代码：新建删除 PR，遵循正常分支+PR 流程

### Follow-up ADR

- ADR-0013：Today 核心对象生命周期重构（决定 Activity 拆分方式，进而决定 ADR-0011 的最终处置）
- ADR-0014：AI Platform 6 角色职责与拆分粒度

### 验证方式

- ARCHITECTURE.md §6 模块清单与本 ADR 一致
- DATABASE_DESIGN.md 不再包含 goal / community_event / registration 作为目标产品核心表
- SPRINT_PLAN.md 不再包含 Growth/Community Sprint（Story 标注暂缓）
- 代码核查报告确认 growth/community/story 包实际状态
