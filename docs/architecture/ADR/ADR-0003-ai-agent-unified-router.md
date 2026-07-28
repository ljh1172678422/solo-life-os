# ADR-0003: AI Agent 统一经 Router 路由


Date:    2026-07-28

Status:  Accepted


## Decision


所有 AI Agent（Planner / Recommendation / Emotion / Story / Assistant 等）必须经 Agent Router 统一路由，禁止 Agent 之间直接相互调用。


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
