# ADR-0020: Unified AI Pipeline Call Chain

Date: 2026-08-07

Status: Accepted

> 本 ADR 解决 ADR-0003 与 ADR-0014 在 AI Pipeline 调用链上的冲突，确立唯一权威调用顺序。
> 替代 ADR-0003 的链路定义（ADR-0003 已标记 Deprecated）；ADR-0003 的核心原则（Agent 不直接互调、Agent 产出经 Domain API 落库）由本 ADR 完整继承。

---

## Decision

确立 AI Pipeline 唯一调用链顺序，明确 Orchestrator 所属位置，重申 Agent 产出通过 Domain API 落库的约束。ADR-0003 的链路定义（`Memory Layer → Context Builder → Agent Router → Agent → LLM Provider`）由本 ADR 替代；ADR-0003 的核心原则保留并强化。

## Conflict Being Resolved

| ADR | 原链路定义 | 问题 |
|---|---|---|
| ADR-0003 | `Memory Layer → Context Builder → Agent Router → Agent → LLM Provider` | Context Builder 在 Router 之前，但 Router 作为唯一入口需要先接收请求再触发上下文构建；未定义多角色编排（Orchestrator） |
| ADR-0014 | `外部请求 → Router → Context Builder → Router/Orchestrator 编排角色` | Router 出现两次（入口 + 编排），语义不清晰；未明确 Orchestrator 是否独立于 Router |

两个 Accepted ADR 对同一调用链给出不同且互斥的顺序，违反 SSOT 原则。

## Unified Call Chain

```
外部请求（用户主动查询 / 定时触发 / 通知触发）
  ↓
Safety Gate（ADR-0018，最前置安全检测）
  ├─ Level 2 → 短路 Pipeline，输出安全支持流程
  ├─ Level 1 → 停止普通推荐，温和提供专业支持入口
  ├─ 无信号 → 继续
  ↓
Agent Router（唯一技术入口，ADR-0014 §调用契约）
  ├─ 权限校验、限流、审计、可观测性
  ├─ 决策调用哪些角色、编排顺序
  ↓
Context Builder（由 Router 调用，构建请求上下文）
  ├─ 从 Memory Layer 读取用户记忆 / 偏好 / 推断
  ├─ 从 external_fact 读取事实可信度（ADR-0015）
  ├─ 组装结构化上下文
  ↓
Agent Router / Workflow Orchestrator（编排多角色，属 Router 内部能力）
  ├─ Opportunity Discovery → Proposal Composer → Motivation Engine → Life Curator
  ├─ 角色 Service 之间不直接互调，由 Orchestrator 编排
  ↓
角色 Service（6 角色，ADR-0014）
  ├─ 各角色经 LLM Provider 调用 LLM（角色不直接持有 LLM）
  ├─ Life Curator 只返回 GateDecision
  ↓
Agent Router（汇聚角色产出）
  ├─ Commercial Attribution 附加层（ADR-0017，自然决策后附加）
  ↓
Domain API（Agent 产出经 Domain API 落库，禁止 Agent 直接写库）
  ├─ Today Module：ExperienceProposal / ProposalDecision 等经 Domain API 持久化
  ↓
通知服务（若 GateDecision=OUTPUT_PROPOSAL 且高置信度）
  ├─ 发送前再次校验授权（ADR-0016）/ 场景 / 频率 / 静默时段
  ↓
App 内展示 / 主动提醒
```

### 关键约束

1. **唯一顺序**：Safety Gate → Router → Context Builder → Orchestrator 编排角色 → 角色经 LLM Provider → Router 汇聚 → Domain API 落库 → 通知服务。此顺序为唯一权威，下游文档不得冲突
2. **Router 是唯一技术入口**：所有外部请求必须经 Router，6 角色 Service 不得直接暴露给外部（继承 ADR-0003 + ADR-0014）
3. **Context Builder 由 Router 调用**：Router 接收请求后调用 Context Builder 构建上下文，Context Builder 不在 Router 之前独立运行
4. **Orchestrator 属 Router 内部能力**：Workflow Orchestrator 不是独立服务，是 Router 的内部编排能力；Router 决策调用哪些角色、编排顺序，Orchestrator 执行编排
5. **角色 Service 不直接互调**：6 角色 Service 之间禁止直接调用，必须经 Router/Orchestrator 编排（继承 ADR-0003）
6. **Agent 产出经 Domain API 落库**：Agent 不持有业务状态，不直接持久化业务数据；产出必须通过业务模块的 Domain API 落库，禁止 Agent 直接调用 Repository 或写数据库（继承 ADR-0003，与 ARCHITECTURE §21 AI Boundary 一致）
7. **角色不直接持有 LLM**：角色 Service 经 LLM Provider 调用 LLM，不直接管理 LLM 连接
8. **Memory Layer 不属 6 角色**：Memory Layer、Context Builder、Router 是平台基础设施，6 角色通过接口使用（ADR-0014）

