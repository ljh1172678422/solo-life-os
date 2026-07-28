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


- 所有业务表存 PostgreSQL，遵循 §3 Shared Domain
- 图片 / 头像 / AI 生成图片一律存 OSS，禁止入库
- AI 语义记忆存 Vector DB，与主库解耦
- 禁止使用 MySQL 专属语法，保证未来可迁移
