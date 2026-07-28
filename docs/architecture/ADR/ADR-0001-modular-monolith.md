# ADR-0001: 采用 Modular Monolith 架构


Date:    2026-07-28

Status:  Accepted


## Decision


Solo Life OS 在 Phase 0–2 阶段采用 Modular Monolith（模块化单体）架构，所有业务模块同进程部署，通过领域接口（Port + Adapter）交互。


## Reason


- MVP 阶段团队 / AI Agent 规模有限，微服务会带来运维与一致性成本
- 模块化设计保证领域边界清晰，未来拆分服务时无需重构代码
- 单体便于 AI Agent 在长周期内持续维护，避免分布式调试难题
- 与 Evolution Roadmap §13 一致：先稳定边界，再按需拆分


## Impact


- 后端代码组织为单一 Spring Boot 应用，包结构按模块划分
- 模块间禁止跨包直连，必须通过 Domain API
- 数据库共享，但表按模块前缀归属（详见 §22 Data Ownership）
- 未来如某模块性能压力上升，可按 Phase 3 触发条件拆出独立服务
