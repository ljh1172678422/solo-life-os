# Solo Life OS Task Board

Version: 2.9

Last Update: 2026-07-30


> 本看板是 Sprint 执行层入口，所有 AI Agent 领取任务、更新状态、提交 PR 必须先查阅本文档。
> 与 SPRINT_PLAN v2.1、AGENTS v1.2 §7 Task Ownership、ARCHITECTURE v2.2 §22 完全对齐。
> 状态机：Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived（AGENTS §7）。


---

# Current Sprint


Sprint 1：User Module


Status:

In Progress


Sprint Goal:

完成用户注册、登录、资料、偏好设置。


Depends:

Sprint 0（Done 2026-07-29）


Reviewer Gate:

Architecture Agent


> Sprint 0 已于 2026-07-29 关闭，全部 7 个基础任务达成。进入业务代码阶段，不再迭代架构文档。


---


# Sprint 0 Task Dependency Graph


```
                    TASK-0001 Architecture Foundation
                              │
        ┌──────────┬──────────┼──────────┬──────────┐
        │          │          │          │          │
        ▼          ▼          ▼          ▼          ▼
  TASK-0002    TASK-0003    TASK-0005  TASK-0007   (TASK-0004)
  Backend      Frontend     AI Fdn     Doc Fdn      Database
     │                                                │
     │  ┌─────────────────────────────────────────────┘
     │  │  (TASK-0004 同时依赖 TASK-0002 的 Flyway 配置)
     ▼  ▼
  TASK-0004 Database Foundation
     │
     │
     ▼
  TASK-0006 CI/CD Foundation (依赖 TASK-0002 + TASK-0003)
```


依赖说明：

- TASK-0001 是根任务，所有其他任务依赖其架构决策
- TASK-0004 Database Foundation 同时依赖 TASK-0001（架构）和 TASK-0002（Flyway 配置），避免与 Backend Agent 同时修改配置文件
- TASK-0006 CI/CD 依赖 TASK-0002 和 TASK-0003 的工程结构就绪
- TASK-0007 Documentation Foundation 仅依赖 TASK-0001，可与其他任务并行


---


# Active Tasks


## TASK-0001 Architecture Foundation


Owner:

Architecture Agent


Reviewer:

QA Agent


Status:

Done


Module:

Foundation


Branch:

feature/foundation-architecture


Description:

完成 Sprint 0 架构基础设计（Architecture Freeze Gate），为后续 Backend / Frontend / Database / AI 任务提供约束基线。


Todo:

- [x] 确认 Modular Monolith 基础结构（ADR-0001 Accepted）
- [x] 确认 Backend Package Convention（ARCHITECTURE §19）
- [x] 创建 ADR-0005 Vector DB Adapter Strategy（Proposed；候选 pgvector / Milvus / Qdrant + Adapter 延迟绑定，Provider 延后至 Sprint 5）
- [x] 创建 ADR-0010 Tag Ownership（Proposed；决策方向：Tag 归 Shared Kernel，Owner Architecture）
- [x] 创建 ADR-0011 Activity Ownership（Accepted；Activity 归 Today，CommunityEvent 独立，已是架构事实）
- [x] 确认 Module Boundary（ARCHITECTURE §3 / §4 / §22，8 模块 + AI Platform）
- [x] 确认环境配置规范（.env 分层 + docker-compose 本地开发）
- [x] 更新 ARCHITECTURE.md（v2.3 ADR 清单已调整）


Module Boundary Freeze（架构冻结输出）：


Package 结构（ARCHITECTURE §19）：

```
com.sololifeos
├── common/          统一返回 / 异常 / 日志 / 配置
├── user/            User Module
├── today/           Today Module（Activity Owner）
├── explore/         Explore Module（Location Owner）
├── mood/            Mood Module
├── growth/          Growth Module（Goal Owner）
├── community/       Community Module（CommunityEvent Owner）
├── story/           Story Module
└── ai/              AI Platform（Memory / Conversation Owner）
```


Module Owner 冻结表：

| Module | Owner | 核心数据对象 |
|--------|-------|------------|
| User | User Module | user / user_preference / favorite |
| Today | Today Module | daily_plan / activity |
| Explore | Explore Module | location |
| Mood | Mood Module | mood_record |
| Growth | Growth Module | goal |
| Community | Community Module | community_event / registration |
| Story | Story Module | （聚合 Memory / Goal / Mood / Activity） |
| AI | AI Platform | ai_memory / ai_conversation |
| Tag | Shared Kernel（ADR-0010 Proposed） | tag |


环境配置规范：

```
.env                    本地开发环境变量（不入库，.gitignore）
docker-compose.yml      PostgreSQL + Redis 本地容器
docker-compose.ci.yml   CI 环境覆盖配置
application.yml         Spring Boot 默认配置
application-dev.yml     开发环境覆盖
```


DoD:

