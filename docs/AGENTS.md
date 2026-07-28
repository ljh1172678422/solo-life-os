# AI Agent 协作规范

Version: 1.1

Last Update: 2026-07-28


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
| Architecture Agent | `docs/` `database/design/` | `apps/` `ai/agents/prompts/` `database/migrations/` |
| Backend Agent | `backend/` `database/migrations/` | `apps/` |
| Frontend Agent | `apps/` | `backend/` `ai/` |
| AI Agent | `ai/` `docs/AGENTS.md`（自描述部分） | `backend/` `apps/` |
| QA Agent | `.github/` `docs/review/` | 业务源码 |


注意：

- `database/migrations/` 是高风险目录，归 Backend Agent 执行
- Architecture Agent 负责 `database/design/`（设计稿），不直接产出迁移脚本
- 流程：Architecture 设计 → Backend 写 migration → QA 验证


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
- 多个 Agent 同时修改同一核心模块（见 §7 Task Ownership）
- 先写代码再补架构（见 §8 Architecture Change Process）


---

# 7. Task Ownership


所有开发任务必须绑定唯一 Owner Agent，避免多 Agent 并发改同一模块造成冲突。


任务卡片格式：


```
TASK:      TASK-001
Owner:     Backend Agent
Reviewer:  Architecture Agent
Status:    Doing
Module:    user
Branch:    feature/backend-user-module
```


规则：


- 同一核心模块同一时间只能有一个 Primary Owner
- 如需协作，必须明确：
  - Primary Owner：负责实现
  - Secondary Reviewer：负责评审
- 领取任务前必须检查 docs/TASK_BOARD.md，确认目标模块未被占用
- 完成或释放任务时，必须立即更新 TASK_BOARD 状态


禁止：

多个 Agent 同时修改同一核心模块。


---

# 8. Architecture Change Process


任何影响以下内容的修改，必须经过 Architecture Agent 审核：


- 数据模型
- 核心实体
- 模块边界
- API 契约
- AI Agent 结构


流程：


```
提出变更
  ↓
Architecture Agent 评估
  ↓
更新 docs/ARCHITECTURE.md
  ↓
更新 docs/DATABASE_DESIGN.md
  ↓
进入开发
```


禁止：

先写代码，再补架构。


注意：

- Backend Agent 不得擅自新增核心 Entity（如出现 User / UserProfile / AccountUser 三套用户体系）
- 任何新 Entity 必须先在 DATABASE_DESIGN.md 登记
- 任何新模块必须先在 ARCHITECTURE.md 登记


---

# 9. AI 提交前检查


提交 PR 前必须逐项确认：


## Context

- [ ] 已阅读 docs/PROJECT_CONTEXT.md


## Architecture

- [ ] 未违反 docs/ARCHITECTURE.md
- [ ] 如涉及架构变更，已完成 §8 流程


## Database

- [ ] 未创建重复模型
- [ ] 新增字段已在 DATABASE_DESIGN.md 登记


## Code

- [ ] 已执行测试
- [ ] 未跨模块直连数据库
- [ ] 未引入未经批准的框架


## Documentation

- [ ] 已更新相关文档
- [ ] 已更新 docs/TASK_BOARD.md


## Git

- [ ] Commit 符合 `type(scope): description` 规范
- [ ] 未直接提交到 develop / main


---

# 10. Agent Handoff Protocol


Agent 完成任务后，必须在 PR 描述或 docs/AI_CHANGELOG.md 中输出交接信息，供下一任 Agent 接续。


交接格式：


```
## Changed
修改内容

## API
新增接口（路径 / 方法 / 入参 / 返回）

## Database
新增字段 / 新增表

## Dependency
依赖变化

## Next Agent
建议下一步负责人与任务
```


示例：


```
## Changed
新增用户资料接口

## API
POST /api/user/profile
  入参: nickname, avatar, city
  返回: UserProfile

## Database
user_profile 表新增 nickname 字段

## Dependency
无

## Next Agent
Frontend Agent — 对接用户资料页
```


---

# 11. 文档版本管理


所有核心文档必须维护版本（详见 PROJECT_CONTEXT §20）：


- 文件名：`XXX.md`
- 版本：`v1.x`
- 每次修改必须记录：修改时间 / 修改原因 / 影响范围


禁止无记录修改核心设计。


---

# 12. 完成任务后必须更新


每次任务完成，必须同步更新以下文档：


1. docs/TASK_BOARD.md        勾选完成项、更新 Sprint 状态
2. docs/CHANGELOG.md        按变更类型追加产品变更记录
3. docs/AI_CHANGELOG.md     记录 AI Agent 的行为、原因、影响范围（含 §10 Handoff 信息）
