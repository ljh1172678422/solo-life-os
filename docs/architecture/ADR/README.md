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
| ADR-0007 | Map Provider Adapter | Accepted | [ADR-0007-map-provider-adapter.md](./ADR-0007-map-provider-adapter.md) |
| ADR-0012 | Product Module Boundary Revision | Accepted | [ADR-0012-product-module-boundary-revision.md](./ADR-0012-product-module-boundary-revision.md) |
| ADR-0013 | Today Core Object Lifecycle Refactor | Accepted | [ADR-0013-today-core-object-lifecycle-refactor.md](./ADR-0013-today-core-object-lifecycle-refactor.md) |
| ADR-0014 | AI Platform Six Roles and Confidence Gating | Accepted | [ADR-0014-ai-platform-six-roles-and-confidence-gating.md](./ADR-0014-ai-platform-six-roles-and-confidence-gating.md) |
| ADR-0015 | External Fact Trustworthiness Model | Accepted | [ADR-0015-external-fact-trustworthiness-model.md](./ADR-0015-external-fact-trustworthiness-model.md) |
| ADR-0016 | Passive Sensing Consent Boundary | Accepted | [ADR-0016-passive-sensing-consent-boundary.md](./ADR-0016-passive-sensing-consent-boundary.md) |
| ADR-0017 | Commercial Recommendation Boundary | Accepted | [ADR-0017-commercial-recommendation-boundary.md](./ADR-0017-commercial-recommendation-boundary.md) |
| ADR-0018 | Mental Health Boundary and Immediate Safety Support Flow | Accepted | [ADR-0018-mental-health-safety-gate.md](./ADR-0018-mental-health-safety-gate.md) |
| ADR-0019 | LifeResponseMap / ai_memory Ownership and Data Governance | Accepted | [ADR-0019-life-response-map-ai-memory-governance.md](./ADR-0019-life-response-map-ai-memory-governance.md) |

## Proposed ADR

| ADR | Title | Status | File |
|-----|-------|--------|------|
| ADR-0005 | Vector DB Adapter Strategy | Proposed | [ADR-0005-vector-db-adapter-strategy.md](./ADR-0005-vector-db-adapter-strategy.md) |
| ADR-0010 | Tag Ownership | Proposed | [ADR-0010-tag-ownership.md](./ADR-0010-tag-ownership.md) |

## Future ADR

| ADR | Title | Sprint | Note |
|-----|-------|--------|------|
| ADR-0008 | LLM Provider Strategy | Sprint 5 | AI Platform 启动时创建 |

## Deprecated ADR

| ADR | Title | Status | 替代来源 | File |
|-----|-------|--------|--------|------|
| ADR-0011 | Activity Ownership | Deprecated（2026-08-07） | Activity Ownership / Explore 引用条款 → [ADR-0013](./ADR-0013-today-core-object-lifecycle-refactor.md)；CommunityEvent 独立领域实体条款 → [ADR-0012](./ADR-0012-product-module-boundary-revision.md) | [ADR-0011-activity-ownership.md](./ADR-0011-activity-ownership.md) |

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