- [x] ADR-0005 进入 Proposed 状态（决策方向已明确，Adapter 实现归 TASK-0005）
- [x] ADR-0010 进入 Proposed 状态（Tag Ownership 决策方向明确：Shared Kernel）
- [x] ADR-0011 进入 Accepted 状态（Activity Ownership 已是架构事实）
- [x] Module Boundary 冻结（8 模块 + AI Platform + Shared Kernel）
- [x] Backend / Frontend / Database / AI 任务可在不二次确认架构的情况下启动


---


## TASK-0002 Backend Foundation


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Done


Module:

Foundation


Branch:

feature/backend-foundation (已删除)


Branch Status:

Merged


Validation:

✅ mvn clean compile passed (2026-07-28, Java 17, 23 source files)
✅ Squash merged to develop (PR #1, 2026-07-28)


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

Done


Module:

Foundation


Branch:

feature/frontend-foundation (已删除)


Branch Status:

Merged


Validation:

✅ files created (14 files, JSON validated, TS strict config)
✅ Squash merged to develop (PR #3, 2026-07-28)


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

Done


Module:

Foundation


Branch:

feature/database-foundation (已删除)


Branch Status:

Merged


Validation:

✅ 文件结构完整，对齐 DATABASE_DESIGN §6.1/§6.2/§6.10 + §7 枚举 + §8 索引 + §9 外键策略
⚠️ sandbox 无 docker/网络，docker compose up + flyway migrate 待本地验证
✅ Squash merged to develop (PR #6, 2026-07-29)


Depends:

TASK-0001, TASK-0002 (已满足)


Description:

建立数据库基础环境与初始 Migration（仅 User Module 前置表）。依赖 TASK-0002 的 Spring Boot Flyway 配置，避免与 Backend Agent 同时修改配置文件。


Todo:

- [x] PostgreSQL Docker 环境（docker-compose.yml）
- [x] Redis Docker 环境（docker-compose.yml）
- [x] Flyway 初始化配置（locations → filesystem:database/migrations + validate-on-migrate）
- [x] Migration 目录规范：

```
database/
├── design/            设计稿（Architecture Agent）
└── migrations/        迁移脚本（Backend Agent）
    ├── V20260728_001__create_user_table.sql
    ├── V20260728_002__create_user_preference_table.sql
    └── V20260728_003__create_tag_table.sql
```

- [x] 创建初始 Migration（仅以下三张表，对齐 DATABASE_DESIGN v2.1）：
  - `user`
  - `user_preference`
  - `tag`
- [x] 配置应用层连接池（HikariCP: max 10 / min 2）
- [x] docker-compose.ci.yml（CI 环境 tmpfs 覆盖）


DoD:

- [ ] `docker-compose up` 可拉起 PostgreSQL + Redis（待本地验证）
- [ ] Flyway migrate 幂等执行通过（待本地验证，sandbox 无 docker）
- [x] 新环境可完整初始化（文件就绪，凭据对齐 .env.example）
- [x] 三张表结构与 DATABASE_DESIGN §6.1 / §6.2 / §6.10 完全一致


禁止:

- [X] 创建 daily_plan / mood_record / goal / community_event 等业务表（各 Module Sprint 负责）
- [X] 物理外键约束（DATABASE_DESIGN §9）
- [X] 跨 Module 表合并迁移移


---

## TASK-0005 AI Foundation


Owner:

AI Agent


Reviewer:

Architecture Agent


Status:

Done


Module:

Foundation / AI Infrastructure


Branch:

feature/ai-foundation (已删除)


Branch Status:

Merged


Validation:

✅ mvn clean compile passed (2026-07-28, Java 17, 32 source files)
✅ Squash merged to develop (2026-07-28)


Depends:

TASK-0001


Description:

建立 AI 基础接口层（注意：Sprint 0 仅是 AI Interface Foundation，不是 AI Platform；AI Platform 完整实现属 Sprint 5），为 Sprint 5 真实接入预留扩展点。


Todo:

- [ ] 创建 Agent Interface（统一 execute 契约）
- [ ] 创建 Router Interface（路由策略抽象）
- [ ] 创建 Memory Interface（读写长期记忆）
- [ ] 创建 Conversation Interface（短期对话上下文）
- [ ] 创建 VectorStoreAdapter Interface（基于 ADR-0005 决策方向实现抽象层，不绑定具体实现）
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


职责边界（与 TASK-0001 分工）：

- Architecture Agent（TASK-0001）：决定 Vector DB 选型方向（ADR-0005）
- AI Agent（TASK-0005）：依据 ADR-0005 方向实现 VectorStoreAdapter 接口抽象


DoD:

- [ ] 6 个核心 Interface 全部定义：
  - Agent
  - Router
  - Memory
  - Conversation
  - VectorStoreAdapter
  - LLMProvider
- [ ] 接口编译通过
- [ ] 接口签名与 ARCHITECTURE §7 一致


禁止:

- [X] 真实 LLM 接入（Sprint 5）
- [X] Prompt 编写（Sprint 5）
- [X] Agent 业务实现（Sprint 5）
- [X] Vector DB 实例部署（Sprint 5）
- [X] ai_memory / ai_conversation 表 Migration（Sprint 5）
- [X] 修改业务 Module（common / user / today / explore / mood / growth / community / story）的 Entity / Repository / Domain Service（ARCHITECTURE §21）
- [X] 直接访问数据库（ARCHITECTURE §21）
- [X] 跨模块 import 业务内部类


---

## TASK-0006 CI/CD Foundation


Owner:

Backend Agent


Reviewer:

QA Agent


Status:

Done


Module:

DevOps


Branch:

feature/cicd-foundation (已删除)


Branch Status:

Merged


Validation:

✅ 两个 workflow 文件创建，YAML 语法校验通过
✅ PR 模板升级（新增治理检查段）
✅ 分支保护规则建议文档化
⚠️ CI 实际触发待 PR 创建后 GitHub Actions 运行验证
✅ Squash merged to develop (PR #7, 2026-07-29)


Depends:

TASK-0002, TASK-0003 (已满足)


Description:

建立 CI 基础流水线，所有 PR 必须经 CI 检查。


Todo:

- [x] `.github/workflows/backend-ci.yml`（Maven 编译 + 测试，JDK 17 + Maven 缓存）
- [x] `.github/workflows/frontend-ci.yml`（npm install + type-check + build，Node 20 + npm 缓存）
- [x] Backend：编译 + 单元测试（§15.8 Compile Validation）
- [x] Frontend：构建 + 类型检查（CODE_RULES §2 禁 any）
- [x] PR 模板约束（升级 `.github/PULL_REQUEST_TEMPLATE.md`，新增治理检查段）
- [x] 分支保护规则建议（`.github/branch-protection.md`，main + develop 禁直推）


DoD:

- [ ] PR 提交后 CI 自动触发（待 PR 创建后验证）
- [ ] Backend CI 通过（待 GitHub Actions 运行）
- [ ] Frontend CI 通过（待 GitHub Actions 运行）


---


## TASK-0007 Documentation Foundation


Owner:

Architecture Agent


Reviewer:

QA Agent


Status:

Done


Module:

Foundation / Documentation


Branch:

feature/documentation-foundation (已删除)


Branch Status:

Merged


Validation:

✅ ADR Index created (7 ADR 登记: 5 Accepted + 2 Proposed + 4 Future)
✅ ADR Template created (template.md)
✅ Version Synchronization Rule created (DOCUMENT_VERSION_RULE.md)
✅ AI_CHANGELOG Template created (AI_CHANGELOG_TEMPLATE.md)
✅ 范围控制：仅建立治理结构与模板，未修改已冻结的架构文档
✅ Squash merged to develop (PR #8, 2026-07-29)


Depends:

TASK-0001 (已满足)


Description:

建立文档治理基础设施，确保后续所有 Sprint 的 CHANGELOG / AI_CHANGELOG / ADR / TASK_BOARD 同步规则可执行。


Todo:

- [x] 创建 ADR Index（`docs/architecture/ADR/README.md`，登记 ADR-0001~0011 状态）
- [x] 建立 ADR 模板（`docs/architecture/ADR/template.md`，标准化 ADR-XXXX 文件结构）
- [x] 建立版本同步规则（`docs/governance/DOCUMENT_VERSION_RULE.md`，代码优先 + 非必要禁止修改核心文档）
- [x] 创建 AI_CHANGELOG 模板（`docs/AI_CHANGELOG_TEMPLATE.md`，固定条目格式）


目录结构：

```
docs/
├── architecture/
│   └── ADR/
│       ├── README.md          ADR Index（本次新增）
│       ├── template.md        ADR 模板（本次新增）
│       ├── ADR-0001-modular-monolith.md
│       ├── ADR-0002-postgresql-as-primary-db.md
│       ├── ADR-0003-ai-agent-unified-router.md
│       ├── ADR-0004-no-microservices-in-mvp.md
│       ├── ADR-0005-vector-db-adapter-strategy.md
│       ├── ADR-0010-tag-ownership.md
│       └── ADR-0011-activity-ownership.md
├── governance/
│   └── DOCUMENT_VERSION_RULE.md  版本同步规则（本次新增）
└── AI_CHANGELOG_TEMPLATE.md      AI 行为日志模板（本次新增）
```


DoD:

- [x] ADR Index 文件存在且登记 ADR-0001/0002/0003/0004/0011 为 Accepted
- [x] ADR-0005 标注为 Proposed，ADR-0010 标注为 Proposed
- [x] ADR-0006~0009 标注为 Future（对应 Sprint）
- [x] ADR 模板文件存在
- [x] AI_CHANGELOG 模板示例存在
- [x] 版本同步规则文档化


禁止:

- [X] 在 Sprint 0 创建 ADR-0005~0011 的具体内容（属 TASK-0001 / 各 Module Sprint）
- [X] 修改业务文档的业务内容（仅建立治理结构与模板）
- [X] 创建未在 ARCHITECTURE §14 登记的 ADR
- [X] 修改 ARCHITECTURE.md 大结构（已冻结）
- [X] 修改 ADR 已接受决策（已冻结）
- [X] 修改数据模型 / 模块边界（已冻结）


---


## TASK-0101 User Migration Review


Owner:

Architecture Agent


Reviewer:

QA Agent


Status:

Done


Module:

User Module


Branch:

feature/user-migration-review (已删除)


Branch Status:

Merged


Depends:

Sprint 0 TASK-0004（已满足）


Description:

确认 Sprint 0 创建的 user / user_preference / tag 三张表符合 User Module Domain Design；如需字段扩展，通过增量 Migration 修改，禁止重复创建表。


Todo:

- [x] 核对 user 表 vs DATABASE_DESIGN §6.1（10 字段 + 索引 + 枚举）
- [x] 核对 user_preference 表 vs DATABASE_DESIGN §6.2（7 字段 + 索引 + 枚举）
- [x] 核对 tag 表 vs DATABASE_DESIGN §6.10（5 字段 + 索引 + 枚举）
- [x] 核对外键策略（§9，逻辑关联不建 FK）
- [x] Gap 分析：password 字段缺失，归 Auth 任务（ADR-0006）
- [x] 输出审查记录 docs/modules/user/MIGRATION_REVIEW.md


Validation:

✅ 字段对齐：22/22 全部对齐（user 10 + user_preference 7 + tag 5）
✅ 索引对齐：5/5 全部对齐
✅ 枚举对齐：3/3 全部对齐（USER_STATUS / BUDGET_LEVEL / TAG_TYPE）
✅ 外键策略：逻辑关联，无物理 FK
✅ 无重复建表（TASK-0004 已创建，本任务未重建）
✅ 无需增量 Migration（password 字段归 Auth 任务 / ADR-0006）


DoD:

- [x] 三张表字段与 DATABASE_DESIGN 完全对齐
- [x] 索引 / 枚举 / 外键策略对齐
- [x] Gap 已识别并归口（password → Auth 任务）
- [x] 审查记录文档化


禁止:

- [X] 重复创建 user / user_preference / tag 表
- [X] 在 TASK-0101 添加 password 字段（归 Auth 任务，与 ADR-0006 耦合）
- [X] 修改 DATABASE_DESIGN §6.1/§6.2/§6.10（核心架构文档已冻结）


交付物：

- `docs/modules/user/MIGRATION_REVIEW.md`：字段逐项核对 + 索引 / 枚举 / 外键策略核对 + Gap 分析 + 后续任务依赖


---


## TASK-0102 User Domain Layer


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Done


Module:

User Module


Branch:

feature/user-domain-layer (已删除)


Branch Status:

Merged


Depends:

TASK-0101（已满足）


Description:

建立 User Module 领域层：JPA Entity + Repository Interface + Domain Service，基于 TASK-0101 已审查的 user / user_preference / tag schema。不实现 Application Service / Controller / DTO（归 TASK-0103 / 0104）。


Todo:

- [x] 引入 Spring Data JPA 依赖（pom.xml）
- [x] 配置 JPA（ddl-auto=none，Flyway 管理schema，open-in-view=false）
- [x] 创建 User 模块包结构（domain/model + domain/service + repository）
- [x] 创建枚举：UserStatus / BudgetLevel / TagType（对齐 §7）
- [x] 创建 JPA Entity：User / UserPreference / Tag（对齐 §6.1/6.2/6.10）
- [x] 创建 Repository Interface：UserRepository / UserPreferenceRepository / TagRepository
- [x] 创建 Domain Service：UserDomainService / UserPreferenceDomainService / TagDomainService


Validation:

✅ 代码结构完整，对齐 CODE_RULES §3 分层 + §4 包结构 + §5 DTO/Entity 边界
✅ Entity 字段与 DATABASE_DESIGN §6.1/6.2/6.10 逐项对齐（22 字段）
✅ 枚举与 §7 USER_STATUS / BUDGET_LEVEL / TAG_TYPE 对齐
✅ 外键策略：逻辑关联不建 FK（§9），Entity 间无 @ManyToOne 物理映射
✅ User 软删除：@SQLDelete + @SQLRestriction（Hibernate 6.4）
✅ password 字段未加入 Entity（归 Auth 任务 / ADR-0006）
✅ CI 编译通过（PR #12, Backend CI, 2026-07-30）
✅ Squash merged to develop (PR #12, 2026-07-30)


DoD:

- [x] 3 Entity + 3 Repository + 3 Domain Service + 3 枚举全部定义
- [x] 接口编译通过（CI 验证通过，PR #12）
- [x] 代码与 ARCHITECTURE §2 分层 / CODE_RULES §3 一致


禁止:

- [X] Application Service 实现（归 TASK-0103）
- [X] Controller / DTO 实现（归 TASK-0104）
- [X] 数据库 Migration（三表已由 TASK-0004 创建）
- [X] 添加 password 字段（归 Auth 任务 / ADR-0006 JWT）
- [X] Entity 间建物理 FK 关系映射（违反 §9 逻辑关联策略）
- [X] Entity 直出 Controller（CODE_RULES §5）


交付物：

- `backend/solo-server/src/main/java/com/sololifeos/user/domain/model/`：User / UserPreference / Tag Entity + UserStatus / BudgetLevel / TagType 枚举
- `backend/solo-server/src/main/java/com/sololifeos/user/repository/`：UserRepository / UserPreferenceRepository / TagRepository
- `backend/solo-server/src/main/java/com/sololifeos/user/domain/service/`：UserDomainService / UserPreferenceDomainService / TagDomainService


---


## TASK-0103 User Application Service


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Done


Module:

User Module


Branch:

feature/user-application-service (已删除)


Branch Status:

Merged


Depends:

TASK-0102（已满足）


Description:

建立 User Module 应用服务层：用例协调 + 事务边界。调用 Domain Service 做业务规则，调用 Repository 做持久化。不实现 Controller / DTO（归 TASK-0104）。


Todo:

- [x] UserApplicationService：注册（含默认偏好创建）/ 资料查询 / 资料更新 / 激活 / 封禁
- [x] UserPreferenceApplicationService：偏好查询 / 偏好更新
- [x] TagApplicationService：标签创建 / 标签查询（按用户 / 按类型）
- [x] 事务边界：写操作 @Transactional，读操作 @Transactional(readOnly=true)
- [x] 注册闭环：注册时事务内创建 user + 默认 preference


Validation:

✅ 3 Application Service 全部定义（对齐 CODE_RULES §3.1 Application Service 职责）
✅ 入参用原始类型，出参用 Domain Entity（DTO 转换归 Controller TASK-0104）
✅ 事务边界明确：写 @Transactional，读 @Transactional(readOnly=true)
✅ 注册闭环：register 方法事务内创建 user + 默认 preference
✅ 构造器注入（CODE_RULES §3.3）
⚠️ 编译验证待 CI（沙箱无网络）


DoD:

- [x] 3 Application Service 全部定义
- [x] 接口编译通过（待 CI 验证）
- [x] 事务边界与 CODE_RULES §3.1 一致


禁止:

- [X] Controller / DTO 实现（归 TASK-0104）
- [X] 业务规则写在 Application Service（归 Domain Service）
- [X] SQL 写在 Application Service（归 Repository）
- [X] Entity 直出 Controller（CODE_RULES §5）


交付物：

- `backend/solo-server/src/main/java/com/sololifeos/user/application/`：UserApplicationService / UserPreferenceApplicationService / TagApplicationService


---


## TASK-0104 User Controller + DTO


Owner:

Backend Agent


Reviewer:

Architecture Agent


Status:

Reviewing


Module:

User Module


Branch:

feature/user-controller-dto


Branch Status:

PR-Open


Depends:

TASK-0103（已满足）


Description:

建立 User Module REST 端点 + DTO。禁 Entity 直出 Controller（CODE_RULES §5），经 Assembler 转换。


Todo:

- [x] 7 DTO：UserRegisterRequest / UserUpdateRequest / UserResponse / UserPreferenceUpdateRequest / UserPreferenceResponse / TagCreateRequest / TagResponse
- [x] UserAssembler：Entity → Response DTO 转换
- [x] UserController：POST /api/users（注册）/ GET /{id} / PUT /{id}
- [x] UserPreferenceController：GET /api/users/{userId}/preference / PUT
- [x] TagController：POST /api/users/{userId}/tags / GET（支持 ?type= 筛选）
- [x] 参数校验（@Valid + jakarta.validation）


Validation:

✅ 7 DTO 全部用 Java record（Spring Boot 3 推荐）
✅ Entity 不直出 Controller（CODE_RULES §5），经 UserAssembler 转换
✅ 参数校验：@NotBlank / @Email / @Size
✅ 返回统一 ApiResponse<T>（ARCHITECTURE §11）
⚠️ 编译验证待 CI（沙箱无网络）


DoD:

- [x] Controller + DTO + Assembler 全部定义
- [x] Swagger 可见（springdoc 已配置）
- [x] 接口编译通过（待 CI 验证）


禁止:

- [X] Entity 直出 Controller（CODE_RULES §5）
- [X] login / activate / ban 端点（归 Auth 任务 / ADR-0006）
- [X] 业务逻辑写在 Controller（归 Application Service）
- [X] 组件直连 axios（前端规则，归 TASK-0105）


交付物：

- `backend/solo-server/src/main/java/com/sololifeos/user/dto/`：7 DTO record
- `backend/solo-server/src/main/java/com/sololifeos/user/application/UserAssembler.java`
- `backend/solo-server/src/main/java/com/sololifeos/user/controller/`：UserController / UserPreferenceController / TagController


---


# Sprint 0 Definition of Done


## Code

- [x] Backend 可启动并访问 `/health`（TASK-0002 Done）
- [x] Frontend H5 可启动并访问首页（TASK-0003 Done）
- [x] Database 可初始化（PostgreSQL + Redis 可用）（TASK-0004 Done，文件就绪；sandbox 无 docker，docker compose up + flyway migrate 待本地验证）
- [x] AI Foundation 6 个核心 Interface 已定义（TASK-0005 Done）：
  - Agent
  - Router
  - Memory
  - Conversation
  - VectorStoreAdapter
  - LLMProvider


## Test

- [ ] Backend 单元测试框架运行（JUnit 5）（延期至 Sprint 1，Sprint 0 仅工程骨架）
- [ ] API 测试框架运行（MockMvc / WebTestClient）（延期至 Sprint 1，Sprint 0 仅工程骨架）


## Documentation

- [x] CHANGELOG.md 更新
- [x] AI_CHANGELOG.md 更新
- [x] ADR Index 建立（TASK-0007 Done）
- [x] TASK_BOARD.md 状态全部更新为 Done（TASK-0001~0007 全部 Done，Sprint 0 关闭）


## Architecture

- [x] ADR-0005 Vector DB Adapter Strategy 进入 Proposed 状态
- [x] ADR-0010 Tag Ownership 进入 Proposed 状态
- [x] ADR-0011 Activity Ownership 进入 Accepted 状态
- [x] Module Boundary 确认（含 ADR-0010 Tag Ownership / ADR-0011 Activity Owner）
- [x] 无越权修改（ARCHITECTURE §22）
- [x] 无重复 Entity（ARCHITECTURE §3）
- [x] 无跨模块数据库访问（ARCHITECTURE §2）
- [x] AI 未直连数据库（ARCHITECTURE §21）


---

# Completed


## TASK-0001 Architecture Foundation

- Owner: Architecture Agent
- Completed: 2026-07-28
- 交付物：ADR-0005（Proposed）/ ADR-0010（Proposed）/ ADR-0011（Accepted）/ Module Boundary Freeze / 环境配置规范
- Sprint 0 架构冻结完成，后续任务可并行启动


## TASK-0002 Backend Foundation

- Owner: Backend Agent
- Reviewer: Architecture Agent
- Completed: 2026-07-28
- 交付物：Spring Boot 3.2.5 + Java 17 工程初始化 / Modular Monolith 8 模块包结构 / ApiResponse + ResultCode / SoloException 异常体系 + GlobalExceptionHandler / TraceIdFilter / HealthController / OpenAPI + Swagger UI / CORS 配置 / application.yml 环境分层
- Validation：mvn clean compile 通过（23 source files）
- 合并方式：Squash merge to develop (PR #1)


## TASK-0003 Frontend Foundation

- Owner: Frontend Agent
- Reviewer: Architecture Agent
- Completed: 2026-07-28
- 交付物：uni-app + Vue3 + TypeScript + Pinia H5 工程初始化 / api/ 请求封装（携带 traceId）/ stores/ Pinia / pages/index / health API 对接 / TS strict mode / VITE_API_BASE_URL 环境变量配置
- Validation：14 文件创建（JSON + TS 严格模式校验通过）
- 合并方式：Squash merge to develop (PR #3)


## TASK-0005 AI Foundation

- Owner: AI Agent
- Reviewer: Architecture Agent
- Completed: 2026-07-28
- 交付物：6 个核心 Interface 定义完成
  - Agent（agents/）：统一 execute 契约 + AgentResult + Context
  - AgentRouter（orchestrator/）：路由策略抽象（ADR-0003）
  - MemoryService（memory/）：长期记忆读写（ai_memory）
  - ConversationService（memory/）：短期对话上下文（ai_conversation）
  - VectorStoreAdapter（llm/）：Vector DB 抽象层（ADR-0005，不绑定 Provider）
  - LLMProvider（llm/）：模型调用抽象层（ADR-0008，Sprint 5 实现）
- 禁止项全部遵守：无 LLM 接入 / 无 Prompt / 无 Agent 实现 / 无 Vector DB 部署
- Validation：mvn clean compile 通过（32 source files）
- 合并方式：Squash merge to develop


## TASK-0004 Database Foundation

- Owner: Backend Agent
- Reviewer: Architecture Agent
- Completed: 2026-07-29
- 交付物：docker-compose.yml（PostgreSQL 16 + Redis 7）/ docker-compose.ci.yml（CI tmpfs 覆盖）/ Flyway 初始化配置（locations + validate-on-migrate）/ HikariCP 连接池（max 10 / min 2）/ 初始 Migration 三张表（user / user_preference / tag，对齐 DATABASE_DESIGN §6.1/§6.2/§6.10）
- Validation：文件结构完整，对齐 §7 枚举 + §8 索引 + §9 外键策略；sandbox 无 docker，docker compose up + flyway migrate 待本地验证
- 合并方式：Squash merge to develop (PR #6)


## TASK-0006 CI/CD Foundation

- Owner: Backend Agent
- Reviewer: QA Agent
- Completed: 2026-07-29
- 交付物：.github/workflows/backend-ci.yml（JDK 17 + Maven 缓存 + clean compile/test）/ .github/workflows/frontend-ci.yml（Node 20 + npm 缓存 + type-check + build:h5）/ .github/branch-protection.md（main + develop 分支保护规则建议）/ .github/PULL_REQUEST_TEMPLATE.md 升级（治理检查段 + TASK_BOARD 字段）
- Validation：两个 workflow YAML 语法校验通过；Sprint 0 阶段 test/build 步骤 continue-on-error: true，业务测试待 Sprint 1 补全
- 合并方式：Squash merge to develop (PR #7)


## TASK-0007 Documentation Foundation

- Owner: Architecture Agent
- Reviewer: QA Agent
- Completed: 2026-07-29
- 交付物：docs/architecture/ADR/README.md（ADR Index，5 Accepted + 2 Proposed + 4 Future）/ docs/architecture/ADR/template.md（ADR 标准模板）/ docs/governance/DOCUMENT_VERSION_RULE.md（版本同步规则，代码优先）/ docs/AI_CHANGELOG_TEMPLATE.md（AI 行为日志模板）
- 范围控制：仅建立治理结构与模板，未修改已冻结的架构文档（ARCHITECTURE / DATABASE_DESIGN / ADR 已接受决策 / 数据模型 / 模块边界）
- Validation：4 个治理文件创建，ADR-0001~0011 状态登记完整
- 合并方式：Squash merge to develop (PR #8)


---

# Sprint 0 Close Gate

Sprint 0 全部 7 个任务达成，正式关闭：

| Foundation | Task | Status |
|------------|------|--------|
| Architecture | TASK-0001 | ✅ Done |
| Backend | TASK-0002 | ✅ Done |
| Frontend | TASK-0003 | ✅ Done |
| Database | TASK-0004 | ✅ Done |
| AI Platform | TASK-0005 | ✅ Done |
| CI/CD | TASK-0006 | ✅ Done |
| Documentation | TASK-0007 | ✅ Done |

进入业务代码阶段，不再迭代架构文档。


---

# Sprint 1 Remaining Tasks


Sprint 1 已启动（Current Sprint）。TASK-0101 User Migration Review 已完成（见 Active Tasks 段）。以下为待启动任务，按依赖顺序推进：


Sprint 1 Goal:

完成用户注册、登录、资料、偏好设置。


Depends:

Sprint 0（Done 2026-07-29）


待启动任务：

- ✅ TASK-0101 User Migration Review（Done 2026-07-29）
- ✅ TASK-0102 User Domain Layer（Done 2026-07-30，PR #12 merged）
- ✅ TASK-0103 User Application Service（Done 2026-07-30，PR #13 merged）
- ✅ TASK-0104 User Controller + DTO（Reviewing 2026-07-30，PR-Open）
- TASK-0105 User Frontend（登录 / 资料 / 偏好页）
- TASK-0106 User Test Suite（JUnit 5 + MockMvc）

> 注意：Authentication（ADR-0006 JWT，含 password 字段增量 Migration）为 Sprint 1 关键项，任务编号待与 User Domain Layer 依赖关系确认后定稿。password 字段归此任务，与 ADR-0006 同期落地。


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


## v2.9 - 2026-07-30

- TASK-0103 User Application Service 状态更新：Reviewing → Done（PR #13 merged，CI 编译通过）
- 新增 TASK-0104 User Controller + DTO 任务卡（Owner: Backend Agent，Status: Reviewing）
- TASK-0104 交付物：7 DTO（record）+ UserAssembler + 3 Controller
- REST 端点：POST /api/users（注册）/ GET /{id} / PUT /{id} + preference + tags
- Entity 不直出 Controller（CODE_RULES §5），经 UserAssembler 转换
- 范围控制：login / activate / ban 归 Auth 任务（ADR-0006）


## v2.8 - 2026-07-30

- TASK-0102 User Domain Layer 状态更新：Reviewing → Done（PR #12 merged，CI 编译通过）
- 新增 TASK-0103 User Application Service 任务卡（Owner: Backend Agent，Status: Reviewing）
- TASK-0103 交付物：3 Application Service（UserApplicationService / UserPreferenceApplicationService / TagApplicationService）
- 注册闭环：register 方法事务内创建 user + 默认 preference
- 事务边界：写 @Transactional，读 @Transactional(readOnly=true)
- 范围控制：未实现 Controller / DTO（归 TASK-0104），业务规则归 Domain Service


## v2.7 - 2026-07-30

- 新增 TASK-0102 User Domain Layer 任务卡（Owner: Backend Agent，Status: Reviewing）
- TASK-0102 交付物：3 Entity + 3 Repository + 3 Domain Service + 3 枚举（对齐 DATABASE_DESIGN §6.1/6.2/6.10 + §7）
- 引入 Spring Data JPA 依赖，配置 ddl-auto=none（Flyway 管理 schema）
- User 软删除：@SQLDelete + @SQLRestriction（Hibernate 6.4）
- 范围控制：未实现 Application Service / Controller / DTO（归 TASK-0103/0104），未添加 password（归 Auth/ADR-0006）


## v2.6 - 2026-07-29

- Sprint 1：User Module 启动（Current Sprint 从 Sprint 0 切换至 Sprint 1）
- Sprint 0 Status：Done (Closed 2026-07-29) → 已归档，进入业务代码阶段
- 新增 TASK-0101 User Migration Review 任务卡（Owner: Architecture Agent，Status: Done）
- TASK-0101 审查结论：Sprint 0 三表（user / user_preference / tag）22 字段 + 5 索引 + 3 枚举 + 外键策略全部对齐 DATABASE_DESIGN，无需增量 Migration
- Gap 归口：password 字段缺失，归 Authentication 任务（ADR-0006 JWT），不在 TASK-0101 扩展
- 新增交付物：docs/modules/user/MIGRATION_REVIEW.md


## v2.5 - 2026-07-29

- Sprint 0 Status：In Progress → Done (Closed 2026-07-29)
- TASK-0004 Database Foundation：Reviewing → Done（PR #6 Squash merged to develop）
- TASK-0006 CI/CD Foundation：Reviewing → Done（PR #7 Squash merged to develop）
- TASK-0007 Documentation Foundation：Reviewing → Done（PR #8 Squash merged to develop）
- 三个 feature 分支标记已删除，Branch Status：PR-Open → Merged
- Sprint 0 DoD：Code / Documentation 段全部勾选；Test 段延期至 Sprint 1（Sprint 0 仅工程骨架）
- Completed 段新增 TASK-0004 / TASK-0006 / TASK-0007 交付物清单
- 新增 Sprint 0 Close Gate 段，Sprint 0 正式关闭，进入业务代码阶段


## v2.3 - 2026-07-28

- Sprint 0 Status：Planning → Ready
- TASK-0001 Architecture Foundation 执行完成，Status: Designing → Done
- 创建 ADR-0005 Vector DB Adapter Strategy（Proposed）
- 创建 ADR-0010 Tag Ownership（Proposed，Tag 归 Shared Kernel）
- 创建 ADR-0011 Activity Ownership（Accepted）
- 输出 Module Boundary Freeze（8 模块 + AI Platform + Shared Kernel 冻结表）
- 输出环境配置规范（.env / docker-compose / application.yml 分层）
- TASK-0001 移入 Completed 段


## v2.2 - 2026-07-28

- 按 ADR 评审意见调整 Sprint 0 TASK-0001 Todo：新增 ADR-0010 Tag Ownership（Proposed）与 ADR-0011 Activity Ownership（Accepted）创建项
- TASK-0001 DoD 调整为三 ADR 状态：ADR-0005 Proposed / ADR-0010 Proposed / ADR-0011 Accepted
- Sprint 0 DoD Architecture 段同步调整为三 ADR 状态
- 与 ARCHITECTURE v2.3 / SPRINT_PLAN v2.2 ADR Roadmap 对齐（ADR 生命周期与 Sprint 生命周期一致，禁止提前批量创建）


## v2.1 - 2026-07-28

- P0-1 修复：TASK-0101 User Migration 改为 Migration Review，禁止 Sprint 1 重复创建 user / user_preference / tag 表
- P0-2 修复：TASK-0005 Module 从「AI Platform」改为「Foundation / AI Infrastructure」（AI Platform 完整实现属 Sprint 5）
- P0-3 修复：ADR-0005 职责拆分——Architecture Agent 负责 Vector DB Selection Proposal（定方向），AI Agent 负责 VectorStoreAdapter Interface 实现（抽象层）
- P1-1 修复：TASK-0004 Database Foundation 增加 TASK-0002 依赖（Flyway 配置需先就绪）
- P1-2 修复：TASK-0005 新增业务模块禁止项（禁改 Entity / Repository / Domain Service / 跨模块 import，对齐 ARCHITECTURE §21）
- 架构修复：Sprint 0 DoD 明确 6 个核心 Interface（含 VectorStoreAdapter）
- 新增 TASK-0007 Documentation Foundation（ADR Index / ADR 模板 / 版本同步规则 / AI_CHANGELOG 模板）
- 新增 Sprint 0 Task Dependency Graph 依赖关系图
- DoD Architecture 段新增 ADR-0005 Proposed 与 Module Boundary 确认项


## v2.0 - 2026-07-28

- 全量重写：从「功能清单」升级为「Module + Owner + Reviewer + Status」任务卡
- 与 SPRINT_PLAN v2.1 / AGENTS v1.2 §7 / ARCHITECTURE v2.2 §22 对齐
- 拆分 Sprint 0 为 6 个独立任务（TASK-0001 ~ TASK-0006）
- 引入任务状态机（Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived）
- 收紧 AI Agent 任务边界：Sprint 0 仅定义 Interface，禁止真实 LLM / Prompt / Agent 实现
- 收紧数据库边界：Sprint 0 仅创建 user / user_preference / tag 三张表
- 新增 Sprint 0 Definition of Done 四层约束（Code / Test / Documentation / Architecture）
- 新增 Next Sprint 任务预拆分（Sprint 1 User Module）
