# ADR-0005: Vector DB Adapter Strategy


Date:    2026-07-28

Status:  Proposed


## Decision


采用 VectorStoreAdapter 抽象模式隔离 Vector DB 实现。

Sprint 0 仅定义 VectorStoreAdapter Interface（TASK-0005），不选定具体 Vector DB Provider。具体 Provider（pgvector / Milvus / Qdrant）在 Sprint 5 AI Platform 启动时结合真实 Memory 数据量决策。

业务代码与 AI Memory 层禁止直接依赖具体 Vector DB 实现，必须通过 VectorStoreAdapter 接口访问。


```
AI Memory Layer
       │
       ▼
VectorStoreAdapter（Interface，Sprint 0 定义）
       │
       ├── pgvector（复用 PostgreSQL，轻量）
       ├── Milvus（独立部署，大规模）
       └── Qdrant（轻量级，Rust 实现）

       Provider 在 Sprint 5 决定
```


## Reason


- Sprint 0 阶段尚无 Memory 数据，提前部署 Vector DB 实例增加 Docker / CI 复杂度但无数据可存
- Vector DB 选型需结合 Sprint 5 真实 Memory 数据量与检索性能需求，当前信息不足
- Adapter 模式保证未来切换 Provider 时业务代码零修改
- 与 ARCHITECTURE §18 Integration Boundary 一致：外部依赖通过 Adapter 隔离
- 与 ARCHITECTURE §10 Persistence 一致：Vector DB 仅用于 AI 语义检索


## Impact


- Sprint 0（TASK-0005 AI Foundation）：仅创建 VectorStoreAdapter Interface，不部署实例
- Sprint 5（AI Platform）：决策具体 Provider 并实现 Adapter
- 候选方案与评估维度（Sprint 5 决策时使用）：
  - pgvector：复用 PostgreSQL，运维成本低，适合中小规模
  - Milvus：独立部署，支持大规模向量，运维成本高
  - Qdrant：轻量级，Rust 实现，性能优秀
  - 评估维度：数据量、检索延迟、运维成本、与 PostgreSQL 集成度
- 禁止：AI Memory 层直接 import 具体向量数据库 SDK
- 禁止：在 Sprint 0 部署 Vector DB 实例（延后至 Sprint 5）


## Status Transition


- Proposed（Sprint 0）：本状态，仅定 Adapter 方向
- Accepted（Sprint 5 启动时）：选定具体 Provider 后转为 Accepted
