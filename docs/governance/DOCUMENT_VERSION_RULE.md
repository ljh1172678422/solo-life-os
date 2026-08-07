# Document Version Synchronization Rule

Version: 1.4

Last Update: 2026-08-07

> 本规则约束核心文档的修改条件，防止文档频繁变更导致架构漂移。
> 对齐 AGENTS §0 文档权威层级 / §13 文档版本管理 / §8 Architecture Change Process。

---

## 1. 修改条件

| 文件 | 修改条件 | 修改人 |
|------|----------|--------|
| `Solo_Product_Principles.md` | 产品宪法级变更（产品定位 / 边界 / 北极星 / AI 角色原则等） | **人工产品负责人**（AI/Agent 可提出建议，但不得自行变更） |
| `PROJECT_CONTEXT.md` | 产品上下文调整（受 Solo_Product_Principles 约束） | Architecture Agent |
| `README.md` | Sprint 完成 / Major milestone / Release 发布 / 架构阶段切换（见 §6） | Architecture Agent |
| `ARCHITECTURE.md` | 架构边界变化、新增模块、新增 ADR | Architecture Agent |
| `DATABASE_DESIGN.md` | Schema 变化、新增表、新增字段、枚举变化 | Architecture Agent |
| `AGENTS.md` | AI 行为规则变化、权限分级变化、Git 工作流变化 | Architecture Agent |
| `TASK_BOARD.md` | Task 状态变化、Owner 变化 | 任何 Agent（仅自己 Owner 的任务） |
| `SPRINT_PLAN.md` | Sprint 范围变化、Sprint 顺序调整 | Architecture Agent |
| `CODE_RULES.md` | 编码规范变化、新增语言规则 | Architecture Agent |
| `CHANGELOG.md` | 每次 PR 合并 | PR 提交者 |
| `AI_CHANGELOG.md` | AI 架构决策 / 治理规则修改 / 非预期技术方案 / 重要依赖引入（见 §8） | AI Agent |
| `ADR/*` | 新建 ADR / ADR 状态变更 | Architecture Agent |

---

## 2. 核心原则

### 2.1 文档与代码保持同步

```
文档与代码同等对待，保持同步演进
```

核心架构文档（ARCHITECTURE / DATABASE_DESIGN / AGENTS）与代码同等重要，均走 feature 分支 + PR 流程。修改必须有明确业务或技术驱动，禁止为「完善文档」而修改；但涉及产品边界、领域模型或架构决策时，**必须先形成 Accepted ADR，再实施代码**，禁止"先写代码后补文档"。

文档与代码同步约束：

- 涉及产品宪法（Solo_Product_Principles）→ 人工产品负责人审核
- 涉及产品边界 / 领域模型 / 架构决策 → 上游（Product Principles / Project Context）决策确认后，更新下游 ARCHITECTURE / DATABASE_DESIGN 及实施代码前须先形成 Accepted ADR（注：重写 Project Context 本身不要求 ADR 前置，因为 Project Context 是 ADR 的上游）
- 涉及 Schema / 枚举 / 模块边界 → ARCHITECTURE / DATABASE_DESIGN 与 migration 同 PR 或紧邻 PR
- 普通功能 / Bug 修复 → 代码先，CHANGELOG 同步

### 2.2 非必要禁止修改

以下情况禁止修改核心架构文档：

- 没有经过 Architecture Agent 审核的架构调整
- 没有对应 ADR 的产品边界 / 领域模型变更
- Sprint 进行中的临时性调整（应等到 Sprint 回顾时统一处理）
- 个人偏好性修改（措辞、格式、排版）

### 2.3 修改必须记录

任何核心文档修改必须：

1. 在 `CHANGELOG.md` 记录修改内容
2. 在 `AI_CHANGELOG.md` 记录修改原因（AI Agent 行为，详见 §8）
3. 更新文件头部的 `Version` 和 `Last Update`
4. 在 PR 描述中说明修改原因

### 2.4 状态唯一来源原则（Single Source Of Truth）

项目状态有且仅有一个权威来源，其他文档只做引用，不做覆盖。同时，核心文档之间存在严格权威层级（详见 AGENTS §0），下游文档不得与上游文档冲突，冲突时以上游为准。

**文档权威层级（自上而下）：**

