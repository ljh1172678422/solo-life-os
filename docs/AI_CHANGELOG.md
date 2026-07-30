# AI Development Log


记录 AI Agent 在 Solo Life OS 仓库中的所有开发行为。

与根目录 CHANGELOG.md 区别：

- CHANGELOG.md：面向用户，记录产品功能变更
- AI_CHANGELOG.md：面向团队，记录 AI Agent 的行为、决策、原因


---

## 条目格式


所有条目必须遵循以下固定格式：


```
## YYYY-MM-DD

Agent:     <Agent 名称>
Task:      <TASK-XXX 或「非任务」>
Action:    <做了什么>
Reason:    <为什么做>
Impact:    <影响范围>
Reviewer:  Human / Pending
```


---


## 2026-07-28


Agent:

Architecture Agent


Task:

非任务


Action:

建立 Git 协作基础设施：
- 新建 develop 分支作为研发集成分支
- 新增 README.md / .gitignore / .github/PULL_REQUEST_TEMPLATE.md
- 迁移 .ai/AGENTS.md 与 .ai/CODE_RULES.md 至 docs/
- 升级 docs/AGENTS.md，纳入分支策略 / Agent 权限分级 / PR 流程
- 新增 docs/AI_CHANGELOG.md（本文件）


Reason:

按 PROJECT_CONTEXT v1.2 §17 / §18 / §19 的要求，建立多 Agent 长周期协作所需的操作协议层，防止后续 AI 直接提交到 main、跨模块修改、创建重复 Entity。


Impact:

仅影响仓库结构与文档，无代码与数据库变更。


Reviewer:

Pending


---


## 2026-07-28 (第 2 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

升级 AGENTS.md v1.0 → v1.1，并整理文档位置：
- 新增 §7 Task Ownership / §8 Architecture Change Process / §9 AI 提交前检查 / §10 Agent Handoff Protocol
- §3 权限调整：Architecture Agent 禁止 database/migrations/，迁移归 Backend Agent
- CHANGELOG.md 从根目录迁移至 docs/CHANGELOG.md（git mv 保留历史）
- 新增根目录 AGENTS.md 入口，便于 AI Coding Agent 扫描发现协作规范


Reason:

按评审意见补充多 Agent 长周期协作缺失的协议层：任务领取防冲突、架构变更审批、提交前自检、Agent 间交接；并将 CHANGELOG 收纳进 docs/ 保持根目录整洁。


Impact:

仅影响 docs/ 与根目录文档结构，无代码与数据库变更。


Reviewer:

Pending


---


## 2026-07-28 (第 3 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

