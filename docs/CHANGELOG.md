# Changelog


记录 Solo Life OS 所有重要变更。


格式参考 [Keep a Changelog](https://keepachangelog.com/)。

版本号遵循 [Semantic Versioning](https://semver.org/)。


---

## [Unreleased]


### Added

- 初始化 AI 研发管理包目录结构
- 新增 `.ai/AGENTS.md` Agent 工作规范
- 新增 `.ai/CODE_RULES.md` 编码规范
- 新增 `docs/PROJECT_CONTEXT.md` 项目上下文
- 新增 `docs/ARCHITECTURE.md` 系统架构
- 新增 `docs/DATABASE_DESIGN.md` 数据库设计
- 新增 `docs/SPRINT_PLAN.md` Sprint 规划
- 新增 `docs/TASK_BOARD.md` 任务看板


### Changed

- 升级 `docs/PROJECT_CONTEXT.md` v1.0 → v1.1
  - 新增「目标用户」与「用户场景」章节，约束推荐逻辑
  - 新增「用户核心痛点」章节，明确产品价值定位
  - 新增「产品边界」章节，防止功能膨胀
  - 新增「核心竞争力」章节（Personal Life Memory / AI 主动陪伴 / Life Story），指导架构重点
  - 新增「产品发展阶段」Phase 0–4，指导开发优先级
  - 新增「数据资产战略」章节，指导数据库设计
  - 新增「AI 输出原则」（可解释 / 可编辑 / 可拒绝），指导 Agent 行为
  - 新增「隐私原则」与「AI 研发原则」，防止 AI 随意编码
- 升级 `docs/PROJECT_CONTEXT.md` v1.1 → v1.2
  - 重构 §11 AI 架构为 Orchestrator 模式（路由层 + 横向可扩展 Agent），新增 Assistant / Memory Service，禁止 Agent 网状依赖
  - 新增 §16 项目成功标准（Daily Value / AI 价值 / 数据资产 / 情感价值指标）
  - 新增 §17 架构演进原则（初期 Modular Monolith、后期服务拆分条件、核心模块边界与领域接口）
  - 新增 §18 AI 代码生成原则（编码前必读文档清单、禁止事项、新增功能流程）
  - 新增 §19 AI Agent 角色体系（Product / Architecture / Backend / Frontend / AI / QA 六类 Agent，共享 PROJECT_CONTEXT）
  - 新增 §20 文档版本管理（核心文档版本规则与修改记录要求）


### Added (Git 协作基础设施)


- 新建 `develop` 分支作为研发集成分支，`main` 仅承载稳定版本
- 新增 `README.md` 项目入口（文档导航 / 技术栈 / 分支策略摘要）
- 新增 `.gitignore`（Java / Node / uni-app / IDE / OS / 凭据）
- 新增 `.github/PULL_REQUEST_TEMPLATE.md`（变更类型 / 架构影响 / 文档更新 / 测试情况）
- 新增 `docs/AI_CHANGELOG.md`，记录 AI Agent 行为日志，与产品 CHANGELOG 区分
- 升级 `docs/AGENTS.md`：纳入 §3 Agent 权限分级 / §5 Git 协作规范 / §5.2 AI 分支规则 / §5.4 PR 流程 / §7 完成任务后必更清单


### Changed (目录结构调整)


- 迁移 `.ai/AGENTS.md` → `docs/AGENTS.md`（升级版）
- 迁移 `.ai/CODE_RULES.md` → `docs/CODE_RULES.md`（补充 commit scope 规范）
- 删除空的 `.ai/` 目录


### Changed (AGENTS.md v1.0 → v1.1)


- 升级 `docs/AGENTS.md` v1.0 → v1.1
  - 新增 §7 Task Ownership（任务卡片格式 / Primary Owner / 领取前检查）
  - 新增 §8 Architecture Change Process（架构变更必须先评估后开发）
  - 新增 §9 AI 提交前检查（Context / Architecture / Database / Code / Documentation / Git 六类自检）
  - 新增 §10 Agent Handoff Protocol（Changed / API / Database / Dependency / Next Agent 交接格式）
  - §3 权限调整：Architecture Agent 改为可写 `database/design/`，禁止 `database/migrations/`（迁移归 Backend Agent）
  - §6 禁止事项补充：多 Agent 同改同模块、先写代码再补架构
  - §12 完成任务后必更清单指向 `docs/CHANGELOG.md`（原根目录 CHANGELOG 已迁移）


### Changed (CHANGELOG 位置调整)


- `CHANGELOG.md` → `docs/CHANGELOG.md`（使用 `git mv` 保留历史，便于根目录整洁）
- 新增根目录 `AGENTS.md` 作为 AI Coding Agent 入口，指向 `docs/AGENTS.md`
- 更新 `README.md` 文档导航表，同步链接调整


### Changed (AGENTS.md v1.1 → v1.2)


- 升级 `docs/AGENTS.md` v1.1 → v1.2
  - §5.1 分支策略新增 `hotfix/*`（生产紧急修复通道）与 `docs/*`（文档独立生命周期）分支类型
  - §7 Task Ownership 新增任务生命周期状态机（Backlog→Assigned→Designing→Developing→Reviewing→Testing→Done→Archived）
  - 新增 §11 Prompt 文件管理规则（受控变更，禁止无记录修改 Prompt，防漂移）
  - 新增 §12 Repository Structure（仓库目录地图，禁止擅建根目录）
  - 原 §11/§12 顺延为 §13 文档版本管理 / §14 完成任务后必更
- 升级 `docs/AI_CHANGELOG.md`，固定条目格式（Agent/Task/Action/Reason/Impact/Reviewer）


### Changed (ARCHITECTURE.md v1.0 → v2.0)


- 全量升级 `docs/ARCHITECTURE.md`，从「系统拓扑图」升级为「研发约束文档」
  - 新增 §1 Architecture Principles（DDD 轻量版 + Modular Monolith + AI Native）
  - 新增 §2 Layer Architecture（Controller/Application/Domain/Repository 分层与禁止规则）
  - 新增 §3 Shared Domain（8 个共享核心 Entity，禁止重复定义）
  - 新增 §4 Module Dependencies（模块依赖图与单向依赖规则）
  - 新增 §5 升级版总体架构图（Client → Spring Boot → AI Platform → 持久化四层）
  - 新增 §7 AI Platform 完整链路（Memory → Context → Router → Agent → LLM Provider 抽象层）
  - 新增 §9 Event Flow（事件流解耦 + 典型事件表）
  - 新增 §10 Persistence（PostgreSQL + Redis + Vector DB + OSS 四存储）
  - 新增 §11 API Boundary（前端禁直调 AI、统一返回格式）
  - 新增 §12 Repository Structure（仓库目录地图，与 AGENTS.md §12 对齐）
  - 新增 §13 Evolution Roadmap（Phase 0–4 演进路线与触发条件）


### Changed (ARCHITECTURE.md v2.0 → v2.1)


- 升级 `docs/ARCHITECTURE.md` v2.0 → v2.1，纳入企业级研发治理
  - §5 总体架构图：App → Spring Boot Modular Monolith，统一使用 Module 命名
  - §6 服务设计 → 模块设计，全文 Service → Module（与 Phase 0 一致）
  - 新增 §14 ADR（Architecture Decision Record）机制
  - 新增 §15 NFR（性能 P95 / 可用性 / 可扩展性 / 隐私）
  - 新增 §16 Observability（Logging / Metrics / Tracing / Audit）
  - 新增 §17 Security Boundary（认证 / 授权 / 限流 / 加密）
  - 新增 §18 Integration Boundary（外部集成通过 Adapter 层）
  - 新增 §19 Package Convention（后端包结构固定）
  - 新增 §20 Error Handling（统一异常体系与错误码）
  - 新增 §21 AI Boundary（AI 永远不能直连数据库，必须经 Domain API）
  - 新增 §22 Data Ownership（每个核心数据对象唯一 Owner 模块）
- 新增 `docs/architecture/ADR/` 目录与 4 份初始 ADR：
  - ADR-0001 采用 Modular Monolith
  - ADR-0002 选择 PostgreSQL 作为主数据库
  - ADR-0003 AI Agent 统一经 Router 路由
  - ADR-0004 MVP 阶段不使用微服务


### Changed (DATABASE_DESIGN.md v1.0 → v2.0)


- 全量升级 `docs/DATABASE_DESIGN.md`，从「领域模型草稿」升级为「开发基线」
  - 新增 §1 Design Principles + §2 Naming Convention
  - 新增 §3 Shared Entities（与 ARCHITECTURE §3 对齐）
  - 新增 §4 Entity Ownership（每张表唯一 Owner 模块）
  - 新增 §5 ER Diagram（实体关系图）
  - 重写 §6 Table Design：每张表含完整字段说明（类型 / Nullable / 默认值 / 描述）
  - 新增 §7 Enum Definition（12 类枚举显式定义，禁止自由字符串）
  - 新增 §8 Index Strategy（15 个索引，对齐 §15 NFR 性能指标）
  - 新增 §9 Constraint Strategy（逻辑关联不建 FK + 唯一约束 + 非空约束）
  - 新增 §10 Migration Rule（命名规范 + 幂等 + 破坏性变更需 ADR）
  - 新增 §11 Version History + §12 与其他文档对齐
  - 修正 Activity.location → location_id（关联 Location Entity）
  - 修正 Favorite 增加 UNIQUE(user_id, target_type, target_id)
  - 扩展 ai_memory：新增 memory_type / source / summary / embedding_id / visibility 5 字段


### Deprecated


### Removed


### Fixed


### Security


---

## 变更类型说明

- `feat` 新增功能
- `fix` 修复缺陷
- `refactor` 重构
- `docs` 文档变更
- `chore` 构建/工程任务
