# Solo Life OS 系统架构

Version: 2.0

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
          ┌──────────────────┐
          │  Spring Boot App │
          ├──────────────────┤
          │ User             │
          │ Today            │
          │ Explore          │
          │ Mood             │
          │ Growth           │
          │ Community        │
          │ Story            │
          └────────┬─────────┘
                   │
          ┌──────────────────┐
          │   AI Platform    │
          ├──────────────────┤
          │ Memory Layer     │
          │ Context Builder  │
          │ Agent Router     │
          │ Planner Agent    │
          │ Recommendation   │
          │ Emotion Agent    │
          │ Story Agent      │
          └────────┬─────────┘
                   │
      ┌────────────────────────┐
      │ PostgreSQL │ Redis     │
      │ Vector DB  │ OSS       │
      └────────────────────────┘
```


---

# 6. 服务设计


## User Service

负责：用户、偏好、设置

依赖：Preference


## Plan Service（Today）

负责：Today 模块

核心：DailyPlan

依赖：User / Preference / Memory


## Explore Service

负责：地点、路线、收藏

依赖：User / Location / Preference


## Mood Service

负责：情绪数据

依赖：Memory


## Growth Service

负责：目标、成长

依赖：Goal / Memory


## Community Service

负责：活动、社交

依赖：User / Activity / Location


## Story Service

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
| activity.completed | Today / Explore | Memory / Growth |
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
