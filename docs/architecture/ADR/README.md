# ADR Index

Solo Life OS Architecture Decision Records

> 本文件是所有 ADR 的唯一入口。新建 ADR 必须在此登记，并使用 [template.md](./template.md)。
> 状态机：Proposed → Accepted → Deprecated（不可逆向回滚，废弃后新建 ADR 替代）。

---

## Accepted ADR

| ADR | Title | Status | File |
|-----|-------|--------|------|
| ADR-0001 | Modular Monolith Architecture | Accepted | [ADR-0001-modular-monolith.md](./ADR-0001-modular-monolith.md) |
| ADR-0002 | PostgreSQL as Primary Database | Accepted | [ADR-0002-postgresql-as-primary-db.md](./ADR-0002-postgresql-as-primary-db.md) |
| ADR-0003 | AI Agent Unified Router | Accepted | [ADR-0003-ai-agent-unified-router.md](./ADR-0003-ai-agent-unified-router.md) |
| ADR-0004 | No Microservices in MVP | Accepted | [ADR-0004-no-microservices-in-mvp.md](./ADR-0004-no-microservices-in-mvp.md) |
| ADR-0006 | JWT Authentication | Accepted | [ADR-0006-jwt-authentication.md](./ADR-0006-jwt-authentication.md) |
| ADR-0011 | Activity Ownership | Accepted | [ADR-0011-activity-ownership.md](./ADR-0011-activity-ownership.md) |

## Proposed ADR

| ADR | Title | Status | File |
|-----|-------|--------|------|
| ADR-0005 | Vector DB Adapter Strategy | Proposed | [ADR-0005-vector-db-adapter-strategy.md](./ADR-0005-vector-db-adapter-strategy.md) |
| ADR-0010 | Tag Ownership | Proposed | [ADR-0010-tag-ownership.md](./ADR-0010-tag-ownership.md) |

## Future ADR

| ADR | Title | Sprint | Note |
|-----|-------|--------|------|
| ADR-0007 | Map Provider Adapter | Sprint 3 | Explore Module 启动时创建 |
| ADR-0008 | LLM Provider Strategy | Sprint 5 | AI Platform 启动时创建 |
| ADR-0009 | Payment Adapter | Sprint 7 | Community 商业化时创建 |

---

## ADR 生命周期规则

1. **Proposed**：决策方向已明确，待验证。允许在对应 Sprint 推进过程中确认。
2. **Accepted**：决策已生效，成为架构事实。修改必须新建 ADR 替代，不可直接改写。
3. **Deprecated**：决策已废弃。保留文件供追溯，但不再生效。

## 创建规则

- 文件命名：`ADR-XXXX-<kebab-case-title>.md`
- 编号规则：连续递增，不复用废弃编号
- 必须使用 [template.md](./template.md) 模板
- 创建后必须在本 Index 登记
- 对齐 AGENTS §8 Architecture Change Process：必须经 Architecture Agent 审核
