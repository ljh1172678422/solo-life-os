# ADR-0004: MVP 阶段不使用微服务


Date:    2026-07-28

Status:  Accepted


## Decision


Solo Life OS 在 MVP 阶段（Phase 0–2）严格使用 Modular Monolith，禁止引入 Spring Cloud / Dubbo 等微服务框架。


## Reason


- AI 项目最大的架构风险不是「不够先进」，而是「频繁重构」
- 微服务化需要模块边界已稳定，当前阶段边界仍在演进
- 微服务带来分布式事务 / 服务发现 / 链路追踪等额外复杂度，MVP 阶段无法负担
- Modular Monolith 已通过领域接口隔离模块，未来可平滑拆分


## Impact


- 禁止引入 Spring Cloud / Dubbo / gRPC 跨进程通信框架
- 模块间通过同进程领域接口调用
- 未来如某模块满足 Phase 3 触发条件，需先写 ADR 说明拆分方案
- 与 §13 Evolution Roadmap 完全一致，禁止跳跃式演进