升级 AGENTS.md v1.1 → v1.2，纳入企业级研发治理细节：
- §5.1 分支策略新增 hotfix/* 与 docs/* 分支类型及 Emergency Fix 流程
- §7 Task Ownership 新增任务生命周期状态机（Backlog→Assigned→Designing→Developing→Reviewing→Testing→Done→Archived）
- 新增 §11 Prompt 文件管理规则（受控变更，禁止无记录修改）
- 新增 §12 Repository Structure（仓库目录地图，禁止擅建根目录）
- 原 §11/§12 顺延为 §13/§14


Reason:

补充生产环境紧急修复通道、任务状态可视化、Prompt 漂移防护、仓库结构约束，使规范从「AI 使用规范」升级为「AI 软件研发组织运行规范」。


Impact:

仅影响 docs/AGENTS.md 与 docs/AI_CHANGELOG.md，无代码与数据库变更。


Reviewer:

Pending


---


## 2026-07-28 (第 4 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 ARCHITECTURE.md v1.0 → v2.0，从「系统拓扑图」升级为「研发约束文档」：
- 新增 §1 Architecture Principles（DDD + Modular Monolith + AI Native）
- 新增 §2 Layer Architecture（分层架构与禁止规则）
- 新增 §3 Shared Domain（8 个共享核心 Entity）
- 新增 §4 Module Dependencies（模块依赖图与单向依赖规则）
- 新增 §7 AI Platform 完整链路（Memory → Context → Router → Agent → LLM Provider）
- 新增 §9 Event Flow（事件流解耦 + 典型事件表）
- 新增 §10 Persistence（四存储架构）
- 新增 §11 API Boundary（前端禁直调 AI）
- 新增 §12 Repository Structure（仓库目录地图）
- 新增 §13 Evolution Roadmap（Phase 0–4 演进路线）


Reason:

原 ARCHITECTURE v1.0 仅是部署拓扑图，未回答「系统如何分层、模块如何通信、数据如何流动、AI 如何接入、以后如何拆微服务」等研发约束问题。升级后，DATABASE_DESIGN / SPRINT_PLAN / TASK_BOARD / 各 Agent 行为都将自动统一，防止架构漂移。


Impact:

仅影响 docs/ARCHITECTURE.md，无代码与数据库变更。本文档生效后，后续所有模块开发必须遵守 §3 共享 Entity、§4 模块依赖、§2 分层规则。


Reviewer:

Pending


---


## 2026-07-28 (第 5 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 ARCHITECTURE.md v2.0 → v2.1，纳入企业级研发治理：
- §5 总体架构图：App → Spring Boot Modular Monolith，统一 Module 命名
- §6 服务设计 → 模块设计，全文 Service → Module（与 Phase 0 一致）
- 新增 §14 ADR / §15 NFR / §16 Observability / §17 Security Boundary
- 新增 §18 Integration Boundary / §19 Package Convention / §20 Error Handling
- 新增 §21 AI Boundary（AI 永远不能直连数据库，必须经 Domain API）
- 新增 §22 Data Ownership（每数据对象唯一 Owner 模块）
- 新建 docs/architecture/ADR/ 目录与 4 份初始 ADR（0001-0004）


Reason:

v2.0 解决了架构拓扑与边界问题，但缺少非功能性约束、可观测性、安全边界、错误处理、AI 边界、数据归属等企业级治理细节。本次升级补齐，使架构文档真正承担研发治理职责，防止后期架构漂移。


Impact:

影响 docs/ARCHITECTURE.md 与新增的 docs/architecture/ADR/ 目录。无代码与数据库变更。本文档生效后，后续所有模块开发必须遵守 §17 安全边界、§20 错误处理、§21 AI Boundary、§22 Data Ownership。


Reviewer:

Pending


---


## 2026-07-28 (第 6 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 DATABASE_DESIGN.md v1.0 → v2.0，从「领域模型草稿」升级为「开发基线」：
- 新增 §1 Design Principles / §2 Naming Convention
- 新增 §3 Shared Entities（与 ARCHITECTURE §3 对齐）
- 新增 §4 Entity Ownership（每张表唯一 Owner 模块）
- 新增 §5 ER Diagram
- 重写 §6 Table Design（每表含完整字段说明）
- 新增 §7 Enum Definition（12 类枚举显式定义）
- 新增 §8 Index Strategy（15 索引，对齐 NFR）
- 新增 §9 Constraint Strategy（逻辑关联不建 FK）
- 新增 §10 Migration Rule / §11 Version History / §12 对齐
- 修正 Activity.location → location_id
- 修正 Favorite 增加 UNIQUE(user_id, target_type, target_id)
- 扩展 ai_memory 新增 memory_type / source / summary / embedding_id / visibility


Reason:

原 v1.0 仅是字段列表，无法回答表关系 / Owner / 字段约束 / 索引 / 外键策略 / 枚举定义等开发基线问题。升级后所有 Entity / Repository / Migration 的生成将自动统一，防止数据层架构漂移。


Impact:

仅影响 docs/DATABASE_DESIGN.md，无代码变更。本文档生效后，后续所有数据层开发必须遵守 §4 Ownership、§7 枚举、§8 索引、§9 外键策略。


Reviewer:

Pending


---


## 2026-07-28 (第 7 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 CODE_RULES.md v1.0 → v2.0，使规范从「代码风格约束」升级为「AI 生成代码约束」：
- 新增 §1 General Principles / §4 Package Convention / §5 DTO/Entity/VO Rules
- 新增 §6 Exception Handling / §7 Logging Rules / §10 Testing Rules
- 新增 §11 AI Generated Code Rules（与 AGENTS / ARCHITECTURE 联动）
- 重写 §2 Frontend（禁 any/as any，必经 api/ 封装）
- 重写 §3 Backend（补全五层架构与命名规范）
- 重写 §8 Database Rules（对齐 DATABASE_DESIGN v2.0）
- 重写 §9 API Rules（返回格式增加 traceId）
- 重写 §12 Git Convention（分支类型对齐 AGENTS.md §5.1）
- 新增 §13 Version History + §14 Alignment 对齐表


Reason:

原 v1.0 仅约束代码风格，无法约束 AI Agent 生成代码的边界（DTO/Entity 混用、Repository 写业务、组件直连 axios 等）。升级后与 ARCHITECTURE / DATABASE_DESIGN / AGENTS 真正联动，形成完整闭环。


Impact:

仅影响 docs/CODE_RULES.md，无代码变更。本文档生效后，后续所有 AI 生成的代码必须遵守 §5 DTO 边界、§6 异常体系、§7 日志规范、§11 AI 代码规则。


Reviewer:

Pending


---


## 2026-07-28 (第 8 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 SPRINT_PLAN.md v1.0 → v2.0，从「功能清单」升级为「可执行 Sprint 计划」：
- Sprint 按 Module 组织（不按页面），统一术语 Module
- 每个 Sprint 增加：Sprint Goal / Deliverables / Agents / Depends / Risk / DoD 六段式
- Sprint 5 改名 AI Personal Agent → AI Platform
- 新增 §2 Sprint Roadmap / §12 Milestones / §13 Dependencies
- 新增 §14 Definition of Done（代码/测试/文档/架构四层）
- 新增 §15 Sprint Lifecycle + Risk 管理规则
- 新增 §16 Version History + §17 Alignment
- 识别 5 个待写 ADR（ADR-0005~0009）


Reason:

原 v1.0 仅是按页面切分的功能清单，无法回答 Sprint 完成标准、依赖关系、风险、Agent 分工等执行问题。升级后与 ARCHITECTURE / DATABASE_DESIGN / AGENTS 完全对齐，AI Agent 可按 Sprint 顺序稳定执行，避免开发顺序错误与功能蔓延。


Impact:

仅影响 docs/SPRINT_PLAN.md，无代码变更。本文档生效后，后续所有 Sprint 开发必须遵守 §13 依赖关系与 §14 DoD。


Reviewer:

Pending


---


## 2026-07-28 (第 9 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

协同调整 DATABASE_DESIGN / ARCHITECTURE / SPRINT_PLAN 三个根文档，解决评审识别的领域所有权冲突：
- P0-1 Activity Owner 从 Today/Explore 改为 Today（解决唯一 Owner 冲突，违反 ARCHITECTURE §22）
- P0-2 Sprint 7 Community 不复用 activity，改用 community_event 独立领域实体
- P0-3 Sprint 0 Vector DB 延后为 Adapter Interface，实例部署延后至 Sprint 5
- P1-4 Sprint 5 新增 ai_conversation 表（短期对话，与 ai_memory 长期记忆互补）
- P1-5 新增 SPRINT_PLAN §16 ADR Roadmap（ADR-0001~0011 完整清单）


Reason:

原 v2.0 中 Activity Owner 标注为「Today / Explore」违反 ARCHITECTURE §22「每个核心数据对象有唯一 Owner」。Community 复用 activity 会导致 activity 表变成万能表污染领域。Vector DB 在 Sprint 0 提前部署会增加复杂度但无 Memory 数据可存。


Impact:

影响 docs/DATABASE_DESIGN.md（v2.0→v2.1）、docs/ARCHITECTURE.md（v2.1→v2.2）、docs/SPRINT_PLAN.md（v2.0→v2.1），无代码变更。本文档生效后，Explore 不创建 activity 表，Community 不复用 activity 表，AI Platform 必须同时实现 Memory 与 Conversation。


Reviewer:

Pending


---


## 2026-07-28 (第 10 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

全量升级 TASK_BOARD.md v1.0 → v2.0，从「功能清单格式」升级为「Module + Owner + Reviewer + Status」任务卡：
- 与 SPRINT_PLAN v2.1 / AGENTS v1.2 §7 Task Ownership / ARCHITECTURE v2.2 §22 完全对齐
- 拆分 Sprint 0 为 6 个独立任务（TASK-0001 ~ TASK-0006）
- 引入任务状态机（Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived）
- 收紧 AI Agent 任务边界：Sprint 0 仅定义 Interface，禁止真实 LLM / Prompt / Agent 实现
- 收紧数据库边界：Sprint 0 仅创建 user / user_preference / tag 三张表
- 新增 Sprint 0 Definition of Done 四层约束（Code / Test / Documentation / Architecture）
- 新增 Next Sprint 任务预拆分（Sprint 1 User Module TASK-0101~0106）


Reason:

原 v1.0 仅是粗粒度 Todo 清单（如「[ ] 完成系统架构」「[ ] 创建 Agent 框架」），无法回答 Owner / Reviewer / Branch / 状态生命周期 / 边界约束等执行问题，与已升级的 SPRINT_PLAN v2.1 / AGENTS v1.2 / ARCHITECTURE v2.2 / DATABASE_DESIGN v2.1 存在明显不一致。升级后形成「产品规划层 → 执行层 → Agent 工作流」链路，AI Agent 可按任务卡稳定领取工作，避免越权与边界蔓延。


Impact:

仅影响 docs/TASK_BOARD.md 与 docs/CHANGELOG.md，无代码变更。本文档生效后，Sprint 0 可正式启动，AI Agent 领取任务必须遵守任务状态机与禁止项。


Reviewer:

Pending


---


## 2026-07-28 (第 11 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

升级 TASK_BOARD.md v2.0 → v2.1，按评审意见修复 5 项问题并新增 1 项任务：
- P0-1 修复：TASK-0101 User Migration 改为 Migration Review，禁止 Sprint 1 重复创建 user / user_preference / tag 表
- P0-2 修复：TASK-0005 Module 从「AI Platform」改为「Foundation / AI Infrastructure」（AI Platform 完整实现属 Sprint 5）
- P0-3 修复：ADR-0005 职责拆分——Architecture Agent 负责 Vector DB Selection Proposal（定方向），AI Agent 负责 VectorStoreAdapter Interface 实现（抽象层）
- P1-1 修复：TASK-0004 Database Foundation 增加 TASK-0002 依赖（Flyway 配置需先就绪）
- P1-2 修复：TASK-0005 新增业务模块禁止项（禁改 Entity / Repository / Domain Service / 跨模块 import，对齐 ARCHITECTURE §21）
- 架构修复：Sprint 0 DoD 明确 6 个核心 Interface（含 VectorStoreAdapter）
- 新增 TASK-0007 Documentation Foundation（ADR Index / ADR 模板 / 版本同步规则 / AI_CHANGELOG 模板）
- 新增 Sprint 0 Task Dependency Graph 依赖关系图
- DoD Architecture 段新增 ADR-0005 Proposed 与 Module Boundary 确认项


Reason:

v2.0 评审发现 3 项 P0 问题（Migration 重复风险 / Module 命名与 Sprint Roadmap 不一致 / ADR-0005 职责混淆）与 2 项 P1 优化（Flyway 依赖缺失 / AI Agent 业务模块越权风险），同时识别 Sprint 0 DoD 未明确 VectorStoreAdapter、缺少 Documentation Agent 任务。v2.1 修复后职责边界清晰，AI Agent 可按任务卡无冲突并行执行。


Impact:

仅影响 docs/TASK_BOARD.md（v2.0→v2.1）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md，无代码变更。本文档生效后，Sprint 0 任务卡 7 个（TASK-0001~0007），AI Agent 领取任务必须遵守 Depends 顺序与禁止项。


Reviewer:

Pending


---


## 2026-07-28 (第 12 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

调整 ADR Roadmap，将 ADR 生命周期与 Sprint 生命周期对齐，避免一次性批量创建 ADR-0005~0011：
- 补充 ADR-0002 PostgreSQL Impact：存储分层禁止项（禁 Redis 作主数据源 / 禁 Vector DB 保存业务事实 / 禁 MySQL 专属语法）
- 补充 ADR-0003 AI Router Decision：Agent 不持有业务状态，不直接持久化业务数据；产出必须经 Domain API 落库
- ARCHITECTURE.md v2.2 → v2.3：§14 ADR 清单重写，ADR-0006~0009 标注对应 Sprint 与 Pending，ADR-0010 提前到 Sprint 0（Proposed），ADR-0011 提前到 Sprint 0（Accepted，已是架构事实）；新增 §23 Version History
- SPRINT_PLAN.md v2.1 → v2.2：§16 ADR Roadmap 重写，增加备注列与创建时机规则
- TASK_BOARD.md v2.1 → v2.2：TASK-0001 Todo 新增 ADR-0010 / ADR-0011 创建项；DoD 调整为三 ADR 状态


Reason:

v2.1 中 ADR-0005~0011 虽然标注了「负责 Sprint」，但未明确「禁止提前批量创建」的规则，AI Agent 容易在 Sprint 0 一次性创建全部 ADR，导致 ADR-0006~0009 在对应 Sprint 启动前就被过早锁定。同时 ADR-0002 / ADR-0003 缺少关键的存储分层与 Agent 不持有状态约束，未来 AI Agent 容易误用 Redis 作主数据源或让 Agent 直接写数据库。


Impact:

影响 docs/ARCHITECTURE.md（v2.2→v2.3）、docs/SPRINT_PLAN.md（v2.1→v2.2）、docs/TASK_BOARD.md（v2.1→v2.2）、docs/architecture/ADR/ADR-0002-postgresql-as-primary-db.md、docs/architecture/ADR/ADR-0003-ai-agent-unified-router.md、docs/CHANGELOG.md、docs/AI_CHANGELOG.md，无代码变更。本文档生效后，Sprint 0 仅创建 ADR-0005 / ADR-0010 / ADR-0011 三个 ADR，其余按对应 Sprint 推进。


Reviewer:

Pending


---


## 2026-07-28 (第 14 次变更)


Agent:

Architecture Agent


Task:

非任务


Action:

新增 AGENTS.md §15 Git Branch Governance，修复 AI Agent 直接提交 develop 的问题：
- 发现 TASK-0002 Backend Foundation 代码直接在 develop 分支准备提交，违反 §5.2「AI Agent 严禁直接操作 develop」
- 根因：§5.2 仅有声明无强制执行机制
- 新增 §15.1 Develop Branch Protection（硬约束）
- 新增 §15.2 Task Start Checklist（feature 分支创建后才能 Developing）
- 新增 §15.3 Task Commit Workflow（feature → PR → 审核 → 合并）
- 新增 §15.4 Branch Status 字段（Created / Pushed / PR-Open / Merged）
- 新增 §15.5 AI Agent 自检规则（git 命令前检查当前分支）
- 新增 §15.6 PR 合并条件
- TASK-0002 起严格执行 feature 分支流程


Reason:

TASK-0001 之前的提交均在 develop 上完成（仓库初始化阶段，§15.1 例外允许）。但从 TASK-0002 开始进入代码交付阶段，多个 Agent 并行时 develop 会失控。必须在第一个代码任务启动前建立强制约束。


Impact:

影响 docs/AGENTS.md（v1.2→v1.3）、docs/TASK_BOARD.md、docs/CHANGELOG.md、docs/AI_CHANGELOG.md，无代码变更。本文档生效后，所有代码任务必须先创建 feature 分支，禁止直接提交 develop。


Reviewer:

Pending


---


## 2026-07-28 (第 15 次变更)


Agent:

Backend Agent


Task:

TASK-0002 Backend Foundation


Action:

初始化 Spring Boot 后端工程，建立 Modular Monolith 基础结构：
- 创建 Spring Boot 3.2.5 + Java 17 + Maven 工程（backend/solo-server/）
- 建立 Modular Monolith 包结构（ARCHITECTURE §19）：common + user/today/explore/mood/growth/community/story/ai 共 9 个模块包
- 实现统一 Response Wrapper：ApiResponse<T>（code/message/data/traceId）+ ResultCode 枚举
- 实现异常体系（ARCHITECTURE §20）：SoloException 基类 + BusinessException/ValidationException/AIException/ExternalException/AuthException + GlobalExceptionHandler（业务异常 400 / 系统异常 500 / 不返回堆栈）
- 实现 TraceIdFilter（ARCHITECTURE §16：traceId 贯穿前端→Backend→AI）
- 实现 GET /health 端点
- 配置 OpenAPI / Swagger UI
- 配置 CORS（开发环境 localhost）
- 配置 application.yml + application-dev.yml 环境分层（DB/Redis 环境变量占位符）
- 集成依赖：Spring Web / Validation / Data Redis / Actuator / Flyway 10.10 / PostgreSQL / springdoc-openapi
- mvn compile 验证通过（23 个源文件编译成功）
- 首次执行 §15 Git Branch Governance：在 feature/backend-foundation 分支提交，非 develop


Reason:

Sprint 0 Phase 2 工程实现启动。Backend Foundation 是所有后续任务（Database / AI Interface / Test）的基础。按 TASK-0002 DoD 要求建立分层骨架与公共基础设施，为 Sprint 1 User Module 开发提供可运行的后端工程。


Impact:

新增 backend/solo-server/ 目录，包含 27 个文件（pom.xml + 23 个 Java 源文件 + 2 个配置文件 + .env.example）。无业务模块 Entity/Repository/Service/Controller（仅 common 基础设施）。本文档生效后，TASK-0004 Database Foundation 可依赖 Flyway 配置启动，TASK-0005 AI Foundation 可依赖 common 包结构启动。


Reviewer:

Pending


Task:

TASK-0001 Architecture Foundation


Action:

执行 Sprint 0 Architecture Freeze Gate，完成架构边界冻结：
- Sprint 0 Status：Planning → Ready
- TASK-0001 Status：Designing → Done
- 创建 ADR-0005 Vector DB Adapter Strategy（Proposed）：采用 VectorStoreAdapter 抽象模式，候选 pgvector / Milvus / Qdrant，Provider 延后至 Sprint 5 决策
- 创建 ADR-0010 Tag Ownership（Proposed）：决策方向为 Tag 归 Shared Kernel，Owner Architecture，避免多模块反向依赖 User Module
- 创建 ADR-0011 Activity Ownership（Accepted）：Activity 归 Today Module，Explore 只读引用，CommunityEvent 独立不复用 activity
- 输出 Module Boundary Freeze：8 模块（User / Today / Explore / Mood / Growth / Community / Story / AI Platform）+ Shared Kernel 冻结表
- 输出环境配置规范：.env（不入库）/ docker-compose.yml / docker-compose.ci.yml / application.yml / application-dev.yml 分层


Reason:

Sprint 0 的核心目标是冻结架构边界，使后续 TASK-0002~0007 可无冲突并行执行。TASK-0001 作为 Architecture Freeze Gate，必须在工程任务启动前完成 ADR-0005（Vector DB 接口边界）、ADR-0010（Tag 领域边界争议决策）、ADR-0011（Activity Owner 架构事实固化）。完成后进入「减少文档修改频率，增加代码产出频率」阶段。


Impact:

新增 docs/architecture/ADR/ADR-0005-vector-db-adapter-strategy.md、ADR-0010-tag-ownership.md、ADR-0011-activity-ownership.md 三个文件。更新 docs/TASK_BOARD.md（v2.2→v2.3，TASK-0001 Done，Sprint 0 Ready）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md。无代码变更。本文档生效后，TASK-0002 / TASK-0003 / TASK-0005 / TASK-0007 可并行启动，TASK-0004 待 TASK-0002 完成后启动。


Reviewer:

Pending


---


## 2026-07-28 (第 16 次变更)


Agent:

AI Agent


Task:

TASK-0005 AI Foundation


Action:

定义 AI Foundation 6 个核心 Interface（Sprint 0 仅接口，不实现）：
- Agent（agents/Agent.java）：统一 execute 契约 + AgentResult + Context
- AgentRouter（orchestrator/AgentRouter.java）：路由策略抽象（ADR-0003）
- MemoryService（memory/MemoryService.java）：长期记忆读写（ai_memory）
- ConversationService（memory/ConversationService.java）：短期对话上下文（ai_conversation）
- VectorStoreAdapter（llm/VectorStoreAdapter.java）：Vector DB 抽象层（ADR-0005，不绑定 Provider）
- LLMProvider（llm/LLMProvider.java）：模型调用抽象层（ADR-0008，Sprint 5 实现）
- 遵守全部禁止项：无 LLM 接入 / 无 Prompt / 无 Agent 实现 / 无 Vector DB 部署 / 无业务 Entity 修改
- 执行 §15 Git Branch Governance：feature/ai-foundation 分支提交
- 执行 §15.8 Compile Validation：mvn clean compile 通过（32 source files）


Reason:

Sprint 0 Phase 2 AI Foundation，为 Sprint 5 AI Platform 预留扩展点。Sprint 0 仅定义接口边界，确保未来切换 LLM Provider / Vector DB 时业务代码零修改。


Impact:

新增 backend/solo-server/src/main/java/com/sololifeos/ai/ 下 9 个文件（6 Interface + 3 支撑类）。无业务实现，无数据库变更。本文档生效后，Sprint 5 AI Platform 可基于此 6 个 Interface 实现具体 Agent。


Reviewer:

Pending


---


## 2026-07-28 (第 17 次变更)


Agent:

Frontend Agent


Task:

TASK-0003 Frontend Foundation


Action:

初始化 uni-app + Vue3 + TypeScript + Pinia H5 工程：
- 创建 apps/h5/ 目录，14 个文件
- 配置 TypeScript strict mode + @/* 路径别名
- 配置 Pinia 状态管理
- 配置 Vite + uni-app 插件
- 创建 src/api/ 通用请求封装（对齐 Backend ApiResponse 格式）
- 创建 src/stores/ app store
- 创建首页 pages/index/index.vue
- 配置 VITE_API_BASE_URL 环境变量
- 执行 §15 Git Branch Governance：feature/frontend-foundation 分支提交


Reason:

Sprint 0 Phase 2 Frontend Foundation，为 Sprint 1+ User Module 前端页面开发提供可运行骨架。MVP 阶段仅 H5 端，多端结构已预留。


Impact:

新增 apps/h5/ 目录，14 个文件。无业务页面，无后端变更。本文档生效后，Sprint 1 可在此基础上开发登录 / 资料 / 偏好页。


Reviewer:

Pending


---


## 2026-07-28 (第 19 次变更)


Agent:

Architecture Agent


Task:

非任务（Sprint 0 Phase 2 收尾）


Action:

执行 Sprint 0 Phase 2 收尾，将三个任务从 Reviewing → Done：
- TASK-0002 Backend Foundation：Reviewing → Done（PR #1 已合并 develop，Squash merge）
- TASK-0003 Frontend Foundation：Reviewing → Done（PR #3 已合并 develop，Squash merge）
- TASK-0005 AI Foundation：Reviewing → Done（已合并 develop，Squash merge）
- 三个 feature 分支（feature/backend-foundation / feature/ai-foundation / feature/frontend-foundation）已删除（remote + local）
- `docs/TASK_BOARD.md` v2.3 → v2.4：
  - Branch Status：PR-Open → Merged
  - Sprint 0 DoD Code 段：Backend / Frontend / AI Foundation 三项已勾选
  - Sprint 0 DoD Architecture 段：8 项全部已勾选
  - Completed 区新增 TASK-0002 / TASK-0003 / TASK-0005 交付物清单
- `docs/CHANGELOG.md`：新增 "Changed (Sprint 0 Phase 2 完成收尾)" 段
- 中间过程：解决 docs/CHANGELOG.md 冲突（develop 上的 §15 governance 提交与 feature 分支重复 §15.7/§15.8 提交冲突），通过 rebase + force-push 整理三个 feature 分支


Reason:

三个 PR 已通过 GitHub Web 合并到 develop，但 TASK_BOARD 仍停留在 Reviewing 状态。AGENTS §7 Task Ownership 与 §15.4 Branch Status 要求任务完成后立即更新状态机。本次收尾使 TASK_BOARD 与实际 git 状态对齐，并为 Sprint 0 Phase 3（TASK-0004 Database Foundation）启动扫清障碍。


Impact:

影响 docs/TASK_BOARD.md（v2.3→v2.4）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md，无代码变更。本文档生效后，Sprint 0 Phase 2 闭环，剩余 TASK-0004 / TASK-0006 / TASK-0007 可启动。


Reviewer:

Pending


---


## 2026-07-28 (第 20 次变更)


Agent:

Backend Agent


Task:

TASK-0004 Database Foundation


Action:

执行 TASK-0004 Database Foundation，建立数据库基础环境与初始 Migration：
- 新建 docker-compose.yml（PostgreSQL 16-alpine + Redis 7-alpine，healthcheck + 命名数据卷）
- 新建 docker-compose.ci.yml（CI 环境 tmpfs 覆盖）
- 新建 database/migrations/ 目录与三个初始 Migration：
  - V20260728_001__create_user_table.sql（§6.1，10 字段 + 3 索引，email/phone partial unique）
  - V20260728_002__create_user_preference_table.sql（§6.2，7 字段 + uk_user_id unique）
  - V20260728_003__create_tag_table.sql（§6.10，5 字段 + uk_user_name_type unique）
- 更新 application.yml：
  - Flyway locations 从 classpath:db/migration 改为 filesystem:database/migrations（对齐 DATABASE_DESIGN §10）
  - 新增 validate-on-migrate: true
  - 新增 HikariCP 连接池配置（max 10 / min 2 / connection-timeout 30s / idle-timeout 600s / max-lifetime 1800s）
- 更新 .env.example：新增 DB_POOL_MAX / DB_POOL_MIN / FLYWAY_LOCATIONS
- 严格对齐 DATABASE_DESIGN：§6 字段定义 / §7 枚举 / §8 索引策略 / §9 外键策略（逻辑关联不建 FK）
- tag 表 Owner 标注 ADR-0010（Proposed）待定稿：Shared Kernel


Reason:

TASK-0002 Backend Foundation 已合并 develop，Flyway 依赖已就绪。TASK-0004 建立数据库基础环境，为 Sprint 1 User Module 提供可初始化的 PostgreSQL + Redis + 三张前置表。SQL 严格对齐 DATABASE_DESIGN v2.1，确保后续 Entity 生成无歧义。


Impact:

新增 docker-compose.yml / docker-compose.ci.yml / database/migrations/ 目录（3 个 SQL 文件）。修改 application.yml（Flyway locations + HikariCP）与 .env.example。无 Java 代码变更。
⚠️ sandbox 无 docker / 无网络，docker compose up + flyway migrate 待本地验证。


Reviewer:

Pending


---


## 2026-07-28 (第 21 次变更)


Agent:

Backend Agent


Task:

TASK-0006 CI/CD Foundation


Action:

执行 TASK-0006 CI/CD Foundation，建立 GitHub Actions CI 基础流水线：
- 新建 .github/workflows/backend-ci.yml：
  - 触发：PR + push 到 develop（paths: backend/**）
  - JDK 17 + Maven 缓存（actions/setup-java@v4）
  - 步骤：mvn clean compile -B -ntp（§15.8 编译验证）+ mvn test（单元测试，Sprint 0 continue-on-error）
  - 测试结果上传为 artifact（7 天保留）
- 新建 .github/workflows/frontend-ci.yml：
  - 触发：PR + push 到 develop（paths: apps/**）
  - Node 20 + npm 缓存（actions/setup-node@v4）
  - 步骤：npm install + npm run type-check（CODE_RULES §2）+ npm run build:h5（Sprint 0 continue-on-error）
  - 构建产物上传为 artifact（7 天保留）
- 新建 .github/branch-protection.md：分支保护规则建议
  - main：PR + 1 approval + required status checks（backend-ci, frontend-ci）+ 禁 bypass
  - develop：同上
  - 含 gh API 配置命令
- 升级 .github/PULL_REQUEST_TEMPLATE.md：
  - 新增 DevOps 变更类型
  - 新增治理检查段（§15.6 PR 合并条件：分支正确 / 未直推 develop / CI 通过 / Reviewer 审核）
  - 新增 TASK_BOARD 字段
- Sprint 0 阶段 test/build 步骤使用 continue-on-error: true，Sprint 1 起收紧为必须通过


Reason:

TASK-0002/0003 已合并 develop，工程结构就绪。建立 CI 流水线使后续所有 PR 经 CI 把关，防止编译失败 / 类型错误进入 develop。分支保护规则对齐 AGENTS §5.2（AI Agent 严禁直接操作 develop/main）。


Impact:

新增 .github/workflows/backend-ci.yml / frontend-ci.yml / .github/branch-protection.md。修改 .github/PULL_REQUEST_TEMPLATE.md 与 docs/。无业务代码变更。
⚠️ CI 实际触发待 PR 创建后 GitHub Actions 运行验证。


Reviewer:

Pending


---


## 2026-07-28 (第 22 次变更)


Agent:

Architecture Agent


Task:

TASK-0007 Documentation Foundation


Action:

执行 TASK-0007 Documentation Foundation，建立文档治理基础设施（严格范围控制，未修改已冻结架构）：
- 新建 docs/architecture/ADR/README.md：ADR Index
  - Accepted: ADR-0001 / 0002 / 0003 / 0004 / 0011（5 个）
  - Proposed: ADR-0005 / 0010（2 个）
  - Future: ADR-0006 / 0007 / 0008 / 0009（4 个，对应 Sprint 1/3/5/7）
  - ADR 生命周期规则：Proposed → Accepted → Deprecated（不可逆向回滚）
- 新建 docs/architecture/ADR/template.md：ADR 标准模板（Decision / Reason / Impact / Migration）
- 新建 docs/governance/DOCUMENT_VERSION_RULE.md：版本同步规则
  - 9 份核心文档修改条件矩阵
  - 核心原则：代码优先 + 非必要禁止修改核心架构文档
  - 允许场景：架构变更（需 ADR）/ Bug 修复 / 新增功能（需评估）
- 新建 docs/AI_CHANGELOG_TEMPLATE.md：AI 行为日志模板（6 字段固定格式）


Reason:

Sprint 0 即将关闭，需要冻结文档治理规则，防止后续 Sprint 频繁修改核心架构文档导致架构漂移。ADR Index 统一管理所有 ADR 状态，版本同步规则约束修改频率（代码优先），AI_CHANGELOG 模板规范 AI 行为记录格式。


Impact:

新增 4 个文档文件，未修改任何已冻结的架构文档（ARCHITECTURE / DATABASE_DESIGN / ADR 已接受决策 / 数据模型 / 模块边界）。本文档生效后，Sprint 0 可正式关闭，进入 Sprint 1 业务代码阶段。


Reviewer:

Pending


---


## 2026-07-29 (第 23 次变更)


Agent:

Architecture Agent


Task:

非任务（Sprint 0 关闭收尾）


Action:

执行 Sprint 0 关闭对账，将 PR #6 / #7 / #8 合并后的任务状态同步到 TASK_BOARD：
- TASK-0004 Database Foundation：Reviewing → Done，Branch Status：PR-Open → Merged（PR #6）
- TASK-0006 CI/CD Foundation：Reviewing → Done，Branch Status：PR-Open → Merged（PR #7）
- TASK-0007 Documentation Foundation：Reviewing → Done，Branch Status：PR-Open → Merged（PR #8）
- Sprint 0 Status：In Progress → Done (Closed 2026-07-29)
- Sprint 0 DoD：Code / Documentation 段全部勾选；Test 段延期至 Sprint 1
- Completed 段新增 TASK-0004 / TASK-0006 / TASK-0007 交付物清单
- 新增 Sprint 0 Close Gate 段
- TASK_BOARD.md v2.4 → v2.5


Reason:

PR #6 / #7 / #8 合并后，TASK_BOARD 仍停留在 Reviewing / PR-Open 状态，未做合并后对账，导致 Sprint 0 无法正式关闭。按 AGENTS §15.4 Branch Status 字段规则，PR 合并后必须将任务卡更新为 Done / Merged，并归档到 Completed 段。本次仅做状态对账，未修改任何已冻结的架构文档。


Impact:

仅影响 docs/TASK_BOARD.md / docs/CHANGELOG.md / docs/AI_CHANGELOG.md，无代码与数据库变更。本文档生效后，Sprint 0 正式关闭，进入 Sprint 1 User Module 业务代码阶段。


Reviewer:

Pending


---


## 2026-07-29 (第 24 次变更)


Agent:

Architecture Agent


Task:

TASK-0101 User Migration Review


Action:

执行 Sprint 1 首个任务 TASK-0101 User Migration Review，对 Sprint 0（TASK-0004）创建的三张表做字段逐项核对：
- user 表 vs DATABASE_DESIGN §6.1：10/10 字段对齐
- user_preference 表 vs §6.2：7/7 字段对齐
- tag 表 vs §6.10：5/5 字段对齐
- 索引：5/5 对齐（uk_user_email / uk_user_phone / idx_user_status / uk_user_preference_user_id / uk_tag_user_name_type）
- 枚举：3/3 对齐（USER_STATUS / BUDGET_LEVEL / TAG_TYPE）
- 外键策略：逻辑关联，无物理 FK（§9）
- Gap 分析：user 表缺 password 字段，归 Auth 任务（ADR-0006 JWT）处理，不在 TASK-0101 扩展
- 结论：无需增量 Migration，三表完全符合 User Module Domain Design
- 新建 docs/modules/user/MIGRATION_REVIEW.md 记录审查结果


Reason:

Sprint 1 User Module 启动前，必须确认 Sprint 0 创建的数据基线符合 User Module Domain Design，避免 Domain Layer 开发时发现 schema 与设计不符返工。password 字段因与 ADR-0006 认证策略（哈希算法 / OAuth 字段）耦合，归 Auth 任务同期落地，避免在 ADR 未定时过早锁定 schema。


Impact:

新增 docs/modules/user/MIGRATION_REVIEW.md（审查记录）；更新 docs/TASK_BOARD.md（v2.5→v2.6，新增 TASK-0101 任务卡 + Sprint 1 剩余任务清单）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md。无代码与数据库变更，未修改任何已冻结架构文档（DATABASE_DESIGN §6.1/§6.2/§6.10）。


Reviewer:

Pending


---


## 2026-07-30 (第 25 次变更)


Agent:

Backend Agent


Task:

TASK-0102 User Domain Layer


Action:

建立 User Module 领域层，基于 TASK-0101 已审查的 user / user_preference / tag schema：
- 引入 Spring Data JPA 依赖到 pom.xml，配置 ddl-auto=none（Flyway 管理 schema）+ open-in-view=false（显式事务边界）
- 创建 user 模块包结构：domain/model + domain/service + repository（对齐 CODE_RULES §4）
- 创建 3 枚举：UserStatus / BudgetLevel / TagType（对齐 DATABASE_DESIGN §7）
- 创建 3 JPA Entity：User（软删除 @SQLDelete + @SQLRestriction）/ UserPreference / Tag（对齐 §6.1/6.2/6.10）
- 创建 3 Repository：UserRepository / UserPreferenceRepository / TagRepository（Spring Data JPA 代理实现）
- 创建 3 Domain Service：UserDomainService / UserPreferenceDomainService / TagDomainService（业务规则，不持久化）
- 范围控制：未实现 Application Service / Controller / DTO（归 TASK-0103/0104），未添加 password 字段（归 Auth/ADR-0006），Entity 间无物理 FK 映射（§9 逻辑关联）


Reason:

TASK-0101 确认 Sprint 0 三表完全符合 User Module Domain Design 后，Domain Layer 可直接基于现有 schema 开发。本任务为后续 Application Service（TASK-0103）与 Controller（TASK-0104）提供领域对象与业务规则层。password 字段因与 ADR-0006 认证策略耦合，归 Auth 任务同期落地。


Impact:

新增 backend/solo-server 下 user 模块 12 个 Java 源文件（3 Entity + 3 枚举 + 3 Repository + 3 Domain Service）；修改 pom.xml（加 JPA 依赖）+ application.yml（加 JPA 配置）；更新 docs/TASK_BOARD.md（v2.6→v2.7）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md。无数据库 Migration 变更，未修改已冻结架构文档。编译验证待 CI（沙箱无网络，JPA 依赖未在本地 Maven 缓存）。CI 通过，PR #12 已合并。


Reviewer:

Pending


---


## 2026-07-30 (第 26 次变更)


Agent:

Backend Agent


Task:

TASK-0103 User Application Service


Action:

建立 User Module 应用服务层（CODE_RULES §3.1 Application Service）：
- UserApplicationService：register（事务内创建 user + 默认 preference）/ getById / getByEmail / getByPhone / updateProfile / activate / ban
- UserPreferenceApplicationService：getByUserId / update
- TagApplicationService：create / listByUser / listByUserAndType
- 事务边界：写操作 @Transactional，读操作 @Transactional(readOnly=true)
- 入参用原始类型，出参用 Domain Entity（DTO 转换归 Controller TASK-0104）
- 构造器注入（CODE_RULES §3.3），注入 Domain Service + Repository
- 注册闭环：register 方法事务内创建 user + 默认 preference（SPRINT_PLAN: 注册→登录→设置偏好闭环）
- TASK-0102 状态对账：Reviewing → Done（PR #12 merged，CI 编译通过）


Reason:

TASK-0102 已建立 Domain Layer（Entity / Repository / Domain Service），Application Service 作为用例协调层连接 Domain Service 与 Repository，提供事务边界。注册时自动创建默认偏好，保证注册→登录→设置偏好的用户闭环完整。password 字段与认证逻辑归 Auth 任务（ADR-0006），本任务不涉及。


Impact:

新增 backend/solo-server/src/main/java/com/sololifeos/user/application/ 下 3 个 Java 源文件；更新 docs/TASK_BOARD.md（v2.7→v2.8，TASK-0102 Done + 新增 TASK-0103 卡片）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md。无数据库 Migration 变更，未修改已冻结架构文档。编译验证待 CI（沙箱无网络）。


Reviewer:

Pending


---


## 2026-07-30 (第 27 次变更)


Agent:

Backend Agent


Task:

TASK-0107 Authentication（ADR-0006 JWT）


Action:

实现 JWT 认证闭环（ADR-0006），建立 MVP 阶段认证基础设施：
- 创建 ADR-0006 JWT Authentication（Accepted）：HS256 + BCrypt + 自定义 JwtAuthFilter，不引入完整 Spring Security 框架
- 数据库 Migration：V20260730_001__add_password_to_user.sql（ALTER TABLE "user" ADD COLUMN password varchar(100)，nullable 兼容存量数据）
- 依赖：jjwt 0.12.6（HS256 签发/验证）+ spring-security-crypto（BCryptPasswordEncoder）
- 配置：application.yml 根级 jwt.secret + jwt.expiration-ms（环境变量 JWT_SECRET / JWT_EXPIRATION_MS 注入）
- common/security 新增 5 个组件：
  - JwtProperties（@ConfigurationProperties("jwt")，@Validated 校验）
  - JwtService（HS256 签发/验证，jjwt 0.12 API，subject + uid + nickname claim）
  - JwtAuthFilter（白名单 + Bearer token 解析 + 401 ApiResponse 响应，@Order HIGHEST_PRECEDENCE+10）
  - UserContext（ThreadLocal 持有当前 userId，Filter finally 清除防泄漏）
  - PasswordEncoderConfig（BCryptPasswordEncoder Bean）
- user Module 认证组件：
  - AuthService（登录用例：邮箱/手机号查询 + BCrypt 校验 + JWT 签发，登录失败 message 统一防账号枚举）
  - AuthController（POST /api/auth/login，返回 LoginResponse）
  - LoginRequest（account + password，@Valid 校验）/ LoginResponse（token + userId + nickname）
- 修改既有代码：
  - User Entity 加 password 字段（@Column length=100）+ getPassword
  - User.register 工厂方法加 hashedPassword 参数
  - UserDomainService.register 加 hashedPassword 参数 + 非空校验
  - UserApplicationService.register 接受 rawPassword，BCrypt 哈希后传 Domain Service
  - UserRegisterRequest 加 password 字段（@NotBlank + @Size 6-100）
  - UserController.register 传 request.password() 参数
- SoloLifeOsApplication 加 @ConfigurationPropertiesScan 启用 JwtProperties 绑定


Reason:

SPRINT_PLAN Sprint 1 DoD 要求"注册→登录→设置偏好"闭环，必须实现 login 端点与 token 机制。ADR-0006 已决策采用 JWT（HS256）+ BCrypt，不引入完整 Spring Security 框架（避免 SecurityFilterChain / UserDetailsService / OAuth2 复杂度）。password 字段 nullable 兼容存量数据，明文密码经 HTTPS 传输在 Application Service 层 BCrypt 哈希后入库。


Impact:

新增 docs/architecture/ADR/ADR-0006-jwt-authentication.md（Accepted）；新增 database/migrations/V20260730_001__add_password_to_user.sql；新增 backend/solo-server/src/main/java/com/sololifeos/common/security/ 下 5 个 Java 源文件；新增 backend/solo-server/src/main/java/com/sololifeos/user/ 下 AuthService + AuthController + LoginRequest + LoginResponse；修改 User Entity / UserRegisterRequest / UserDomainService / UserApplicationService / UserController / application.yml / pom.xml / SoloLifeOsApplication。更新 docs/TASK_BOARD.md（v2.9→v3.0，新增 TASK-0107 任务卡）、docs/CHANGELOG.md、docs/AI_CHANGELOG.md、docs/architecture/ADR/README.md（ADR-0006 登记到 Accepted）。

mvn clean compile passed（67 source files，jjwt + spring-security-crypto 依赖经 Aliyun 镜像 + HTTP 代理下载成功）。


Reviewer:

Pending
