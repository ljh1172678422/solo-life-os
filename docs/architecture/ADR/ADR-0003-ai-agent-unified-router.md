# ADR-0003: AI Agent 统一经 Router 路由


Date:    2026-07-28

Status:  Deprecated（2026-08-07）

> **Deprecated 说明**：本 ADR 的链路定义（`Memory Layer → Context Builder → Agent Router → Agent → LLM Provider`）由 [ADR-0020](./ADR-0020-unified-ai-pipeline-call-chain.md) 替代。核心原则（Agent 经 Router 统一路由、禁止 Agent 间直接互调、Agent 产出经 Domain API 落库）由 ADR-0020 完整继承。
> 架构/代码迁移状态：Implementation Pending（Sprint 5 AI Platform 实现时按 ADR-0020 调用链重构）。


## Decision


所有 AI Agent（Planner / Recommendation / Emotion / Story / Assistant 等）必须经 Agent Router 统一路由，禁止 Agent 之间直接相互调用。

Agent 不拥有业务状态，不直接持久化业务数据。Agent 的产出必须通过业务模块的 Domain API 落库，禁止 Agent 直接调用 Repository 或写数据库。


## Reason


- 统一入口便于权限校验、限流、审计、可观测性（详见 §16 / §17）
- 防止 Agent 间形成网状依赖导致行为不可追溯
- Router 可基于上下文决策调用哪个 Agent，对业务层透明
- 未来新增 Agent（如 Travel / Finance / Health）只需在 Router 注册，无需改动业务模块


## Impact


- AI Platform 链路固定为：Memory Layer → Context Builder → Agent Router → Agent → LLM Provider
- Agent 之间需要协作时，通过 Memory Layer 共享上下文
- 任何新增 Agent 必须在 ARCHITECTURE.md §8 与 AGENTS.md 登记
- 业务模块调用 AI 时，只感知 Router 接口，不感知具体 Agent 实现
- Agent 产出落库的正确链路：Agent → Domain API → Business Module → Repository
- 禁止 Agent 直接持有 / 修改业务 Entity，禁止 Agent 直接写数据库（与 §21 AI Boundary 一致）
