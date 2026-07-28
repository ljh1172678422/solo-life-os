# Solo Life OS 系统架构

Version: 2.3

Status: Planning

Last Update: 2026-07-28


> 本文档定义 Solo Life OS 的研发约束，所有 AI Agent 与人类开发者必须遵守。
> 一旦本文档成熟，DATABASE_DESIGN / SPRINT_PLAN / TASK_BOARD / 各 Agent 行为都将自动统一。


---

# 1. Architecture Principles


Solo Life OS 采用：


- Domain Driven Design（轻量版）
- Modular Monolith
- AI Native Architecture


核心理念：


> 不是今天就微服务，而是设计成以后可以拆。


开发前期：


```
单体项目
  │
  ├── 模块化（按领域划分 package）
  └── 共享数据库
```


后期：


```
按模块拆服务
  │
  └── 无需重构代码
```


这是整个架构最重要的一句话。


其他原则：


- 模块间通过领域接口交互，禁止跨模块直连数据库
- 共享核心 Entity，禁止重复定义（如 Today/Growth/Explore 各建一套 User）
- AI 输出必须可解释、可拒绝
- 隐私优先，敏感数据默认脱敏


---

# 2. Layer Architecture


后端单模块内部采用严格分层，依赖方向只能向下：


```
UI（uni-app）
  ↓
Controller
  ↓
Application Service
  ↓
Domain
  ↓
Repository
  ↓
Database
```


层职责：


| 层 | 职责 | 禁止 |
|----|------|------|
| Controller | 接收请求、参数校验、返回封装 | 写业务逻辑 |
| Application Service | 协调领域对象、编排用例 | 直接写 SQL |
| Domain | 业务规则、实体行为 | 依赖 Controller / 框架 |
| Repository | 持久化 | 写业务规则 |


规则：


- Domain 不能依赖 Controller
- Repository 不能写业务
- Application Service 负责协调，不持有业务规则
- 跨层调用必须经上层，禁止 Domain 直接调 Repository 的实现类（依赖倒置）


---

# 3. Shared Domain


所有模块共享以下核心 Entity，禁止重复定义：


| Entity | 说明 | 使用模块 |
|--------|------|---------|
| User | 用户身份 | 全部 |
| Preference | 用户偏好 | Today / Explore / Growth |
| Activity | 生活事件 | Today / Explore / Community |
| Goal | 成长目标 | Growth / Today |
| Mood | 情绪状态 | Mood / Today / AI |
| Memory | AI 长期记忆 | AI / 全部 |
| Location | 地理位置 | Explore / Today |
| Tag | 通用标签 | 全部 |


规则：


- 新增 Entity 必须先在 DATABASE_DESIGN.md 登记
- 新增 Entity 必须先在本文档 Shared Domain 登记
- 禁止 Today 再建 User、Growth 再建 User、Explore 再建 User


---

# 4. Module Dependencies


模块依赖必须显式声明，禁止随意引用：


```
Today        → User / Preference / Memory
Explore      → User / Location / Preference
Mood         → Memory
Growth       → Goal / Memory
Community    → User / Activity / Location
Story        → Memory / Goal / Mood / Activity
User         → Preference
AI           → 全部模块（只读）
```


规则：


- 依赖方向单向，禁止循环依赖
- Growth 不能直接依赖 Community
- 模块间通过领域接口（Port）交互，不直接 import 对方 Repository
- AI 模块对所有业务模块只读，禁止反向写业务数据


---

# 5. 总体架构


```
                    Client
        (H5 / App / Mini Program)
                    │
              API Gateway
                    │
          ┌─────────────────────────┐
          │ Spring Boot Modular    │
          │ Monolith               │
          ├─────────────────────────┤
          │ ├── User Module        │
          │ ├── Today Module       │
          │ ├── Explore Module     │
          │ ├── Mood Module        │
          │ ├── Growth Module      │
          │ ├── Community Module   │
          │ └── Story Module       │
          └────────────┬────────────┘
                       │
          ┌─────────────────────────┐
          │      AI Platform        │
          ├─────────────────────────┤
          │ Memory Layer            │
          │ Context Builder         │
          │ Agent Router            │
          │ Planner Agent           │
          │ Recommendation Agent    │
          │ Emotion Agent           │
          │ Story Agent             │
          └────────────┬────────────┘
                       │
      ┌────────────────────────────────┐
      │ PostgreSQL │ Redis │ Vector DB │
      │                 │ OSS         │
      └────────────────────────────────┘
```


