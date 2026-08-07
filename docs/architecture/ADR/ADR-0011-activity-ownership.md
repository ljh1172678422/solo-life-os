# ADR-0011: Activity Ownership


Date:    2026-07-28

Status:  Deprecated（2026-08-07）

> **Deprecated 说明**：本 ADR 已被以下 ADR 替代，不再生效：
> - **Activity Ownership / Explore 引用条款** → 由 [ADR-0013](./ADR-0013-today-core-object-lifecycle-refactor.md) 替代（Today 核心对象从 DailyPlan/Activity 重构为 Experience 系列，Activity 不再作为目标产品核心概念）
> - **CommunityEvent 独立领域实体条款** → 由 [ADR-0012](./ADR-0012-product-module-boundary-revision.md) 替代（Community 从目标产品范围移除，CommunityEvent 不再作为目标产品核心概念）
>
> 架构/代码迁移状态：**Implementation Pending**（待第 7 步代码迁移评估）。在代码迁移完成前，现有 daily_plan/activity 表与代码继续运行，但本 ADR 不再作为新开发的架构依据。新开发必须遵循 ADR-0012/0013。


## Decision（已废弃）


Activity 表属于 Today Module，Owner 为 Today。

Explore 仅通过 Activity Domain API 只读引用，不创建 Activity。

CommunityEvent 是独立领域实体，不复用 Activity 表。


```
Today Module（Owner）
       │
       └── activity 表（create / update / delete）
            │
            ├── Activity Domain API
            │
            ▼
  ┌─────────────┐
  │             │
Explore        Community
（只读引用）    （独立 community_event）
```


## Reason（历史记录）


- Activity 是用户每日生活事件的执行上下文，与 DailyPlan 同属 Today 领域
- Explore 职责是发现 Location，不应承担 Activity 写入，否则违反 ARCHITECTURE §22 唯一 Owner 规则
- CommunityEvent 是社会活动，与 Activity（个人生活事件）是不同领域，复用会导致 activity 表变成万能表污染领域
- 此决策已在 SPRINT_PLAN v2.1 评审中确认（P0-1 修复），已是架构事实


## Deprecation Reason


- 产品宪法 v1.1 与 PROJECT_CONTEXT v1.3 重构目标产品范围：Today 核心对象从 DailyPlan/Activity 改为 Experience 系列（ADR-0013）；Community 从目标产品范围移除（ADR-0012）
- 保留两个相互冲突的 Accepted ADR 违反仓库 ADR 状态机（Accepted → Deprecated 不可逆向回滚）
- 架构/代码未迁移应标为 Implementation Pending，不能靠保留旧 ADR 生效解决


## Impact（已废弃）


- Today Module：拥有 activity 表的 create / update / delete 权限，发布 activity.completed 事件
- Explore Module：只读引用 Activity Domain API（如展示用户在某 Location 的活动历史），禁止写 activity 表
- Community Module：使用独立的 community_event 表，禁止复用 activity
- DATABASE_DESIGN §4 已标注 activity Owner: Today
- ARCHITECTURE §22 Data Ownership 已标注 Activity Owner: Today
- ARCHITECTURE §9 Event Flow：activity.completed 发布者为 Today（非 Today/Explore）


## References


- SPRINT_PLAN v2.1 评审 P0-1：Activity Owner 冲突修复
- DATABASE_DESIGN v2.1 §4 / §6.4
- ARCHITECTURE v2.3 §22
- **替代 ADR**：[ADR-0012](./ADR-0012-product-module-boundary-revision.md)（CommunityEvent 条款）、[ADR-0013](./ADR-0013-today-core-object-lifecycle-refactor.md)（Activity 条款）
