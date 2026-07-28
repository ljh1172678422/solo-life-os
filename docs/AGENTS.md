# AI Agent 协作规范

Version: 1.3

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


采用 **Git Flow 精简版**，多层结构：


```
main        产品稳定版本
 │          仅通过 PR 合并，禁止直接提交
 │
develop     研发集成分支
 │          所有 Agent 的开发结果最终汇入
 │
 ├── feature/*   每任务一分支
 ├── hotfix/*    生产紧急修复
 └── docs/*      文档独立生命周期
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


### hotfix


- 定位：生产环境严重问题紧急修复
- 命名格式：`hotfix/<模块>-<简述>`
- 流程：


```
hotfix 分支
  ↓
QA 验证
  ↓
PR → main
  ↓
同步回 develop
```


注意：

hotfix 是唯一允许从 main 切出并直接 PR 回 main 的分支类型，
但严禁绕过 QA 验证与人工审核。


### docs


- 定位：文档独立生命周期（PROJECT_CONTEXT / ARCHITECTURE / DATABASE_DESIGN 等大版本升级）
- 命名格式：`docs/<文档>-<版本>`
- 示例：`docs/project-context-v1.3`、`docs/database-design-v1`


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
Status:    Developing
Module:    user
Branch:    feature/backend-user-module
```


## 任务生命周期


所有任务必须按以下状态机流转：


```
Backlog
  ↓
Assigned
  ↓
Designing
  ↓
Developing
  ↓
Reviewing
  ↓
Testing
  ↓
Done
  ↓
Archived
```


状态说明：


- Backlog      待领取
- Assigned     已分配 Owner，未开始
- Designing    架构 / 数据模型设计阶段（Architecture Agent 常驻此态）
- Developing   编码中
- Reviewing    PR 评审中
- Testing      QA 验证中
- Done         已合并，已完成文档更新
- Archived     归档，不再活跃


规则：


- 同一核心模块同一时间只能有一个 Primary Owner
- 如需协作，必须明确：
  - Primary Owner：负责实现
  - Secondary Reviewer：负责评审
- 领取任务前必须检查 docs/TASK_BOARD.md，确认目标模块未被占用
- 状态变更时必须立即更新 TASK_BOARD
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

# 11. Prompt 文件管理规则


Prompt 是 AI 系统的核心资产，等同代码，必须受控变更。


任何修改 `ai/prompts/` 下的文件，必须：


- 说明修改原因
- 说明影响 Agent
- 说明示例输入输出变化
- 通过 PR 审核，禁止直接提交


禁止：

无记录修改 Prompt。


注意：

Prompt 漂移会导致 Agent 行为不一致，是 AI 项目最隐蔽的风险。
每次 Prompt 变更必须在 docs/AI_CHANGELOG.md 中记录。


---

# 12. Repository Structure


仓库目录地图，AI Agent 必须遵守此结构，不得擅自在根目录创建新目录。


```
Solo-Life-OS
│
├── AGENTS.md                 AI Agent 根入口
├── README.md
├── .gitignore
│
├── docs/
│   ├── PROJECT_CONTEXT.md    项目宪法
│   ├── ARCHITECTURE.md       系统架构
│   ├── DATABASE_DESIGN.md    数据模型
│   ├── AGENTS.md             协作规范（本文档）
│   ├── CODE_RULES.md         编码规范
│   ├── TASK_BOARD.md         任务看板
│   ├── SPRINT_PLAN.md        Sprint 规划
│   ├── CHANGELOG.md          产品变更记录
│   ├── AI_CHANGELOG.md       AI 行为日志
│   └── review/              QA 评审记录
│
├── apps/
│   ├── h5/                  H5 端
│   ├── miniapp/             微信小程序
│   └── app/                 App 端
│
├── backend/                  Spring Boot 服务
│
├── ai/
│   ├── agents/              Agent 实现
│   ├── prompts/             Prompt 文件（受 §11 约束）
│   └── memory/              Memory 系统
│
├── database/
│   ├── design/              设计稿（Architecture Agent）
│   └── migrations/          迁移脚本（Backend Agent）
│
├── scripts/                  工程脚本
│
└── .github/
    ├── workflows/           CI/CD
    └── PULL_REQUEST_TEMPLATE.md
```


注意：

- `apps/` `backend/` `ai/` `database/` `scripts/` 暂未创建，将在对应 Sprint 启动时建立
- 当前阶段（Sprint 0）仅 `docs/` 与 `.github/` 处于活跃状态
- 新增目录必须先在本文档登记


---

# 13. 文档版本管理


所有核心文档必须维护版本（详见 PROJECT_CONTEXT §20）：


- 文件名：`XXX.md`
- 版本：`v1.x`
- 每次修改必须记录：修改时间 / 修改原因 / 影响范围


禁止无记录修改核心设计。


---

# 14. 完成任务后必须更新


每次任务完成，必须同步更新以下文档：


1. docs/TASK_BOARD.md        勾选完成项、更新 Sprint 状态
2. docs/CHANGELOG.md        按变更类型追加产品变更记录
3. docs/AI_CHANGELOG.md     记录 AI Agent 的行为、原因、影响范围（含 §10 Handoff 信息）


