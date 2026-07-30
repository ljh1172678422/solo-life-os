# Changelog


记录 Solo Life OS 所有重要变更。


格式参考 [Keep a Changelog](https://keepachangelog.com/)。

版本号遵循 [Semantic Versioning](https://semver.org/)。


---

## [Unreleased]


### Added

- TASK-0106 User Test Suite：新增 User Module 单元测试套件（JUnit 5 + Mockito + MockMvc）
  - 5 个测试类，40 个测试用例全部通过（0 failures / 0 errors / 0 skipped）
  - UserDomainServiceTest（11）：register / activate / ban / updateProfile 业务规则
  - AuthServiceTest（7）：login 成功（邮箱/手机）/ 账号不存在 / 密码错误 / 用户封禁
  - JwtServiceTest（7）：token 签发 / 解析校验 / 过期 / 篡改
  - UserControllerTest（10）：MockMvc 注册 / 查询 / 更新 + 参数校验 + 业务异常
  - AuthControllerTest（5）：MockMvc 登录 + 参数校验 + 认证异常
  - Controller 测试使用 standalone MockMvc，隔离 Spring Security 自动配置
  - mock-maker-subclass 配置：绕开 inline mock maker 在 Java 25 上的字节码限制
  - Sprint 0 DoD Test 段两项延期项完成（单元测试 + API 测试框架运行）
- TASK-0105 User Frontend：新增 User Module 前端页面，完成注册→登录→设置偏好闭环
  - api 层重构：request.ts 支持 Authorization header 注入 + ApiError 异常体系 + 401 自动跳登录
  - api/types.ts：UserProfile / UserPreference / Tag / LoginRequest 等 TS 类型（禁 any）
  - api/user.ts：auth（login）+ user（register/get/update）+ preference（get/update）+ tag（create/list）API 封装
  - stores/user.ts：token + userInfo 持久化（localStorage），setAuth / setUser / clearAuth
  - 4 个页面：login（登录）/ register（注册）/ profile（资料查看编辑）/ preference（偏好设置）
  - pages/index 登录态守卫：已登录跳资料页，未登录跳登录页
  - 修正 App.vue 移除 vue-router 依赖，改用 uni-app 原生路由 API（uni.reLaunch / navigateTo / navigateBack）
  - 新增 @dcloudio/types devDependency：声明 uni 全局类型
  - vue-tsc --noEmit passed（0 errors，TS strict mode）
- TASK-0107 Authentication（ADR-0006 JWT）：新增 JWT 认证闭环
  - ADR-0006 JWT Authentication（Accepted）：HS256 + BCrypt + 自定义 JwtAuthFilter，不引入完整 Spring Security
  - 数据库 Migration：user 表增加 password 字段（varchar(100)，BCrypt 哈希，nullable）
  - common/security：JwtProperties / JwtService（签发/验证）/ JwtAuthFilter（请求拦截）/ UserContext / PasswordEncoderConfig
  - user/application/AuthService：登录用例（账号查询 + BCrypt 校验 + JWT 签发）
  - user/controller/AuthController：POST /api/auth/login（返回 JWT token）
  - user/dto：LoginRequest（account + password）/ LoginResponse（token + userId + nickname）
  - 修改：User Entity 加 password / UserRegisterRequest 加 password / UserDomainService.register + UserApplicationService.register 支持 BCrypt 哈希
  - 安全规则：明文密码不入库 / 不记日志；登录失败 message 统一防账号枚举
- TASK-0104 User Controller + DTO：新增 User Module REST 端点
  - 7 DTO（Java record）：UserRegisterRequest / UserUpdateRequest / UserResponse / UserPreferenceUpdateRequest / UserPreferenceResponse / TagCreateRequest / TagResponse
  - UserAssembler：Entity → Response DTO 转换
  - UserController：POST /api/users（注册）/ GET /{id} / PUT /{id}
  - UserPreferenceController：GET/PUT /api/users/{userId}/preference
  - TagController：POST/GET /api/users/{userId}/tags（支持 ?type= 筛选）
  - 参数校验（@Valid + jakarta.validation）
- TASK-0103 User Application Service：新增 User Module 应用服务层
  - UserApplicationService：注册（含默认偏好创建）/ 资料查询 / 资料更新 / 激活 / 封禁
  - UserPreferenceApplicationService：偏好查询 / 偏好更新
  - TagApplicationService：标签创建 / 标签查询
  - 事务边界：写 @Transactional，读 @Transactional(readOnly=true)
- TASK-0102 User Domain Layer：新增 User Module 领域层
  - JPA Entity：User / UserPreference / Tag（对齐 DATABASE_DESIGN §6.1/6.2/6.10）
  - 枚举：UserStatus / BudgetLevel / TagType（对齐 §7）
  - Repository Interface：UserRepository / UserPreferenceRepository / TagRepository
  - Domain Service：UserDomainService / UserPreferenceDomainService / TagDomainService
  - 引入 Spring Data JPA 依赖，配置 ddl-auto=none（Flyway 管理 schema）
  - User 软删除支持（@SQLDelete + @SQLRestriction，Hibernate 6.4）

- 初始化 AI 研发管理包目录结构
- 新增 `.ai/AGENTS.md` Agent 工作规范
- 新增 `.ai/CODE_RULES.md` 编码规范
- 新增 `docs/PROJECT_CONTEXT.md` 项目上下文
- 新增 `docs/ARCHITECTURE.md` 系统架构
- 新增 `docs/DATABASE_DESIGN.md` 数据库设计
- 新增 `docs/SPRINT_PLAN.md` Sprint 规划
- 新增 `docs/TASK_BOARD.md` 任务看板


### Changed

- 升级 `docs/PROJECT_CONTEXT.md` v1.0 → v1.1
  - 新增「目标用户」与「用户场景」章节，约束推荐逻辑
  - 新增「用户核心痛点」章节，明确产品价值定位
  - 新增「产品边界」章节，防止功能膨胀
  - 新增「核心竞争力」章节（Personal Life Memory / AI 主动陪伴 / Life Story），指导架构重点
  - 新增「产品发展阶段」Phase 0–4，指导开发优先级
  - 新增「数据资产战略」章节，指导数据库设计
  - 新增「AI 输出原则」（可解释 / 可编辑 / 可拒绝），指导 Agent 行为
  - 新增「隐私原则」与「AI 研发原则」，防止 AI 随意编码
- 升级 `docs/PROJECT_CONTEXT.md` v1.1 → v1.2
  - 重构 §11 AI 架构为 Orchestrator 模式（路由层 + 横向可扩展 Agent），新增 Assistant / Memory Service，禁止 Agent 网状依赖
  - 新增 §16 项目成功标准（Daily Value / AI 价值 / 数据资产 / 情感价值指标）
  - 新增 §17 架构演进原则（初期 Modular Monolith、后期服务拆分条件、核心模块边界与领域接口）
  - 新增 §18 AI 代码生成原则（编码前必读文档清单、禁止事项、新增功能流程）
  - 新增 §19 AI Agent 角色体系（Product / Architecture / Backend / Frontend / AI / QA 六类 Agent，共享 PROJECT_CONTEXT）
  - 新增 §20 文档版本管理（核心文档版本规则与修改记录要求）


### Added (Git 协作基础设施)


- 新建 `develop` 分支作为研发集成分支，`main` 仅承载稳定版本
- 新增 `README.md` 项目入口（文档导航 / 技术栈 / 分支策略摘要）
- 新增 `.gitignore`（Java / Node / uni-app / IDE / OS / 凭据）
- 新增 `.github/PULL_REQUEST_TEMPLATE.md`（变更类型 / 架构影响 / 文档更新 / 测试情况）
- 新增 `docs/AI_CHANGELOG.md`，记录 AI Agent 行为日志，与产品 CHANGELOG 区分
- 升级 `docs/AGENTS.md`：纳入 §3 Agent 权限分级 / §5 Git 协作规范 / §5.2 AI 分支规则 / §5.4 PR 流程 / §7 完成任务后必更清单


### Changed (目录结构调整)


- 迁移 `.ai/AGENTS.md` → `docs/AGENTS.md`（升级版）
- 迁移 `.ai/CODE_RULES.md` → `docs/CODE_RULES.md`（补充 commit scope 规范）
- 删除空的 `.ai/` 目录


### Changed (AGENTS.md v1.0 → v1.1)


- 升级 `docs/AGENTS.md` v1.0 → v1.1
  - 新增 §7 Task Ownership（任务卡片格式 / Primary Owner / 领取前检查）
  - 新增 §8 Architecture Change Process（架构变更必须先评估后开发）
  - 新增 §9 AI 提交前检查（Context / Architecture / Database / Code / Documentation / Git 六类自检）
  - 新增 §10 Agent Handoff Protocol（Changed / API / Database / Dependency / Next Agent 交接格式）
  - §3 权限调整：Architecture Agent 改为可写 `database/design/`，禁止 `database/migrations/`（迁移归 Backend Agent）
  - §6 禁止事项补充：多 Agent 同改同模块、先写代码再补架构
  - §12 完成任务后必更清单指向 `docs/CHANGELOG.md`（原根目录 CHANGELOG 已迁移）


### Changed (CHANGELOG 位置调整)


- `CHANGELOG.md` → `docs/CHANGELOG.md`（使用 `git mv` 保留历史，便于根目录整洁）
- 新增根目录 `AGENTS.md` 作为 AI Coding Agent 入口，指向 `docs/AGENTS.md`
- 更新 `README.md` 文档导航表，同步链接调整


### Changed (AGENTS.md v1.1 → v1.2)


- 升级 `docs/AGENTS.md` v1.1 → v1.2
  - §5.1 分支策略新增 `hotfix/*`（生产紧急修复通道）与 `docs/*`（文档独立生命周期）分支类型
  - §7 Task Ownership 新增任务生命周期状态机（Backlog→Assigned→Designing→Developing→Reviewing→Testing→Done→Archived）
  - 新增 §11 Prompt 文件管理规则（受控变更，禁止无记录修改 Prompt，防漂移）
  - 新增 §12 Repository Structure（仓库目录地图，禁止擅建根目录）
  - 原 §11/§12 顺延为 §13 文档版本管理 / §14 完成任务后必更
- 升级 `docs/AI_CHANGELOG.md`，固定条目格式（Agent/Task/Action/Reason/Impact/Reviewer）


### Changed (ARCHITECTURE.md v1.0 → v2.0)


- 全量升级 `docs/ARCHITECTURE.md`，从「系统拓扑图」升级为「研发约束文档」
  - 新增 §1 Architecture Principles（DDD 轻量版 + Modular Monolith + AI Native）
  - 新增 §2 Layer Architecture（Controller/Application/Domain/Repository 分层与禁止规则）
  - 新增 §3 Shared Domain（8 个共享核心 Entity，禁止重复定义）
  - 新增 §4 Module Dependencies（模块依赖图与单向依赖规则）
  - 新增 §5 升级版总体架构图（Client → Spring Boot → AI Platform → 持久化四层）
  - 新增 §7 AI Platform 完整链路（Memory → Context → Router → Agent → LLM Provider 抽象层）
  - 新增 §9 Event Flow（事件流解耦 + 典型事件表）
  - 新增 §10 Persistence（PostgreSQL + Redis + Vector DB + OSS 四存储）
  - 新增 §11 API Boundary（前端禁直调 AI、统一返回格式）
  - 新增 §12 Repository Structure（仓库目录地图，与 AGENTS.md §12 对齐）
  - 新增 §13 Evolution Roadmap（Phase 0–4 演进路线与触发条件）


### Changed (ARCHITECTURE.md v2.0 → v2.1)


- 升级 `docs/ARCHITECTURE.md` v2.0 → v2.1，纳入企业级研发治理
  - §5 总体架构图：App → Spring Boot Modular Monolith，统一使用 Module 命名
  - §6 服务设计 → 模块设计，全文 Service → Module（与 Phase 0 一致）
  - 新增 §14 ADR（Architecture Decision Record）机制
  - 新增 §15 NFR（性能 P95 / 可用性 / 可扩展性 / 隐私）
  - 新增 §16 Observability（Logging / Metrics / Tracing / Audit）
  - 新增 §17 Security Boundary（认证 / 授权 / 限流 / 加密）
  - 新增 §18 Integration Boundary（外部集成通过 Adapter 层）
  - 新增 §19 Package Convention（后端包结构固定）
  - 新增 §20 Error Handling（统一异常体系与错误码）
  - 新增 §21 AI Boundary（AI 永远不能直连数据库，必须经 Domain API）
  - 新增 §22 Data Ownership（每个核心数据对象唯一 Owner 模块）
- 新增 `docs/architecture/ADR/` 目录与 4 份初始 ADR：
  - ADR-0001 采用 Modular Monolith
  - ADR-0002 选择 PostgreSQL 作为主数据库
  - ADR-0003 AI Agent 统一经 Router 路由
  - ADR-0004 MVP 阶段不使用微服务


### Changed (DATABASE_DESIGN.md v1.0 → v2.0)


- 全量升级 `docs/DATABASE_DESIGN.md`，从「领域模型草稿」升级为「开发基线」
  - 新增 §1 Design Principles + §2 Naming Convention
  - 新增 §3 Shared Entities（与 ARCHITECTURE §3 对齐）
  - 新增 §4 Entity Ownership（每张表唯一 Owner 模块）
  - 新增 §5 ER Diagram（实体关系图）
  - 重写 §6 Table Design：每张表含完整字段说明（类型 / Nullable / 默认值 / 描述）
  - 新增 §7 Enum Definition（12 类枚举显式定义，禁止自由字符串）
  - 新增 §8 Index Strategy（15 个索引，对齐 §15 NFR 性能指标）
  - 新增 §9 Constraint Strategy（逻辑关联不建 FK + 唯一约束 + 非空约束）
  - 新增 §10 Migration Rule（命名规范 + 幂等 + 破坏性变更需 ADR）
  - 新增 §11 Version History + §12 与其他文档对齐
  - 修正 Activity.location → location_id（关联 Location Entity）
  - 修正 Favorite 增加 UNIQUE(user_id, target_type, target_id)
  - 扩展 ai_memory：新增 memory_type / source / summary / embedding_id / visibility 5 字段


### Changed (CODE_RULES.md v1.0 → v2.0)


- 全量升级 `docs/CODE_RULES.md`，使规范从「代码风格约束」升级为「AI 生成代码约束」
  - 新增 §1 General Principles
  - 重写 §2 Frontend：禁止 any / as any，未知用 unknown；禁止组件直连 axios，必须经 api/ 封装
  - 重写 §3 Backend：补全 Application Service / Domain Service / Repository Interface / Infrastructure 五层
  - 新增 §4 Package Convention（对齐 ARCHITECTURE §19）
  - 新增 §5 DTO/Entity/VO Rules：禁止 Entity 直出 Controller
  - 新增 §6 Exception Handling（对齐 ARCHITECTURE §20，禁止 throw new Exception）
  - 新增 §7 Logging Rules（对齐 ARCHITECTURE §16，必含 traceId，禁 System.out.println）
  - 重写 §8 Database Rules（对齐 DATABASE_DESIGN v2.0）
  - 重写 §9 API Rules：返回格式增加 traceId 字段
  - 新增 §10 Testing Rules
  - 新增 §11 AI Generated Code Rules：与 AGENTS / ARCHITECTURE 联动的 10 条约束
  - 重写 §12 Git Convention：分支类型对齐 AGENTS.md §5.1（feature/bugfix/hotfix/docs/refactor）
  - 新增 §13 Version History + §14 Alignment 对齐表


### Changed (SPRINT_PLAN.md v1.0 → v2.0)


- 全量升级 `docs/SPRINT_PLAN.md`，从「功能清单」升级为「可执行 Sprint 计划」
  - Sprint 按 Module 组织（不按页面），统一术语 Module（与 ARCHITECTURE §5 一致）
  - 每个 Sprint 增加：Sprint Goal / Deliverables / Agents / Depends / Risk / DoD 六段式结构
  - Sprint 5 改名 AI Personal Agent → AI Platform（含 Memory / Router / 5 个 Agent）
  - 新增 §2 Sprint Roadmap（含 M1/M2/M3 Milestone 标注）
  - 新增 §12 Milestones（与 PROJECT_CONTEXT §9 Phase 对应）
  - 新增 §13 Dependencies（依赖关系图）
  - 新增 §14 Definition of Done（代码 / 测试 / 文档 / 架构 四层 DoD）
  - 新增 §15 Sprint Lifecycle（与 AGENTS §7 一致）+ Risk 管理规则
  - 新增 §16 Version History + §17 Alignment 对齐表
  - 识别 5 个待写 ADR：ADR-0005 Vector DB / ADR-0006 JWT / ADR-0007 地图 / ADR-0008 LLM / ADR-0009 支付


### Changed (领域所有权冲突修复)


按评审 P0/P1 修改项，协同调整三个根文档解决领域 Owner 冲突：


- `docs/DATABASE_DESIGN.md` v2.0 → v2.1
  - Activity Owner 从 Today/Explore 改为 Today（解决唯一 Owner 冲突）
  - 新增 §6.11 community_event 表（独立领域实体，不复用 activity）
  - 新增 §6.12 registration 表（含 UNIQUE(event_id, user_id) 防重复报名）
  - 新增 §6.13 ai_conversation 表（短期对话上下文，与 ai_memory 长期记忆互补）
  - 新增 5 类枚举：COMMUNITY_EVENT_STATUS / REGISTRATION_STATUS / AGENT_TYPE / CONVERSATION_ROLE
  - 新增 7 个索引 + registration 唯一约束
- `docs/ARCHITECTURE.md` v2.1 → v2.2
  - §22 Data Ownership：Activity Owner 调整 + 新增 Conversation / CommunityEvent / Registration
  - §9 Event Flow：activity.completed 发布者从 Today/Explore 改为 Today
  - §14 ADR 列表新增 ADR-0010 Tag Ownership / ADR-0011 Activity Owner 归 Today
- `docs/SPRINT_PLAN.md` v2.0 → v2.1
  - Sprint 0：Vector DB 本地环境 → Adapter Interface（实例延后至 Sprint 5）
  - Sprint 3 Explore：Deliverables 移除 activity（Owner 是 Today，跨模块经 Domain API）
  - Sprint 5：新增 ai_conversation Migration + Conversation Layer
  - Sprint 7 Community：删除「复用 activity」，改用 community_event 独立领域实体
  - 新增 §16 ADR Roadmap（ADR-0001~0011 完整清单 + 状态跟踪）


### Changed (TASK_BOARD.md v1.0 → v2.0)


- 全量升级 `docs/TASK_BOARD.md`，从「功能清单格式」升级为「Module + Owner + Reviewer + Status」任务卡
  - 与 SPRINT_PLAN v2.1 / AGENTS v1.2 §7 Task Ownership / ARCHITECTURE v2.2 §22 完全对齐
  - 拆分 Sprint 0 为 6 个独立任务（TASK-0001 ~ TASK-0006）：
    - TASK-0001 Architecture Foundation（Owner: Architecture Agent）
    - TASK-0002 Backend Foundation（Owner: Backend Agent）
    - TASK-0003 Frontend Foundation（Owner: Frontend Agent）
    - TASK-0004 Database Foundation（Owner: Backend Agent）
    - TASK-0005 AI Platform Foundation（Owner: AI Agent）
    - TASK-0006 CI/CD Foundation（Owner: Backend Agent）
  - 引入任务状态机（Backlog → Assigned → Designing → Developing → Reviewing → Testing → Done → Archived）
  - 每个任务卡含：Owner / Reviewer / Status / Module / Branch / Description / Todo / DoD / 禁止项
  - 收紧 AI Agent 任务边界：Sprint 0 仅定义 Interface，禁止真实 LLM / Prompt / Agent 实现
  - 收紧数据库边界：Sprint 0 仅创建 user / user_preference / tag 三张表
  - 新增 Sprint 0 Definition of Done 四层约束（Code / Test / Documentation / Architecture）
  - 新增 Next Sprint 任务预拆分（Sprint 1 User Module TASK-0101~0106）
  - 新增 Task Status Legend 状态说明表


### Changed (TASK_BOARD.md v2.0 → v2.1)


- 按评审意见修复 5 项问题并新增 1 项任务：
  - P0-1 修复：TASK-0101 User Migration 改为 Migration Review，禁止 Sprint 1 重复创建 user / user_preference / tag 表
  - P0-2 修复：TASK-0005 Module 从「AI Platform」改为「Foundation / AI Infrastructure」（AI Platform 完整实现属 Sprint 5）
  - P0-3 修复：ADR-0005 职责拆分——Architecture Agent 负责 Vector DB Selection Proposal（定方向），AI Agent 负责 VectorStoreAdapter Interface 实现（抽象层）
  - P1-1 修复：TASK-0004 Database Foundation 增加 TASK-0002 依赖（Flyway 配置需先就绪）
  - P1-2 修复：TASK-0005 新增业务模块禁止项（禁改 Entity / Repository / Domain Service / 跨模块 import，对齐 ARCHITECTURE §21）
  - 架构修复：Sprint 0 DoD 明确 6 个核心 Interface（含 VectorStoreAdapter）
  - 新增 TASK-0007 Documentation Foundation（ADR Index / ADR 模板 / 版本同步规则 / AI_CHANGELOG 模板）
  - 新增 Sprint 0 Task Dependency Graph 依赖关系图
  - DoD Architecture 段新增 ADR-0005 Proposed 与 Module Boundary 确认项


### Changed (ADR Roadmap 调整)


- 按 ADR 评审意见，将 ADR 生命周期与 Sprint 生命周期对齐：
  - `docs/ARCHITECTURE.md` v2.2 → v2.3
    - §14 ADR 清单重写：ADR-0006~0009 标注对应 Sprint 与 Pending 状态，禁止提前批量创建
    - ADR-0010 Tag Ownership 提前到 Sprint 0（Proposed，领域边界争议需提前决策）
    - ADR-0011 Activity Ownership 提前到 Sprint 0（Accepted，已是架构事实）
    - ADR-0007 改为 Map Provider Adapter Pattern（避免高德 / 腾讯硬绑定）
    - ADR-0008 改为 LLM Provider Strategy（抽象层策略，不锁定具体 Provider）
    - ADR-0009 标注 Community MVP 免费活动可延期
    - 新增 §23 Version History
    - ADR-0002 Impact 补充存储分层禁止项（禁 Redis 作主数据源 / 禁 Vector DB 保存业务事实）
    - ADR-0003 Decision 补充 Agent 不持有业务状态、不直接持久化业务数据约束
  - `docs/SPRINT_PLAN.md` v2.1 → v2.2
    - §16 ADR Roadmap 重写：增加备注列、状态列，明确 ADR 创建时机规则
  - `docs/TASK_BOARD.md` v2.1 → v2.2
    - TASK-0001 Todo 新增 ADR-0010 / ADR-0011 创建项
    - TASK-0001 DoD 调整为三 ADR 状态：ADR-0005 Proposed / ADR-0010 Proposed / ADR-0011 Accepted
    - Sprint 0 DoD Architecture 段同步调整为三 ADR 状态


### Added (TASK-0002 Backend Foundation)


- Spring Boot 3.2.5 + Java 17 工程初始化（`backend/solo-server/`）
- Modular Monolith 包结构建立（ARCHITECTURE §19）：
  - common/（response / exception / health / config）
  - user / today / explore / mood / growth / community / story / ai（8 模块 package-info）
- 统一 Response Wrapper（`ApiResponse<T>` + `ResultCode`）
- 异常体系（ARCHITECTURE §20）：SoloException 基类 + 5 个子类 + GlobalExceptionHandler
- TraceId 透传 Filter（ARCHITECTURE §16）
- GET /health 端点
- OpenAPI / Swagger UI 配置
- CORS 开发环境配置
- application.yml + application-dev.yml 环境分层
- .env.example 环境变量模板
- 集成依赖：Spring Web / Validation / Data Redis / Actuator / Flyway 10.10 / PostgreSQL / springdoc-openapi
- mvn clean compile 验证通过（23 source files）


### Added (TASK-0005 AI Foundation)


- AI Foundation 6 个核心 Interface 定义完成（`backend/solo-server/src/main/java/com/sololifeos/ai/`）
  - Agent（agents/）：统一 execute 契约 + AgentResult + Context
  - AgentRouter（orchestrator/）：路由策略抽象（ADR-0003）
  - MemoryService（memory/）：长期记忆读写（ai_memory）
  - ConversationService（memory/）：短期对话上下文（ai_conversation）
  - VectorStoreAdapter（llm/）：Vector DB 抽象层（ADR-0005，不绑定 Provider）
  - LLMProvider（llm/）：模型调用抽象层（ADR-0008，Sprint 5 实现）
- 禁止项全部遵守：无 LLM 接入 / 无 Prompt / 无 Agent 实现 / 无 Vector DB 部署
- mvn clean compile 验证通过（32 source files）


### Added (TASK-0003 Frontend Foundation)


- uni-app + Vue3 + TypeScript + Pinia H5 工程初始化（`apps/h5/`）
  - src/api/：request 通用封装 + health API
  - src/stores/：Pinia app store
  - src/pages/：index 首页
  - TS strict mode + `@/*` 路径别名
  - VITE_API_BASE_URL 环境变量配置
- 配置：package.json / tsconfig.json / vite.config.ts / pages.json / manifest.json


### Added (Git Branch Governance)


- `docs/AGENTS.md` v1.2 → v1.3：新增 §15 Git Branch Governance
  - §15.1 Develop Branch Protection（硬约束：禁止在 develop/main 上提交代码）
  - §15.2 Task Start Checklist（强制：创建 feature 分支后才能 Developing）
  - §15.3 Task Commit Workflow（feature 分支提交 → PR → 审核 → 合并）
  - §15.4 Branch Status 字段（Created / Pushed / PR-Open / Merged）
  - §15.5 AI Agent 自检规则（git 命令前检查当前分支）
  - §15.6 PR 合并条件（DoD + 编译通过 + Reviewer 审核）
- `docs/TASK_BOARD.md`：TASK-0002 新增 Branch Status 字段


### Added (TASK-0001 Architecture Foundation 完成)


- Sprint 0 Status：Planning → Ready
- TASK-0001 Architecture Foundation 执行完成（Architecture Freeze Gate）
- 新增 ADR 文件：
  - `docs/architecture/ADR/ADR-0005-vector-db-adapter-strategy.md`（Proposed）
  - `docs/architecture/ADR/ADR-0010-tag-ownership.md`（Proposed，Tag 归 Shared Kernel）
  - `docs/architecture/ADR/ADR-0011-activity-ownership.md`（Accepted）
- 输出 Module Boundary Freeze（8 模块 + AI Platform + Shared Kernel 冻结表）
- 输出环境配置规范（.env / docker-compose / application.yml 分层）
- `docs/TASK_BOARD.md` v2.2 → v2.3：TASK-0001 移入 Completed，Sprint 0 进入 Ready


### Changed (Sprint 0 Phase 2 完成收尾)


- `docs/TASK_BOARD.md` v2.3 → v2.4：Sprint 0 Phase 2 三个任务收尾
  - TASK-0002 Backend Foundation：Reviewing → Done（PR #1 Squash merged to develop）
  - TASK-0003 Frontend Foundation：Reviewing → Done（PR #3 Squash merged to develop）
  - TASK-0005 AI Foundation：Reviewing → Done（Squash merged to develop）
  - 三个 feature 分支已删除（remote + local）
  - Branch Status：PR-Open → Merged
  - Sprint 0 DoD Code 段：Backend / Frontend / AI Foundation 三项已勾选
  - Sprint 0 DoD Architecture 段：8 项全部已勾选
  - Completed 区新增 TASK-0002 / TASK-0003 / TASK-0005 交付物清单


### Added (TASK-0004 Database Foundation)


- `docker-compose.yml`：PostgreSQL 16 + Redis 7 本地开发环境（healthcheck + 命名数据卷）
- `docker-compose.ci.yml`：CI 环境 tmpfs 覆盖（不持久化数据卷）
- `database/migrations/V20260728_001__create_user_table.sql`（DATABASE_DESIGN §6.1）
  - 字段：id / nickname / avatar / email / phone / city / status / created_time / updated_time / deleted_time
  - 枚举对齐 §7 USER_STATUS
  - 索引：uk_user_email (partial unique) / uk_user_phone (partial unique) / idx_user_status
- `database/migrations/V20260728_002__create_user_preference_table.sql`（DATABASE_DESIGN §6.2）
  - 字段：id / user_id / interest / budget / lifestyle / created_time / updated_time
  - 枚举对齐 §7 BUDGET_LEVEL
  - 索引：uk_user_preference_user_id (unique)
- `database/migrations/V20260728_003__create_tag_table.sql`（DATABASE_DESIGN §6.10）
  - 字段：id / user_id / name / type / created_time
  - 枚举对齐 §7 TAG_TYPE
  - 索引：uk_tag_user_name_type (unique)
- `backend/solo-server/src/main/resources/application.yml`：
  - Flyway locations 改为 `filesystem:database/migrations`（对齐 DATABASE_DESIGN §10）
  - 新增 validate-on-migrate: true
  - 新增 HikariCP 连接池配置（max 10 / min 2 / connection-timeout 30s）
- `backend/solo-server/.env.example`：新增 DB_POOL_MAX / DB_POOL_MIN / FLYWAY_LOCATIONS


### Added (TASK-0006 CI/CD Foundation)


- `.github/workflows/backend-ci.yml`：Backend CI 流水线
  - 触发：PR / push 到 develop（paths: backend/**）
  - JDK 17 + Maven 缓存
  - 步骤：clean compile + test（§15.8 Compile Validation）
  - 测试结果上传为 artifact
- `.github/workflows/frontend-ci.yml`：Frontend CI 流水线
  - 触发：PR / push 到 develop（paths: apps/**）
  - Node 20 + npm 缓存
  - 步骤：npm install + type-check（CODE_RULES §2）+ build:h5
  - 构建产物上传为 artifact
- `.github/branch-protection.md`：分支保护规则建议
  - main：PR + 1 approval + CI + 禁 bypass
  - develop：PR + 1 approval + CI + 禁 bypass
  - 含 gh API 配置命令
- `.github/PULL_REQUEST_TEMPLATE.md`：升级 PR 模板
  - 新增 DevOps 变更类型
  - 新增治理检查段（§15.6 PR 合并条件）
  - 新增 TASK_BOARD 字段
- Sprint 0 阶段 CI 中 test/build 步骤使用 continue-on-error: true（业务测试和完整构建依赖待 Sprint 1 补全）


### Added (TASK-0007 Documentation Foundation)


- `docs/architecture/ADR/README.md`：ADR Index
  - Accepted: ADR-0001 / 0002 / 0003 / 0004 / 0011
  - Proposed: ADR-0005 / 0010
  - Future: ADR-0006 (Sprint 1) / 0007 (Sprint 3) / 0008 (Sprint 5) / 0009 (Sprint 7)
  - ADR 生命周期规则：Proposed → Accepted → Deprecated
- `docs/architecture/ADR/template.md`：ADR 模板（Date / Status / Decision / Reason / Impact / Migration）
- `docs/governance/DOCUMENT_VERSION_RULE.md`：版本同步规则
  - 9 份核心文档的修改条件矩阵
  - 核心原则：代码优先 + 非必要禁止修改核心架构文档
  - 允许的修改场景：架构变更（需 ADR）/ Bug 修复 / 新增功能（需评估）
- `docs/AI_CHANGELOG_TEMPLATE.md`：AI 行为日志模板（Agent / Task / Action / Reason / Impact / Reviewer）


### Changed (Sprint 0 关闭)


- `docs/TASK_BOARD.md` v2.4 → v2.5：Sprint 0 正式关闭
  - TASK-0004 Database Foundation：Reviewing → Done（PR #6 Squash merged to develop）
  - TASK-0006 CI/CD Foundation：Reviewing → Done（PR #7 Squash merged to develop）
  - TASK-0007 Documentation Foundation：Reviewing → Done（PR #8 Squash merged to develop）
  - Sprint 0 Status：In Progress → Done (Closed 2026-07-29)
  - Sprint 0 DoD：Code / Documentation 段全部勾选；Test 段延期至 Sprint 1
  - Completed 段新增 TASK-0004 / TASK-0006 / TASK-0007 交付物清单
  - 新增 Sprint 0 Close Gate 段
- Sprint 0 全部 7 个任务达成（Architecture / Backend / Frontend / Database / AI Platform / CI/CD / Documentation），进入业务代码阶段，不再迭代架构文档


### Added (Sprint 1 启动 / TASK-0101 User Migration Review)


- Sprint 1：User Module 启动（Current Sprint 从 Sprint 0 切换至 Sprint 1）
- `docs/modules/user/MIGRATION_REVIEW.md`：User Module Migration Review
  - 字段逐项核对：user（10）+ user_preference（7）+ tag（5）= 22/22 全部对齐 DATABASE_DESIGN §6.1/§6.2/§6.10
  - 索引核对：5/5 全部对齐（uk_user_email / uk_user_phone / idx_user_status / uk_user_preference_user_id / uk_tag_user_name_type）
  - 枚举核对：3/3 全部对齐（USER_STATUS / BUDGET_LEVEL / TAG_TYPE）
  - 外键策略：逻辑关联，无物理 FK（§9）
  - Gap 分析：password 字段缺失，归 Auth 任务（ADR-0006 JWT），不在 TASK-0101 扩展
  - 审查结论：无需增量 Migration，User Domain Layer 可直接基于现有 schema 开发
- `docs/TASK_BOARD.md` v2.5 → v2.6：新增 TASK-0101 任务卡（Done），Sprint 1 剩余任务清单（TASK-0102~0106）


### Deprecated


### Removed


### Fixed


### Security


---

## 变更类型说明

- `feat` 新增功能
- `fix` 修复缺陷
- `refactor` 重构
- `docs` 文档变更
- `chore` 构建/工程任务
