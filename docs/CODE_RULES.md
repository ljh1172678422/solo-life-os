# 编码规范


# 前端


技术：

Vue3

TypeScript

Pinia


规则：

禁止：

any


必须：

类型定义完整。



组件：

一个组件一个职责。



---

# Java


规范：

Spring Boot


Controller

↓

Service

↓

Domain

↓

Repository


禁止：

Controller写业务逻辑。


---

# 数据库


所有表：

必须：

created_time

updated_time


所有删除：

优先软删除。


---

# API


统一返回：


```
{
  code: 0,
  message: "",
  data: {}
}
```


---

# Git


## Commit 规范


采用 Conventional Commits：


```
type(scope): description
```


### type 类型


- feat     新增功能
- fix      修复缺陷
- refactor 重构（不改外部行为）
- docs     文档变更
- chore    构建 / 工程任务 / 仓库维护
- test     测试相关
- perf     性能优化


### scope 模块名


按核心模块边界（见 PROJECT_CONTEXT §17）取值：


- repo          仓库工程本身
- context       PROJECT_CONTEXT
- architecture  ARCHITECTURE
- database      DATABASE_DESIGN
- agents        AGENTS / AI_CHANGELOG
- rules         CODE_RULES
- sprint        SPRINT_PLAN / TASK_BOARD
- user          User 模块
- today         Today 模块
- explore       Explore 模块
- mood          Mood 模块
- growth        Growth 模块
- community     Community 模块
- story         Life Story 模块
- ai            AI 层 / Orchestrator / Agent / Memory


### 示例


```
docs(context): upgrade PROJECT_CONTEXT to v1.2
feat(today): add daily planning module
fix(auth): resolve token refresh issue
refactor(ai): optimize memory service
chore(repo): add github workflow
```


## 分支命名


```
feature/<模块>-<任务>
```


示例：

```
feature/backend-user-module
feature/ai-memory-agent
feature/docs-architecture
```


## 禁止事项


- 禁止直接提交到 main / develop
- 禁止无 type 的 commit
- 禁止一个 commit 跨多个模块