---


# 15. Git Branch Governance


§5.2 已声明「AI Agent 严禁直接操作 develop 或 main」，本节补充**强制执行机制**，确保规则不被绕过。


## 15.1 Develop Branch Protection（硬约束）


禁止以下行为：


- 在 `develop` 分支上执行 `git commit`
- 在 `develop` 分支上执行 `git push origin develop`
- 在 `main` 分支上执行任何写操作


例外（允许直接提交 develop）：


- 仓库初始化阶段（TASK-0001 Architecture Foundation 之前的文档治理提交）
- 文档热修复（仅 docs/ 目录变更，不涉及代码）
- 用户明确批准的架构紧急修复


从 TASK-0002 开始，所有代码任务必须使用 feature 分支，无例外。


## 15.2 Task Start Checklist（强制）


每个 TASK 进入 Developing 状态前，必须完成以下检查：


```
TASK START CHECKLIST


[1] Read TASK_BOARD，确认 Owner / Reviewer / Branch
[2] git checkout develop
[3] git pull origin develop（确保 develop 最新）
[4] git checkout -b feature/<task-name>（从 TASK_BOARD 提取 Branch 字段）
[5] git push -u origin feature/<task-name>
[6] 验证当前分支：git branch --show-current 输出为 feature/<task-name>
[7] 仅在验证通过后，Status → Developing
```


如果 `git branch --show-current` 输出为 `develop` 或 `main`，**禁止继续**，必须回到步骤 4。


## 15.3 Task Commit Workflow


开发过程中的提交规范：


```
开发中（feature 分支）:
  git add <specific files>
  git commit -m "feat(<module>): <description>"
  git push origin feature/<task-name>


完成时:
  git push origin feature/<task-name>
  → 创建 PR: feature/<task-name> → develop
  → TASK_BOARD Status → Reviewing
  → 等待 Reviewer Agent / Human 审核
  → PR 合并后 Status → Done
```


## 15.4 Branch Status 字段


TASK_BOARD 每个 TASK 卡增加 Branch Status 字段：


```
Branch:
feature/<task-name>

Branch Status:
Created / Pushed / PR-Open / Merged
```


状态流转：


```
Created   分支已创建（git checkout -b）
   ↓
Pushed    分支已推送到远端（git push -u）
   ↓
PR-Open   PR 已创建，等待审核
   ↓
Merged    PR 已合并到 develop
```


## 15.5 AI Agent 自检规则


AI Agent 在执行任何 git 命令前，必须自检：


```
IF current branch == develop OR current branch == main:
    IF task is documentation-only AND no code changes:
        ALLOW commit（§15.1 例外）
    ELSE:
        ABORT: "禁止在 develop/main 上提交代码，请先创建 feature 分支"
        CREATE feature branch per TASK_BOARD
        RETRY on feature branch
ELSE:
    PROCEED
```


## 15.6 PR 合并条件


PR 合并到 develop 前，必须满足：


- TASK_BOARD Status 已更新为 Reviewing
- DoD 所有项已勾选
- 代码编译通过（Backend: mvn clean compile，Frontend: npm run build）
- CHANGELOG.md 已更新
- AI_CHANGELOG.md 已更新
- Reviewer 已审核（Architecture Agent / QA Agent）


## 15.7 Task Branch Validation（硬检查）


每次执行代码任务前，必须执行分支验证：


```
[1] 读取 TASK_BOARD 中当前 TASK 的 Branch 字段
[2] 执行：git branch --show-current
[3] 比对：当前分支 == TASK_BOARD.Branch
[4] 如不匹配，禁止执行 git add / git commit / git push
```


验证失败示例：


```
TASK_BOARD:
  Branch: feature/frontend-foundation

Current branch:
  develop

❌ Task branch mismatch
   Expected: feature/frontend-foundation
   Current:  develop
   Action:  请先执行 §15.2 Task Start Checklist 创建 feature 分支
```


验证通过示例：


```
TASK_BOARD:
  Branch: feature/backend-foundation

Current branch:
  feature/backend-foundation

✅ Task branch validated
```


此规则与 §15.5 自检规则联动：§15.5 检查是否在 develop/main，§15.7 检查是否在正确的 feature 分支。两者都必须通过才能执行 git 写操作。


## 15.8 Compile Validation（编译验证）


TASK 进入 Reviewing 状态前，必须通过编译验证：


- Backend TASK：`mvn clean compile`（必须 BUILD SUCCESS）
- Frontend TASK：`npm run build`（必须 exit code 0）
- AI TASK：`mvn clean compile`（如为 Java）


TASK_BOARD 每个 TASK 卡增加 Validation 字段：


```
Validation:
✅ mvn clean compile passed (YYYY-MM-DD)
或
❌ mvn clean compile failed — <错误简述>
```


禁止：


- 在 Validation 为 ❌ 时将 Status 改为 Reviewing
- 在 Validation 为 ❌ 时创建 PR
- 隐藏编译失败日志