## Reason

- **SSOT 驱动**：ADR-0003 与 ADR-0014 对同一调用链给出互斥顺序，必须统一为唯一权威
- **架构约束**：Router 作为唯一入口（ADR-0003/0014），Context Builder 必须在 Router 之后由 Router 调用，否则 Router 无法获得上下文进行路由决策
- **编排驱动**：ADR-0017 已固定 `Opportunity Discovery → Proposal Composer → Life Curator` 流程，需明确编排者（Orchestrator 属 Router 内部）
- **安全驱动**：ADR-0018 Safety Gate 必须在 Pipeline 最前置，本 ADR 将其纳入唯一调用链

## ADR-0003 Disposition

ADR-0003 已标记为 **Deprecated（2026-08-07）**。处置如下：

| ADR-0003 内容 | 处置 | 替代 |
|---|---|---|
| Agent 必须经 Router 统一路由，禁止 Agent 之间直接互调 | **保留**（核心原则） | 本 ADR §关键约束 2、5 |
| Agent 不拥有业务状态，不直接持久化；产出经 Domain API 落库 | **保留**（核心原则） | 本 ADR §关键约束 6 |
| 链路 `Memory Layer → Context Builder → Agent Router → Agent → LLM Provider` | **替代**（顺序错误） | 本 ADR §Unified Call Chain |
| Agent 之间通过 Memory Layer 共享上下文 | **保留** | 本 ADR（Context Builder 从 Memory Layer 读取） |
| 新增 Agent 须在 ARCHITECTURE §8 与 AGENTS.md 登记 | **保留** | 不变 |

> 架构/代码迁移状态：Implementation Pending。ADR-0003 标记 Deprecated 后，新开发以本 ADR 为准；现有 MockMemoryService / PlannerAgent（Sprint 2）在 Sprint 5 AI Platform 实现时按本 ADR 调用链重构。

## Impact

### 影响模块

- AI Platform：AI Pipeline 调用链按本 ADR 统一；Sprint 5 实现时 Router 内置 Orchestrator + Context Builder 调用
- ARCHITECTURE.md：§7 AI Platform 链路重写（第 4 步）；§8 AI Agent 登记更新
- 所有下游文档：调用链描述以本 ADR 为准

### 需要修改的文档

- ARCHITECTURE.md：§7 AI Pipeline 链路重写为本 ADR 的 Unified Call Chain（第 4 步）；§8 标注 ADR-0003 Deprecated
- DATABASE_DESIGN.md：无直接影响（本 ADR 不新增表）
- ADR-0014：§调用契约 引用本 ADR 作为调用链权威（已在本 PR 同步修正）

### 需要新增/修改的代码

- Sprint 5 AI Platform：Router 实现（含内部 Orchestrator + Context Builder 调用）
- Sprint 5：角色 Service 经 Router/Orchestrator 编排，不直接互调
- Sprint 5：Agent 产出经 Domain API 落库（不直接写 Repository）

### 是否影响现有数据

- 无（本 ADR 为架构调用链决策，无数据变更）

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 ARCHITECTURE.md §7 AI Pipeline 链路
2. Sprint 5：按本 ADR 调用链实现 AI Platform

### Follow-up ADR

- 无（本 ADR 完整定义调用链）
- 未来若 Orchestrator 需独立为服务（如复杂工作流引擎），新建 ADR

### 验证方式

- ARCHITECTURE.md §7 调用链与本 ADR 一致
- Safety Gate 在 Pipeline 最前置（ADR-0018 一致性）
- Router 是唯一技术入口（无角色 Service 直接暴露给外部）
- Context Builder 由 Router 调用（不在 Router 之前独立运行）
- Orchestrator 属 Router 内部（不独立服务）
- 角色 Service 不直接互调（代码审查 + 测试）
- Agent 产出经 Domain API 落库（不直接写 Repository）
- ADR-0003 已标记 Deprecated，核心原则由本 ADR 继承
