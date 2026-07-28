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