命名约定：


- Phase 0–2 阶段：**Module（模块）**，同进程同库
- Phase 3 以后：边界稳定的模块拆为 **Service（服务）**，与 Evolution Roadmap §13 一致


---

# 6. 模块设计


## User Module

负责：用户、偏好、设置

依赖：Preference


## Today Module

负责：今日生活规划

核心：DailyPlan

依赖：User / Preference / Memory


## Explore Module

负责：地点、路线、收藏

依赖：User / Location / Preference


## Mood Module

负责：情绪数据

依赖：Memory


## Growth Module

负责：目标、成长

依赖：Goal / Memory


## Community Module

负责：活动、社交

依赖：User / Activity / Location


## Story Module

负责：人生故事

依赖：Memory / Goal / Mood / Activity


---

# 7. AI Platform


AI Platform 完整链路：


```
Memory Layer
  ↓
Context Builder
  ↓
Agent Router
  ↓
Planner Agent / Recommendation Agent / Emotion Agent / Story Agent
  ↓
LLM Provider
```


各层职责：


| 层 | 职责 |
|----|------|
| Memory Layer | 长期记忆存储与检索 |
| Context Builder | 组装用户上下文（时间/位置/天气/心情/偏好） |
| Agent Router | 路由请求到具体 Agent |
| Agents | 执行具体 AI 任务 |
| LLM Provider | 模型调用抽象层 |


关键约束：


- LLM Provider 是抽象层，换 GPT / GLM / Claude 不影响业务
- 所有 Agent 必须经 Router 路由，禁止 Agent 之间直接相互调用
- Agent 之间需要协作时，通过 Memory Layer 共享上下文


---

# 8. Agent 列表


## Planner Agent

职责：生成每日计划

输入：时间 / 地点 / 天气 / 心情 / 用户偏好


## Recommendation Agent

职责：地点推荐


## Emotion Agent

职责：情绪分析

禁止：心理诊断


## Story Agent

职责：年度人生故事


## Assistant Agent

职责：通用对话与兜底


## Memory Service

职责：长期记忆与用户模型


注意：上述为最小可用集合，未来可按领域横向扩展（Travel / Finance / Health / Social），但必须通过 Agent Router 路由。


---

# 9. Event Flow


跨模块协作通过事件流解耦，禁止同步直连：


```
用户记录心情
  ↓
Mood Service
  ↓
Event: mood.recorded
  ↓
Memory 更新
  ↓
Recommendation 更新
  ↓
Today 重规划
```


典型事件：


| 事件 | 发布者 | 订阅者 |
|------|--------|--------|
| mood.recorded | Mood | Memory / Recommendation / Today |
| goal.progressed | Growth | Memory / Story |
| activity.completed | Today | Memory / Growth |
| user.preference.updated | User | Today / Explore / Recommendation |


规则：


- 事件发布者不依赖订阅者
- 事件处理异步，不影响主流程响应时间
- AI Agent 通过事件感知用户状态变化


---

# 10. Persistence


持久化架构：


| 存储 | 用途 | 示例 |
|------|------|------|
| PostgreSQL | 主数据库 | User / Activity / Goal / Mood |
| Redis | 缓存 / 会话 / 排行 | 今日计划缓存、热门地点 |
| Vector DB | AI 检索 | 语义记忆、相似度检索 |
| Object Storage | 文件存储 | 图片 / 头像 / AI 生成图片 |


规则：


- 图片、头像、AI 生成图片一律存 Object Storage，禁止入库
- 敏感数据加密存储
- 所有表必须含 created_time / updated_time，优先软删除


---

# 11. API Boundary


接口边界与调用链：


```
Frontend
  ↓
REST API
  ↓
Backend (Spring Boot)
  ↓
AI Service
  ↓
LLM
```


规则：


