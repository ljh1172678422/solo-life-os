# ADR-0011: Activity Ownership


Date:    2026-07-28

Status:  Accepted


## Decision


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


## Reason


- Activity 是用户每日生活事件的执行上下文，与 DailyPlan 同属 Today 领域
- Explore 职责是发现 Location，不应承担 Activity 写入，否则违反 ARCHITECTURE §22 唯一 Owner 规则
- CommunityEvent 是社会活动，与 Activity（个人生活事件）是不同领域，复用会导致 activity 表变成万能表污染领域
- 此决策已在 SPRINT_PLAN v2.1 评审中确认（P0-1 修复），已是架构事实


## Impact


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
