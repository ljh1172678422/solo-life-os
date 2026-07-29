# Document Version Synchronization Rule

Version: 1.0

Last Update: 2026-07-28

> 本规则约束核心文档的修改条件，防止文档频繁变更导致架构漂移。
> 对齐 AGENTS §13 文档版本管理 / §8 Architecture Change Process。

---

## 1. 修改条件

| 文件 | 修改条件 | 修改人 |
|------|----------|--------|
| `ARCHITECTURE.md` | 架构边界变化、新增模块、新增 ADR | Architecture Agent |
| `DATABASE_DESIGN.md` | Schema 变化、新增表、新增字段、枚举变化 | Architecture Agent |
| `AGENTS.md` | AI 行为规则变化、权限分级变化、Git 工作流变化 | Architecture Agent |
| `TASK_BOARD.md` | Task 状态变化、Owner 变化 | 任何 Agent（仅自己 Owner 的任务） |
| `SPRINT_PLAN.md` | Sprint 范围变化、Sprint 顺序调整 | Architecture Agent |
| `CODE_RULES.md` | 编码规范变化、新增语言规则 | Architecture Agent |
| `CHANGELOG.md` | 每次 PR 合并 | PR 提交者 |
| `AI_CHANGELOG.md` | 每次 AI Agent 行为 | AI Agent |
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
2. 在 `AI_CHANGELOG.md` 记录修改原因（AI Agent 行为）
3. 更新文件头部的 `Version` 和 `Last Update`
4. 在 PR 描述中说明修改原因

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