- 前端不能直接调 AI，必须经 Backend
- 前端不能直接访问数据库
- AI Service 对 Backend 是独立服务，但 MVP 阶段可同进程模块化
- 所有 API 统一返回格式：


```json
{
  "code": 0,
  "message": "",
  "data": {}
}
```


---

# 12. Repository Structure


仓库目录地图（与 AGENTS.md §12 一致）：


```
Solo-Life-OS
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
│   ├── prompts/             Prompt 文件（受 AGENTS.md §11 约束）
│   └── memory/              Memory 系统
│
├── database/
│   ├── design/              设计稿（Architecture Agent）
│   └── migrations/          迁移脚本（Backend Agent）
│
├── docs/
│   ├── PROJECT_CONTEXT.md
│   ├── ARCHITECTURE.md      本文档
│   ├── DATABASE_DESIGN.md
│   ├── AGENTS.md
│   ├── CODE_RULES.md
│   ├── TASK_BOARD.md
│   ├── SPRINT_PLAN.md
│   ├── CHANGELOG.md
│   └── AI_CHANGELOG.md
│
├── scripts/                  工程脚本
│
└── .github/
    ├── workflows/           CI/CD
    └── PULL_REQUEST_TEMPLATE.md
```


注意：


- apps/ backend/ ai/ database/ scripts/ 暂未创建，对应 Sprint 启动时建立
- 当前 Sprint 0 阶段仅 docs/ 与 .github/ 活跃
- 新增目录必须先在本文档与 AGENTS.md §12 双登记


---

# 13. Evolution Roadmap


架构演进路线，AI Agent 必须遵守，禁止提前跳跃：


```
Phase 0  单体（Modular Monolith）
  ↓
Phase 1  模块化深化（领域接口 Port + Adapter）
  ↓
Phase 2  AI Platform 独立部署
  ↓
Phase 3  按业务拆服务（仅边界稳定的模块）
  ↓
Phase 4  多 Region 部署
```


各阶段触发条件：


| Phase | 触发条件 |
|-------|---------|
| 0 → 1 | 基础功能完成，开始出现跨模块协作 |
| 1 → 2 | AI 调用量上升，需独立扩容 |
| 2 → 3 | 模块边界稳定 + 存在明显性能压力 |
| 3 → 4 | 用户规模扩大，需地域就近服务 |


禁止：


- 在 Phase 0 阶段直接写微服务
- 在模块边界未稳定时拆服务
- 跳跃式演进（如从 Phase 0 直接到 Phase 3）


---

# 14. Architecture Decision Record (ADR)


任何重大架构决策必须以 ADR 形式记录，禁止直接改 ARCHITECTURE.md。


存放目录：`docs/architecture/ADR/`


命名格式：`ADR-XXXX-<简述>.md`（四位流水号）


ADR 内容必须包含：


```
# ADR-XXXX: <决策标题>

Date:    YYYY-MM-DD
Status:  Proposed / Accepted / Superseded by ADR-YYYY
Decision:<决策内容>
Reason:  <为什么这样决策>
Impact:  <影响范围>
```


必须写 ADR 的场景：


- 引入或替换核心框架
- 改变模块边界
- 改变数据库选型
- 改变 AI Agent 路由方式
- 任何与 Evolution Roadmap 不一致的偏离


已有 ADR：


- ADR-0001 采用 Modular Monolith（Accepted）
- ADR-0002 选择 PostgreSQL 作为主数据库（Accepted，含 Redis / Vector DB / OSS 分层禁止项）
- ADR-0003 AI Agent 统一 Router（Accepted，含 Agent 不持有业务状态约束）
- ADR-0004 MVP 阶段不使用微服务（Accepted）


待写 ADR（按 Sprint 生命周期推进，禁止提前批量创建）：


- ADR-0005 Vector DB Adapter Strategy（Sprint 0 创建，Proposed；候选 pgvector / Milvus / Qdrant + Adapter 延迟绑定）
- ADR-0006 JWT 策略（Sprint 1 创建，User Module 启动时）
- ADR-0007 Map Provider Adapter（Sprint 3 创建，采用 Provider Adapter Pattern，避免高德 / 腾讯硬绑定）
- ADR-0008 LLM Provider Strategy（Sprint 5 创建，采用抽象层策略，不锁定具体 Provider）
- ADR-0009 Payment Adapter（Sprint 7 创建，Community MVP 免费活动可延期）
- ADR-0010 Tag Ownership（Sprint 0 创建，Proposed；User Module vs Shared Kernel 决策）
- ADR-0011 Activity Ownership（Sprint 0 创建，Accepted；Activity 归 Today，CommunityEvent 独立）


