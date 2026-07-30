# Document Version Synchronization Rule

Version: 1.2

Last Update: 2026-07-30

> 本规则约束核心文档的修改条件，防止文档频繁变更导致架构漂移。
> 对齐 AGENTS §13 文档版本管理 / §8 Architecture Change Process。

---

## 1. 修改条件

| 文件 | 修改条件 | 修改人 |
|------|----------|--------|
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

### 2.1 代码优先

```
代码产出 > 文档完善
```

核心架构文档（ARCHITECTURE / DATABASE_DESIGN / AGENTS）在 Sprint 边界冻结后，
非必要不修改。修改必须有明确业务或技术驱动，禁止为「完善文档」而修改。

### 2.2 非必要禁止修改

以下情况禁止修改核心架构文档：

- 没有对应代码变更的「文档完善」
- 没有经过 Architecture Agent 审核的架构调整
- Sprint 进行中的临时性调整（应等到 Sprint 回顾时统一处理）
- 个人偏好性修改（措辞、格式、排版）

### 2.3 修改必须记录

任何核心文档修改必须：

1. 在 `CHANGELOG.md` 记录修改内容
2. 在 `AI_CHANGELOG.md` 记录修改原因（AI Agent 行为，详见 §8）
3. 更新文件头部的 `Version` 和 `Last Update`
4. 在 PR 描述中说明修改原因

### 2.4 状态唯一来源原则（Single Source Of Truth）

项目状态有且仅有一个权威来源，其他文档只做引用，不做覆盖。

| 状态类型 | 唯一来源 | 派生引用 |
|----------|----------|----------|
| Task 生命周期（Status / Branch / PR / Validation） | `TASK_BOARD.md` | README "In Progress" 摘要 |
| 提交历史（每次合并做了什么） | `CHANGELOG.md` | 无 |
| 项目当前阶段快照（Current Sprint / Completed / Next） | `README.md`（数据源自 TASK_BOARD） | 无 |
| 架构决策 | `ADR/*` + `ARCHITECTURE.md` | 无 |
| AI 行为决策 | `AI_CHANGELOG.md` | 无 |

禁止：

- [X] README 状态覆盖 TASK_BOARD（如 README 写 "TASK-0200 Completed" 但 TASK_BOARD 是 "Reviewing"）
- [X] 用 CHANGELOG 作为 Task 状态来源（CHANGELOG 是历史，不是当前状态）
- [X] 多文档维护同一份状态数据，导致冲突时无法裁决
- [X] 在 PR / Commit message / Issue 中维护脱离 TASK_BOARD 的私有任务状态

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

### 6.4 数据来源

README 的状态字段来源于 `TASK_BOARD.md` 的 `Project Snapshot` 段（Current Sprint / Current Task / Last Milestone），保证单一数据源。

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
| PR Review 需修改 | Reviewer 反馈 | 无需改文档 | 直接改代码 + push，CI 重跑 |
| PR Merge | `gh pr merge --squash` | `CHANGELOG.md` + `TASK_BOARD.md` | CHANGELOG 加条目；TASK_BOARD Status → Done，Branch Status → Merged，DoD 勾选 CI 验证 + 合并 |
| Sprint 关闭 | Sprint 全部 Task Done | `README.md` + `TASK_BOARD.md` | README 状态快照刷新（§6）；TASK_BOARD 加 Close Gate 段 |

### 7.2 禁止

- [X] Feature 开发完成后才补 TASK_BOARD 状态（应在分支创建时即 Developing）
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
