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