ADR 创建时机规则：


- ADR 必须在对应 Sprint 启动时创建，禁止一次性批量创建 ADR-0005~0011
- 仅当 Sprint 0 即将依赖的领域边界 ADR（ADR-0010 / ADR-0011）可在 Sprint 0 提前创建
- ADR-0006~0009 严格按对应 Sprint 创建，避免决策过早被锁定


---

# 15. Non-Functional Requirements (NFR)


所有功能开发必须同时满足以下非功能性指标：


## Performance 性能


| 接口类型 | P95 延迟 | 超时 |
|---------|---------|------|
| 普通 API | < 300ms | 3s |
| AI 接口 | < 5s | 30s |
| 地图接口 | < 2s | 10s |
| 列表 / 搜索 | < 500ms | 5s |


## Availability 可用性


- MVP 阶段：99% 可用
- 正式阶段：99.9% 可用
- 核心路径（登录 / 今日计划）：99.95% 可用


## Scalability 可扩展性


- 单实例支持 1k QPS
- AI 调用支持队列削峰
- 数据库读写分离预留


## Privacy 隐私


- 位置 / 情绪 / 行为数据默认脱敏
- 禁止 AI 输出敏感推断
- 支持数据删除（详见 PROJECT_CONTEXT §13）


---

# 16. Observability


所有 Service / Module 必须接入可观测性体系：


| 维度 | 实现 | 必含字段 |
|------|------|---------|
| Logging | 结构化 JSON 日志 | traceId / userId / requestId / module |
| Metrics | Prometheus 指标 | qps / latency / error_rate |
| Tracing | 分布式链路追踪 | traceId 贯穿前端→Backend→AI |
| Audit | 审计日志 | 操作人 / 时间 / 变更内容 |


规则：


- 所有请求必须携带 traceId，跨服务透传
- AI Agent 调用必须记录 input / output / latency / token 消耗
- 敏感字段日志输出前必须脱敏
- 审计日志独立存储，不可修改


---

# 17. Security Boundary


## Authentication 认证


- 所有非登录接口必须验证 token
- token 由 User Module 统一签发
- AI Agent 调用 Backend 必须携带服务间凭证


## Authorization 授权


- 基于角色的访问控制（RBAC）
- 用户只能访问自己的数据
- AI Agent 只读业务数据，写操作必须经 Domain API


## Rate Limit 限流


| 维度 | 限制 |
|------|------|
| 单用户 API | 100 req/min |
| AI 接口 | 20 req/min |
| 登录 | 5 req/min |


## Encryption 加密


- 传输：HTTPS 强制
- 存储：密码 bcrypt，敏感字段 AES-256
- token：JWT + 短期过期


禁止：


- AI 绕过权限直接访问数据
- 把凭证写入代码或日志
- 明文存储密码


---

# 18. Integration Boundary


外部集成必须通过 Adapter 层，业务模块禁止直接调用第三方 SDK。


```
Business Module
  ↓
Integration Port（领域接口）
  ↓
Adapter（基础设施层）
  ↓
External API
```


已知外部依赖：


| 类型 | 用途 | 备注 |
|------|------|------|
| Weather | 天气数据 | Today 计划输入 |
| Map | 地图 / 地点 | Explore 模块 |
| Payment | 支付 | Community 报名 |
| LLM | 大模型 | AI Platform |
| Push | 推送通知 | Today / Community |
| OAuth | 第三方登录 | User Module |


规则：


- 所有外部调用必须有超时与重试策略
- 外部调用失败必须降级，不能阻塞主流程
- Adapter 实现可替换（如换地图供应商）


---

# 19. Package Convention


后端每个模块的包结构固定，AI 生成代码必须遵守：


