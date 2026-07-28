# ADR-0010: Tag Ownership


Date:    2026-07-28

Status:  Proposed


## Decision


Tag 属于 Shared Kernel，Owner 为 Architecture（全局共享领域）。

tag 表归 Shared Domain，业务模块（User / Mood / Activity / Location / Story 等）禁止直接操作 tag 表，必须通过 Tag Domain API 访问。


```
Shared Kernel（Architecture 维护）
       │
       └── tag 表
            │
            ├── Tag Domain API（唯一写入入口）
            │
            ▼
  ┌────┬────┬────┬────┬────┐
  │    │    │    │    │    │
User Mood Today Explore Story ...（只读引用）
```


## Reason


- Tag 被 User / Mood / Activity / Location / Story 多模块引用，归属任一业务模块会导致反向依赖
- 若归属 User Module，未来 AI Memory、Recommendation、Story 会反向依赖 User Module，破坏 ARCHITECTURE §4 模块依赖单向规则
- Tag 本质是横切关注点（cross-cutting concern），是共享词汇表，不属于单一业务领域
- 与 ARCHITECTURE §3 Shared Domain 一致：Tag 已被列为全部模块共享的核心 Entity
- 未来会自然出现 mood_tag / goal_tag / memory_tag 等关联，归 Shared Kernel 更合理


## Impact


- 本 ADR 被 Accepted 后，DATABASE_DESIGN §4 Entity Ownership 需将 tag Owner 从 "User" 改为 "Shared Kernel / Architecture"
- tag 表 schema 由 Architecture 维护，各模块通过 Tag Domain API 读写
- 禁止各模块自建 tag 表或本地标签表
- Sprint 1 User Module 不再独占 Tag Repository，改由 Shared Kernel 提供 Tag Domain API
- 与 ARCHITECTURE §22 Data Ownership 对齐（需同步更新 Owner 列）


## Status Transition


- Proposed（Sprint 0）：本状态，决策方向为 Tag → Shared Kernel
- Accepted：经 Architecture Review 通过后，同步更新 DATABASE_DESIGN §4 与 ARCHITECTURE §22