```
Solo_Product_Principles.md   ← 产品宪法（Owner: 人工产品负责人）
        ↓
PROJECT_CONTEXT.md           ← 项目上下文
        ↓
Accepted ADR（docs/architecture/ADR/*）
        ↓
ARCHITECTURE.md / DATABASE_DESIGN.md
        ↓
CODE_RULES.md
        ↓
SPRINT_PLAN.md → TASK_BOARD.md
```

**状态来源映射：**

| 状态类型 | 唯一来源 | 派生引用 |
|----------|----------|----------|
| 产品宪法（定位 / 边界 / 北极星 / AI 角色原则） | `Solo_Product_Principles.md` | PROJECT_CONTEXT 引用 |
| Task 生命周期（Status / Branch / PR / Validation） | `TASK_BOARD.md` | README "In Progress" 摘要 |
| 提交历史（每次合并做了什么） | `CHANGELOG.md` | 无 |
| 项目当前阶段快照（Current Sprint / Completed / Next） | `README.md`（数据源自 TASK_BOARD） | 无 |
| 架构决策 | `Accepted ADR`（docs/architecture/ADR/*） | 无 |
| 当前架构投影 | `ARCHITECTURE.md` | 无（投影自 Accepted ADR） |
| AI 行为决策 | `AI_CHANGELOG.md` | 无 |

禁止：

- [X] 下游文档与上游文档冲突（如 TASK_BOARD 规避 Solo_Product_Principles 的边界约束）
- [X] README 状态覆盖 TASK_BOARD（如 README 写 "TASK-0200 Completed" 但 TASK_BOARD 是 "Reviewing"）
- [X] 用 CHANGELOG 作为 Task 状态来源（CHANGELOG 是历史，不是当前状态）
- [X] 多文档维护同一份状态数据，导致冲突时无法裁决
- [X] 在 PR / Commit message / Issue 中维护脱离 TASK_BOARD 的私有任务状态
- [X] AI/Agent 自行变更 Solo_Product_Principles.md（必须人工产品负责人审核）

### 2.5 Domain Ownership Matrix

§1 定义「谁修改什么文件」，本节定义「谁负责什么领域」。非 Owner 修改他人领域必须显式声明，防止跨边界污染。

| Domain | Owner | 主要文件范围 |
|--------|-------|------------|
| Architecture | Architecture Agent | `docs/ARCHITECTURE.md` / `docs/architecture/ADR/*` / `docs/SPRINT_PLAN.md` |
| Backend | Backend Agent | `backend/solo-server/src/main/java/**`（业务模块） / `backend/solo-server/pom.xml` |
| Frontend | Frontend Agent | `apps/h5/src/**` / `apps/h5/package.json` |
| Database | Backend + Architecture（协作） | `database/migrations/*`（Backend 起草，Architecture 审 Schema） |
| CI/CD | DevOps Agent | `.github/workflows/*` / `.github/branch-protection.md` |
| AI Capability | AI Agent | `backend/solo-server/src/main/java/com/sololifeos/ai/**` |
| Governance | Architecture Agent | `docs/governance/*` / `docs/AGENTS.md` / `docs/CODE_RULES.md` |
| Documentation | Architecture Agent | `README.md` / `docs/CHANGELOG.md` / `docs/AI_CHANGELOG.md` / `docs/TASK_BOARD.md`（状态字段） |

非 Owner 修改他人领域的规则：

1. PR 描述必须说明跨领域原因（为什么需要动他人领域）
2. `AI_CHANGELOG.md` 必须记录（按 §8.1「跨越 Owner 边界操作」）
3. Reviewer 必须 Approve（默认 Reviewer 即该领域 Owner）
4. 紧急修复也必须遵守，事后补流程

禁止：

- [X] Backend Agent 直接修改 `apps/h5/src/**`（应通过 Frontend Agent 或 PR 协作）
- [X] 任何 Agent 直接修改 `docs/ARCHITECTURE.md` 核心边界（必须走 ADR，§3.1）
- [X] Frontend Agent 修改 `database/migrations/*`（Schema 变更归 Architecture）

---

## 3. 允许的修改场景

### 3.1 架构变更（需 ADR）

```
新需求
  ↓
识别架构影响
  ↓
创建 ADR（Proposed）
  ↓
Architecture Agent 审核
  ↓
ADR Accepted
  ↓
更新 ARCHITECTURE / DATABASE_DESIGN
  ↓
进入开发
```

### 3.2 Bug 修复（无需 ADR）

代码 Bug 修复不需要修改架构文档，除非 Bug 根因是架构设计问题。

### 3.3 新增功能（需评估）

新增功能时评估是否影响架构：

- 不影响架构 → 直接开发，仅更新 CHANGELOG
- 影响架构 → 走 §3.1 架构变更流程

---

## 4. 文档版本号规则

- 大版本（v1 → v2）：架构边界变化、Sprint 范围变化
- 小版本（v2.1 → v2.2）：字段补充、规则细化、错误修正
- 修订号（v2.2.1）：仅排版、错别字修正（不记录在 CHANGELOG）

---

## 5. 禁止事项

- [X] 无代码变更的频繁文档迭代
- [X] 跨越 Architecture Agent 直接修改 ARCHITECTURE / DATABASE_DESIGN
- [X] 在 Sprint 进行中修改 Sprint 范围（应等到 Sprint 回顾）
- [X] 修改文档不记录版本号
- [X] 修改文档不更新 CHANGELOG / AI_CHANGELOG


---

## 6. README Status Snapshot

`README.md` 是项目当前状态的对外入口（GitHub 仓库首页），不记录开发过程，只记录当前状态快照。

### 6.1 必须更新

- Sprint 完成
- Major milestone 完成
- Release version 发布
- 架构阶段切换（如 Foundation → Domain Development）
- Public API 变化

### 6.2 无需更新

- 单个 Task 完成（归 TASK_BOARD）
- Bug 修复（归 CHANGELOG）
- 普通 Feature PR（归 CHANGELOG）
- Refactor（归 CHANGELOG）

### 6.3 固定区域

README 至少包含以下区域：

```
# Solo Life OS

## Project Status      （Current Phase / Current Sprint / Status）
## Completed           （已完成能力，按 Sprint 分组）
## In Progress         （当前进行中的任务）
## Tech Stack          （Backend / Frontend / Database / DevOps）
## Repository Structure（顶层目录说明）
## Development Workflow（Feature Branch → PR → CI → Merge Develop）
```

### 6.4 数据来源（Source vs Projection）

README 本身不是状态 Source，只是 TASK_BOARD 的 Projection。两者是单向数据流，非双向同步。

```
TASK_BOARD.md (Project Snapshot 段)
   │  Current Sprint / Current Task / Last Milestone
   │
   ▼  单向投影
README.md (Project Status 区域)
```

- **Source**：`TASK_BOARD.md` Project Snapshot 段
- **Projection**：`README.md` Project Status 区域

规则：

- README 不允许独立维护状态字段，必须从 TASK_BOARD 投影
- TASK_BOARD 更新后，README 在下次 §6.1 触发时同步投影（不强制实时同步）
- 若发现 README 与 TASK_BOARD 状态冲突，以 TASK_BOARD 为准（§2.4）

### 6.5 禁止

- [X] 每个 commit 修改 README（会产生 merge conflict + 污染 PR diff）
- [X] 在 README 记录详细变更历史（归 CHANGELOG）
- [X] 在 README 记录 Task 生命周期细节（归 TASK_BOARD）
- [X] 把 README 当作第二个 TASK_BOARD


---

## 7. PR Lifecycle Synchronization

防止「feature 分支开发完毕才一次性补 TASK_BOARD」导致状态滞后。每个 PR 阶段都有明确的文档同步动作，由 PR 提交者在对应阶段实时执行。

### 7.1 PR 生命周期文档同步矩阵

| 阶段 | 触发动作 | 同步文件 | 字段 |
|------|----------|----------|------|
| Feature 分支创建 | `git checkout -b feature/*` | `TASK_BOARD.md` | Status: Developing，Branch |
| PR 创建 | `gh pr create` | `TASK_BOARD.md` | Status: Reviewing，Branch Status: PR-Open，记录 PR # |
| CI 通过 | GitHub Actions 绿 | `TASK_BOARD.md` | Validation 段补 ✅ CI 通过记录 |
| Changes Requested | Reviewer 请求修改 | `TASK_BOARD.md` | Status: Changes Requested |
| Developer 修复中 | 本地改代码 | `TASK_BOARD.md` | Status: Developing |
| Re-push | `git push`（CI 重跑） | `TASK_BOARD.md` | Status: Reviewing |
| PR Merge | `gh pr merge --squash` | `CHANGELOG.md` + `TASK_BOARD.md` | CHANGELOG 加条目；TASK_BOARD Status → Done，Branch Status → Merged，DoD 勾选 CI 验证 + 合并 |
| Sprint 关闭 | Sprint 全部 Task Done | `README.md` + `TASK_BOARD.md` | README 状态快照刷新（§6）；TASK_BOARD 加 Close Gate 段 |

### 7.2 Task 状态机

PR Review 阶段多次往返时，TASK_BOARD 必须随状态机流转，不能永远停在 Reviewing：

```
Backlog → Assigned → Developing → Reviewing
                                    │
                                    ├─ CI 失败 ────────→ Developing（修复）
                                    │
                                    └─ Changes Requested → Developing（修复）→ Reviewing（re-push）
                                    │
                                    └─ Approved → Done（merge）
```

关键状态：

- **Changes Requested**：Reviewer 已明确请求修改，TASK_BOARD 必须从 Reviewing 切到 Changes Requested
- **Developing（修复中）**：开发者开始改代码，从 Changes Requested 切回 Developing
- **Reviewing（re-push）**：修复后 re-push，CI 重跑，切回 Reviewing

### 7.3 禁止

- [X] Feature 开发完成后才补 TASK_BOARD 状态（应在分支创建时即 Developing）
- [X] PR Review 往返时 TASK_BOARD 永远停在 Reviewing（必须走 Changes Requested → Developing → Reviewing）
- [X] PR 合并后跳过 CHANGELOG 条目
- [X] Sprint 关闭后 README 仍停留在旧 Sprint 状态（违反 §6.1）
- [X] 用 Commit message / PR body 维护脱离 TASK_BOARD 的任务状态（违反 §2.4）


---

## 8. AI_CHANGELOG 边界

`AI_CHANGELOG.md` 记录 AI Agent 做出的**有治理影响的决策**，不是 AI 的操作日志。防止退化为「AI 创建了文件 X / AI 编辑了文件 Y」的流水账。

### 8.1 必须记录

- AI 做出的架构决策（如选用某框架、某模式、某 Provider）
- AI 修改治理规则（AGENTS / CODE_RULES / 本规则 / SPRINT_PLAN）
- AI 采取非预期技术方案（偏离 ARCHITECTURE 或 ADR 默认路径）
- AI 引入重要依赖（pom.xml / package.json 新增非传递依赖）
- AI 跨越 Owner 边界操作（如 Backend Agent 修改 Frontend 文件，需说明原因）
- AI 创建 / 修改 ADR
- AI 主动暂停或回滚任务

### 8.2 无需记录

- 普通代码生成（按 ADR 和 ARCHITECTURE 既定路径实现）
- 文件创建 / 编辑 / 删除（这些归 Git log）
- Bug fix（归 CHANGELOG）
- 格式调整 / 排版修正 / 错别字
- 测试代码编写（除非引入新测试框架）
- 依赖版本小版本升级

### 8.3 格式

每条 AI_CHANGELOG 条目至少包含：

```
## YYYY-MM-DD

### <决策标题>

- Agent: <Backend / Frontend / AI / Architecture / QA>
- Task: TASK-XXXX
- 决策: <一句话描述>
- 原因: <为什么偏离默认或为什么需要记录>
- 影响: <涉及哪些文件 / 模块 / 依赖>
```

### 8.4 禁止

- [X] 把 AI_CHANGELOG 当成 AI 操作日志（"AI 创建了文件 X"）
- [X] 记录普通代码生成（无治理价值的执行细节）
- [X] 用 AI_CHANGELOG 代替 CHANGELOG（CHANGELOG 记录"做了什么"，AI_CHANGELOG 记录"为什么这样决策"）
- [X] 一条 AI_CHANGELOG 跨多个不相关决策（应拆分）

### 8.5 Decision Level（决策分级）

AI 决策数量随 Sprint 增长会快速上升，按影响范围分级，避免「什么都记」或「重大决策漏记」。

| Level | 范围 | 记录位置 | 示例 |
|-------|------|----------|------|
| **L0** Routine | 按 ADR / ARCHITECTURE 既定路径的常规实现 | 无需记录 | 新增一个 Controller、一个 Vue 组件 |
| **L1 Tech Choice** | 框架/库/模式选择，不改变架构边界 | `AI_CHANGELOG.md` | 选 Redis Stream 替代 RabbitMQ 做某用例 |
| **L2 Architecture Impact** | 影响模块边界 / 数据流 / 跨模块契约 | 新建 `ADR` + `AI_CHANGELOG.md` | 引入 Event Driven，跨模块异步 |
| **L3 Critical** | 修改核心系统边界 / 颠覆性架构变化 | Architecture Review + ADR + AI_CHANGELOG | 重划模块边界、替换主 DB |

规则：

- AI 必须先评估决策的 Level，再决定记录方式
- L0 不记录；L1 记 AI_CHANGELOG；L2 必须 ADR（按 §3.1 流程）；L3 必须 Architecture Review
- 跨级误判（如把 L2 当 L1 处理）应在 PR Review 阶段由 Architecture Agent 纠正


---

## 10. Document Validation Checklist

PR Merge 前 PR 提交者必须逐项确认（v1.3 提前落地，原 §9.2 Roadmap 项）。先以人工 checklist 形式执行，成熟后迁入 GitHub Actions 自动化（届时升 v2.0）。

### 10.1 通用检查（每个 PR）

- [ ] 触及的文件属于提交者 Owner 领域（§2.5）；若跨领域，PR 描述已说明原因 + AI_CHANGELOG 已记录
- [ ] 改动 `docs/` 下任何治理 / 架构文档 → Version 字段已更新，CHANGELOG 已记录
- [ ] 不修改已冻结文档（ARCHITECTURE / DATABASE_DESIGN / ADR Accepted）除非有 ADR 支撑
- [ ] 文档修改走 feature 分支 + PR（§11），禁止直推 develop / main

### 10.2 PR Merge 时检查

- [ ] `TASK_BOARD.md` 对应 Task Status → Done，Branch Status → Merged，DoD 已勾选 CI 验证 + 合并（§7.1）
- [ ] `CHANGELOG.md` 有本次合并条目（Squash 粒度，不逐 feature commit 记录）
- [ ] L1+ AI 决策 → `AI_CHANGELOG.md` 有对应条目（§8.5）
- [ ] L2+ 架构影响 → ADR 已创建或更新

### 10.3 Sprint 关闭时检查

- [ ] `README.md` Project Status 已按 §6.1 刷新到当前 Sprint 状态
- [ ] `TASK_BOARD.md` 有 Close Gate 段
- [ ] README 与 TASK_BOARD 状态一致（§6.4 Projection 一致性）

---

## 11. Git Governance Integration

本规则不重复 Git 工作流定义，依赖 `docs/AGENTS.md` §0 文档权威层级与 §15 Git Branch Governance。文档修改与代码修改遵循同一 Git 流程，**无任何例外**。

```
feature/<domain>-<task> 分支
        │
        ▼
开发 + 文档同步（§7 PR Lifecycle）
        │
        ▼
PR + CI Validation
        │
        ▼
Reviewer Approve（默认为该领域 Owner，§2.5；产品宪法变更须人工产品负责人审核）
        │
        ▼
Squash merge to develop
```

核心约束（引自 AGENTS §0 + §15）：

- [X] 禁止直推 `develop` / `main`，无任何例外（包括纯文档修改）
- [X] 所有文档修改必须走 feature 分支 + PR
- [X] `Solo_Product_Principles.md` 修改须人工产品负责人审核，AI/Agent 不得自行变更
- [X] 上游决策（Product Principles / Project Context）确认后，更新下游 ARCHITECTURE / DATABASE_DESIGN 及实施代码前，须先形成 Accepted ADR（注：重写 Project Context 本身不要求 ADR 前置，因为 Project Context 是 ADR 的上游）
- [X] Commit message 遵循 Conventional Commits（`docs(scope): ...`）
- [X] PR 描述说明文档修改原因（治理规则变更需引用对应 §节）

> 本节仅引用 AGENTS §0 + §15，不复制其完整内容。Git 工作流细则以 AGENTS §15 为唯一来源；权威层级以 AGENTS §0 为唯一来源。


---

## 12. Roadmap（v2.0+，待规模需要再落地）

v1.4 为人工产品负责人批准的治理基线调整（确立文档权威层级、Product Principles 进入治理链、删除直推例外），非零散调整。本次调整完成后，本规则进入 v2.0 冻结期：后续仅通过 ADR 修改，禁止零散调整。以下规则已识别但暂缓实现，留待规模需要时再评估。

### 12.1 Documentation Freeze Period（待 §2.6）

Sprint 开始后核心架构文档（ARCHITECTURE / DATABASE_DESIGN / SPRINT_PLAN）冻结，Sprint 内仅可在 ADR Accepted 或 Critical Architecture Bug 时修改。

### 12.2 Document Validation Automation（GitHub Actions）

§10 checklist 成熟后迁入 `.github/workflows/` 自动化检查（README Snapshot 合规 / TASK_BOARD 状态校验 / Version 字段校验）。
