# Solo Life OS Task Board

Version: 2.0

Last Update: 2026-07-28


> 本看板是 Sprint 执行层入口，所有 AI Agent 领取任务、更新状态、提交 PR 必须先查阅本文档。
> 与 SPRINT_PLAN v2.1、AGENTS v1.2 §7 Task Ownership、ARCHITECTURE v2.2 §22 完全对齐。
> 状态机：Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived（AGENTS §7）。


---

# Current Sprint


Sprint 0：工程初始化


Status:

Planning


Sprint Goal:

建立多端研发基础设施，使后续 Module Sprint 可以进入开发。


Depends:

无（首个 Sprint）


Reviewer Gate:

Architecture Agent


---

# Active Tasks


## TASK-0001 Architecture Foundation


Owner:

Architecture Agent


Reviewer:

QA Agent


Status:

Designing


Module:

Foundation


Branch:

feature/foundation-architecture


Description:

完成 Sprint 0 架构基础设计，为后续 Backend / Frontend / Database / AI 任务提供约束基线。


Todo:

- [ ] 确认 Modular Monolith 基础结构
- [ ] 确认 Backend Package Convention（ARCHITECTURE §19）
- [ ] 创建 ADR-0005 Vector DB Adapter Interface
- [ ] 确认环境配置规范（.env / docker-compose 分层）
- [ ] 更新 ARCHITECTURE.md（如涉及边界调整）


DoD:

- [ ] ADR-0005 进入 Proposed 状态
- [ ] Backend / Frontend / Database 任务可在不二次确认架构的情况下启动


---


## TASK-0002 Backend Foundation


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Assigned


Module:

Foundation


Branch:

feature/backend-foundation


Depends:

TASK-0001


Description:

初始化 Spring Boot 后端工程，建立分层骨架与基础设施。


Todo:

- [ ] 创建 Spring Boot 工程（Java 17 + Spring Boot 3.x）
- [ ] 配置 Modular Monolith Package：

```
backend/
└── solo-server/
    └── com/sololifeos/
        ├── common/
        │   ├── response/      统一返回
        │   ├── exception/      全局异常
        │   ├── logging/        日志与 traceId
        │   └── config/        全局配置
        ├── user/
        ├── today/
        ├── explore/
        ├── mood/
        ├── growth/
        ├── community/
        ├── story/
        └── ai/
            ├── orchestrator/
            ├── agents/
            ├── memory/
            └── llm/
```

- [ ] 集成基础依赖：
  - Spring Web
  - Spring Validation
  - PostgreSQL Driver
  - Spring Data Redis
  - Flyway
  - SpringDoc OpenAPI
- [ ] 实现 `/health` 健康检查
- [ ] 实现统一 Response Wrapper（ARCHITECTURE §11）
- [ ] 实现 Global Exception Handler（ARCHITECTURE §20）
- [ ] 接入 traceId 透传（ARCHITECTURE §16）


DoD:

- [ ] `./gradlew bootRun` 可启动
- [ ] `GET /health` 返回 200
- [ ] OpenAPI 文档可访问 `/swagger-ui.html`


禁止:

- [X] 业务 Entity / Repository / Controller 实现
- [X] 数据库 Migration（归 TASK-0004）
- [X] 真实 LLM / Redis 业务逻辑


---


## TASK-0003 Frontend Foundation


Owner:

Frontend Agent


Reviewer:

Architecture Agent


Status:

Assigned


Module:

Foundation


Branch:

feature/frontend-foundation


Depends:

TASK-0001


Description:

初始化 uni-app 多端工程，建立可扩展前端骨架。


Todo:

- [ ] 创建 uni-app 工程目录：

```
apps/
├── h5/                H5 端
├── miniapp/           微信小程序
└── app/               App 端
```

- [ ] 配置 TypeScript（禁 any / as any，CODE_RULES §2）
- [ ] 配置 Vue3 + `<script setup>` 语法
- [ ] 配置 Pinia 状态管理
- [ ] 配置 uni-app Router
- [ ] 创建基础目录结构：

```
src/
├── api/               接口封装（必经，禁直连 axios）
├── stores/
├── pages/
├── components/
├── composables/
├── types/
└── utils/
```

- [ ] 实现 HTTP 请求统一封装（携带 traceId）
- [ ] H5 端首页可启动


DoD:

- [ ] `npm run dev:h5` 可启动
- [ ] 首页可访问
- [ ] TypeScript 严格模式开启
- [ ] API 层封装就绪


禁止:

- [X] 业务页面实现（Page01~Page28）
- [X] 直接调用 axios
- [X] 在组件内写业务逻辑


---


## TASK-0004 Database Foundation


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Assigned


Module:

Foundation


Branch:

feature/database-foundation


Depends:

TASK-0001


Description:

建立数据库基础环境与初始 Migration（仅 User Module 前置表）。


Todo:

- [ ] PostgreSQL Docker 环境（docker-compose）
- [ ] Redis Docker 环境（docker-compose）
- [ ] Flyway 初始化配置
- [ ] Migration 目录规范：

```
database/
├── design/            设计稿（Architecture Agent）
└── migrations/        迁移脚本（Backend Agent）
    ├── V20260728_001__create_user_table.sql
    ├── V20260728_002__create_user_preference_table.sql
    └── V20260728_003__create_tag_table.sql
```

- [ ] 创建初始 Migration（仅以下三张表，对齐 DATABASE_DESIGN v2.1）：
  - `user`
  - `user_preference`
  - `tag`
- [ ] 配置应用层连接池


DoD:

- [ ] `docker-compose up` 可拉起 PostgreSQL + Redis
- [ ] `./gradlew flywayMigrate` 幂等执行通过
- [ ] 新环境可完整初始化
- [ ] 三张表结构与 DATABASE_DESIGN §6.1 / §6.2 / §6.10 完全一致


禁止:

- [X] 创建 daily_plan / mood_record / goal / community_event 等业务表（各 Module Sprint 负责）
- [X] 物理外键约束（DATABASE_DESIGN §9）
- [X] 跨 Module 表合并迁移移


---

## TASK-0005 AI Platform Foundation


Owner:

AI Agent


Reviewer:

Architecture Agent


Status:

Assigned


Module:

AI Platform


Branch:

feature/ai-foundation


Depends:

TASK-0001


Description:

建立 AI Platform 基础接口，为 Sprint 5 真实接入预留扩展点。


Todo:

- [ ] 创建 Agent Interface（统一 execute 契约）
- [ ] 创建 Router Interface（路由策略抽象）
- [ ] 创建 Memory Interface（读写长期记忆）
- [ ] 创建 Conversation Interface（短期对话上下文）
- [ ] 创建 VectorStore Adapter Interface（ADR-0005 决策点）
- [ ] 创建 LLMProvider Interface（模型调用抽象层）


目录结构（ARCHITECTURE §19）：

```
ai/
├── agents/
│   ├── Agent.java
│   └── (各 Agent 接口预留，不实现)
├── orchestrator/
│   └── AgentRouter.java
├── memory/
│   ├── MemoryService.java
│   └── ConversationService.java
└── llm/
    ├── LLMProvider.java
    └── VectorStoreAdapter.java
```


DoD:

- [ ] 5 个核心 Interface 全部定义
- [ ] 接口编译通过
- [ ] 接口签名与 ARCHITECTURE §7 一致


禁止:

- [X] 真实 LLM 接入（Sprint 5）
- [X] Prompt 编写（Sprint 5）
- [X] Agent 业务实现（Sprint 5）
- [X] Vector DB 实例部署（Sprint 5）
- [X] ai_memory / ai_conversation 表 Migration（Sprint 5）


---

## TASK-0006 CI/CD Foundation


Owner:

Backend Agent


Reviewer:

QA Agent


Status:

Backlog


Module:

DevOps


Branch:

feature/devops-foundation


Depends:

TASK-0002, TASK-0003


Description:

建立 CI 基础流水线，所有 PR 必须经 CI 检查。


Todo:

- [ ] `.github/workflows/backend-ci.yml`
- [ ] `.github/workflows/frontend-ci.yml`
- [ ] Backend：编译 + 单元测试 + lint
- [ ] Frontend：构建 + 类型检查 + lint
- [ ] PR 模板约束（已存在 `.github/PULL_REQUEST_TEMPLATE.md`）
- [ ] 分支保护规则建议（main / develop 禁直推）


DoD:

- [ ] PR 提交后 CI 自动触发
- [ ] Backend CI 通过
- [ ] Frontend CI 通过


---


# Sprint 0 Definition of Done


## Code

- [ ] Backend 可启动并访问 `/health`
- [ ] Frontend H5 可启动并访问首页
- [ ] Database 可初始化（PostgreSQL + Redis 可用）
- [ ] AI Platform 5 个核心 Interface 已定义


## Test

- [ ] Backend 单元测试框架运行（JUnit 5）
- [ ] API 测试框架运行（MockMvc / WebTestClient）


## Documentation

- [ ] CHANGELOG.md 更新
- [ ] AI_CHANGELOG.md 更新
- [ ] TASK_BOARD.md 状态全部更新为 Done


## Architecture

- [ ] 无越权修改（ARCHITECTURE §22）
- [ ] 无重复 Entity（ARCHITECTURE §3）
- [ ] 无跨模块数据库访问（ARCHITECTURE §2）
- [ ] AI 未直连数据库（ARCHITECTURE §21）


---

# Completed


暂无


---

# Next Sprint


Sprint 1：User Module


Goal:

完成用户注册、登录、资料、偏好设置。


Depends:

Sprint 0


预计任务（Sprint 1 启动时拆分）：

- TASK-0101 User Migration（user / user_preference / tag 已在 Sprint 0 创建）
- TASK-0102 User Domain Layer
- TASK-0103 User Application Service
- TASK-0104 User Controller + DTO
- TASK-0105 User Frontend（登录 / 资料 / 偏好页）
- TASK-0106 User Test Suite


---


# Task Status Legend


| 状态 | 含义 |
|------|------|
| Backlog | 已识别，未分配 Owner |
| Assigned | 已分配 Owner，未启动 |
| Designing | Owner 进行方案设计 |
| Developing | 编码中 |
| Reviewing | PR 提交，Reviewer 审查中 |
| Testing | QA 测试中 |
| Done | 通过 DoD，已合并 |
| Archived | Sprint 结束归档 |


---

# Version History


## v2.0 - 2026-07-28

- 全量重写：从「功能清单」升级为「Module + Owner + Reviewer + Status」任务卡
- 与 SPRINT_PLAN v2.1 / AGENTS v1.2 §7 / ARCHITECTURE v2.2 §22 对齐
- 拆分 Sprint 0 为 6 个独立任务（TASK-0001 ~ TASK-0006）
- 引入任务状态机（Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived）
- 收紧 AI Agent 任务边界：Sprint 0 仅定义 Interface，禁止真实 LLM / Prompt / Agent 实现
- 收紧数据库边界：Sprint 0 仅创建 user / user_preference / tag 三张表
- 新增 Sprint 0 Definition of Done 四层约束（Code / Test / Documentation / Architecture）
- 新增 Next Sprint 任务预拆分（Sprint 1 User Module）