```
backend/
└── solo-server/
    └── com/sololifeos/
        ├── user/
        │   ├── controller/
        │   ├── application/
        │   ├── domain/
        │   │   ├── model/
        │   │   └── service/
        │   ├── repository/
        │   └── infrastructure/
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


禁止：


- 使用 service / services / biz / manager 等不一致命名
- 跨模块直接 import 对方内部类
- 在 controller 包写业务逻辑


---

# 20. Error Handling


统一异常体系与错误码：


## 异常类层级


```
RuntimeException
  └── SoloException
       ├── BusinessException       业务异常
       ├── ValidationException      参数校验异常
       ├── AIException              AI 调用异常
       ├── ExternalException        外部依赖异常
       └── AuthException            认证授权异常
```


## 错误码规范


```
<模块号><错误类型><流水号>

示例：
USER-ERR-001  用户不存在
TODAY-ERR-001 计划生成失败
AI-ERR-001    LLM 调用超时
```


## 统一返回


所有异常由全局异常处理器捕获，统一返回格式：


```json
{
  "code": 1001,
  "message": "用户不存在",
  "data": null,
  "traceId": "xxx"
}
```


规则：


- 禁止向客户端返回堆栈信息
- 禁止用 200 + code=500 这类语义混乱组合
- 业务异常 HTTP 状态码统一 400，系统异常 500


---

# 21. AI Boundary（最重要）


AI Agent 永远不能直接修改数据库，必须通过 Domain API。


正确流程：


```
Planner Agent
  ↓
TodayModule.createPlan()（Domain API）
  ↓
Application Service
  ↓
Repository
  ↓
Database
```


错误流程（禁止）：


```
Planner Agent
  ↓
SQL / Repository 直调
```


规则：


- AI Agent 只能调用各模块暴露的 Domain API
- AI Agent 不能持有 Repository 引用
- AI Agent 写操作必须经 Domain 校验业务规则
- AI Agent 失败必须可回滚（事务边界在 Application Service）


这是 AI 项目最大的坑。绕过 Domain 直连数据会导致：
- 业务规则被绕过
- 数据一致性破坏
- 审计与权限失效


---

# 22. Data Ownership


每个核心数据对象有唯一 Owner 模块，其他模块只能通过 Owner 的 Domain API 访问。


| 数据 | Owner 模块 | 其他模块访问方式 |
|------|-----------|---------------|
| User | User | Domain API |
| Preference | User | Domain API |
| Activity | Today | 事件订阅 + Domain API |
| Goal | Growth | Domain API |
| Mood | Mood | 事件订阅 + Domain API |
| Memory | AI | Domain API |
| Conversation | AI | Domain API |
| Location | Explore | Domain API |
| Tag | User | Domain API |
| CommunityEvent | Community | Domain API |
| Registration | Community | Domain API |


规则：


- Today 不能直接改 Goal
- Growth 不能直接写 Mood
- 所有写操作归 Owner，跨模块通过事件或 Domain API
- 读操作可经 Domain API 只读访问


禁止：

任何模块绕过 Owner 直连数据库修改非己方数据。


---


# 23. Version History


| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-28 | 初始版本，部署拓扑图 |
| v2.0 | 2026-07-28 | 全量升级为研发约束文档；新增 §1–§13 |
| v2.1 | 2026-07-28 | §5 总体架构图统一 Module；新增 §14 ADR / §15 NFR / §16 Observability / §17 Security / §18 Integration / §19 Package / §20 Error / §21 AI Boundary / §22 Data Ownership；新增 ADR-0001~0004 |
| v2.2 | 2026-07-28 | §22 Data Ownership 调整 Activity Owner 为 Today；新增 Conversation / CommunityEvent / Registration；§9 Event Flow 调整 activity.completed 发布者；§14 ADR 列表新增 ADR-0010 / ADR-0011 |
| v2.3 | 2026-07-28 | §14 ADR 清单重写：ADR 生命周期与 Sprint 生命周期一致；ADR-0006~0009 标注对应 Sprint 与 Pending；ADR-0010 提前到 Sprint 0（Proposed）；ADR-0011 提前到 Sprint 0（Accepted）；ADR-0002 Impact 补充存储分层禁止项；ADR-0003 Decision 补充 Agent 不持有业务状态约束 |
