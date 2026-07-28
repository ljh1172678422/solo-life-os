# ADR-0002: 选择 PostgreSQL 作为主数据库


Date:    2026-07-28

Status:  Accepted


## Decision


Solo Life OS 主数据库采用 PostgreSQL，配合 Redis（缓存）、Vector DB（AI 检索）、Object Storage（文件）形成完整持久化体系。


## Reason


- PostgreSQL 支持 JSONB / 全文检索 / 地理空间（PostGIS），适合生活数据的多样化结构
- 强事务保证，避免 NoSQL 在多表关联上的劣势
- 开源成熟，社区生态完善，AI Agent 熟悉度高
- Redis / Vector DB / OSS 各司其职，避免主库承担过多职责


## Impact


- 所有业务数据默认存 PostgreSQL，遵循 §3 Shared Domain
- 特殊数据按存储分工，禁止混用：
  - Cache / 会话 / 排行 → Redis
  - Semantic Memory / 向量检索 → Vector DB
  - Binary Asset（图片 / 头像 / AI 生成图片） → Object Storage
- 禁止使用 Redis 作为主数据源（仅缓存与短期会话）
- 禁止使用 Vector DB 保存业务事实数据（仅存向量与 embedding_id 引用，事实数据归 PostgreSQL）
- 禁止使用 MySQL 专属语法，保证未来可迁移
- 所有表必须含审计字段（created_time / updated_time），优先软删除
