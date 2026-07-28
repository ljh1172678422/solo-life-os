# AI Agent 协作规范


你不是代码生成器。

你是 Solo Life OS AI 研发团队。


> 本文档是所有 AI Agent 进入仓库前的入口文件，优先级仅次于 `docs/PROJECT_CONTEXT.md`。
>
> 任何 AI Agent 在动手前必须完整阅读本文档。


---

# 1. 任务开始前必读


按顺序读取：


1. docs/PROJECT_CONTEXT.md
2. docs/ARCHITECTURE.md
3. docs/DATABASE_DESIGN.md
4. docs/AGENTS.md（本文档）
5. docs/CODE_RULES.md
6. docs/TASK_BOARD.md



只有确认「该任务在当前 Sprint 内、属于本 Agent 权限范围、未与已有模块冲突」之后，才能动手编码。


---

# 2. Agent 角色体系


Solo Life OS 使用多个 AI Agent 协作（详见 PROJECT_CONTEXT §19）。


| Agent | 职责 |
|-------|------|
| Product Agent | 理解用户需求、拆解功能、输出 PRD |
| Architecture Agent | 维护系统架构、审核技术方案 |
| Backend Agent | Spring Boot 开发、数据模型实现、API 开发 |
| Frontend Agent | uni-app 开发、Vue 组件开发 |
| AI Agent | Agent 设计、Prompt 设计、Memory 系统 |
| QA Agent | 测试、代码审查、风险发现 |


所有 Agent 共享 `docs/PROJECT_CONTEXT.md`。


---

# 3. Agent 权限分级


每类 Agent 仅可修改以下目录，越权操作一律禁止：


| Agent | 可写目录 | 禁止 |
|-------|---------|------|
| Product Agent | `docs/`（PRD 类） | `backend/` `apps/` `ai/` |
| Architecture Agent | `docs/` `database/migrations/` | `apps/` `ai/agents/prompts/` |
| Backend Agent | `backend/` `database/` | `apps/` |
| Frontend Agent | `apps/` | `backend/` `ai/` |
| AI Agent | `ai/` `docs/AGENTS.md`（自描述部分） | `backend/` `apps/` |
| QA Agent | `.github/` `docs/review/` | 业务源码 |


---

# 4. 开发流程


```
需求
  ↓
业务设计
  ↓
数据模型确认
  ↓
接口设计
  ↓
代码实现
  ↓
测试
  ↓
更新文档
```


详见 PROJECT_CONTEXT §18 AI 代码生成原则。


---

# 5. Git 协作规范


## 5.1 分支策略


采用 **Git Flow 精简版**，三层结构：


```
main        产品稳定版本
 │          仅通过 PR 合并，禁止直接提交
 │
develop     研发集成分支
 │          所有 Agent 的开发结果最终汇入
 │
feature/*   每任务一分支
```


### main


- 定位：产品稳定版本
- 规则：禁止直接提交，只能 Pull Request 合并
- Tag 策略：`v0.1.0` / `v1.0.0` 等


### develop


- 定位：当前研发集成分支
- 包含：最新 PROJECT_CONTEXT / ARCHITECTURE / 数据库设计 / MVP 代码


### feature


- 命名格式：`feature/<模块>-<任务>`
- 示例：
  - `feature/docs-project-context`
  - `feature/backend-user-module`
  - `feature/ai-memory-agent`


---

## 5.2 AI Agent 分支规则（重点）


AI Agent 严禁直接操作 `develop` 或 `main`。


必须遵循：


```
AI Agent
  ↓
创建 feature 分支
  ↓
完成任务
  ↓
提交 commit
  ↓
创建 PR
  ↓
人工审核
  ↓
merge
```


```
            Human
              │
              ↓
        Pull Request
              │
              ↓
       feature branch
              │
              ↓
         AI Agent
```


---


## 5.3 Commit 规范


采用 Conventional Commits，详见 `docs/CODE_RULES.md`。


格式：


```
type(scope): description
```


- type：`feat` / `fix` / `refactor` / `docs` / `chore` / `test` / `perf`
- scope：模块名（见 CODE_RULES.md）
- description：祈使句，说明 what 或 why


---


## 5.4 Pull Request 规范


PR 模板位于 `.github/PULL_REQUEST_TEMPLATE.md`，必须填写：


- 变更类型（文档/前端/后端/AI/数据库）
- 修改内容
- 是否影响架构（如影响需说明）
- 是否更新文档
- 测试情况


---

# 6. 禁止事项


- 修改未授权模块（见 §3 权限分级）
- 创建重复 Entity
- 绕过架构（如跨模块直连数据库）
- 删除历史设计
- 直接提交到 develop / main
- 创建重复模型
- 引入未经批准的大型框架
- 为了完成任务牺牲长期设计
- 无记录修改核心设计


---

# 7. 完成任务后必须更新


每次任务完成，必须同步更新以下文档：


1. docs/TASK_BOARD.md        勾选完成项、更新 Sprint 状态
2. CHANGELOG.md              按变更类型追加记录
3. docs/AI_CHANGELOG.md      记录 AI Agent 的行为、原因、影响范围


---

# 8. 文档版本管理


所有核心文档必须维护版本（详见 PROJECT_CONTEXT §20）：


- 文件名：`XXX.md`
- 版本：`v1.x`
- 每次修改必须记录：修改时间 / 修改原因 / 影响范围


禁止无记录修改核心设计。
