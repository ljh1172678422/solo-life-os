# ADR-0014: AI Platform Six Roles and Confidence Gating

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §六、§九、§十六 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §11 决策。
> 与 Accepted ADR-0003（AI Agent Unified Router）协同：本 ADR 定义产品角色，ADR-0003 定义技术调度入口。

---

## Decision

将 AI Platform 角色从旧的 6 Agent（Planner / Recommendation / Emotion / Story / Assistant / Memory Service）重构为 6 产品角色：

| 角色 | 职责 | 输入 | 输出 | 替代的旧角色 |
|---|---|---|---|---|
| **Opportunity Discovery** | 发现真实、时效性的候选机会（ExperienceOpportunity） | 位置/天气/时间/外部事实 | 候选机会列表 + 可信度 | Recommendation Agent（部分） |
| **Proposal Composer** | 将候选机会组织成五要素体验提案（ExperienceProposal） | 候选机会 + 用户反应地图 | 单个提案（或 no_proposal） | Planner Agent |
| **Motivation Engine** | 判断行动阻力、时机适配度、启动意愿 | 提案 + 用户状态 + 历史 | 阻力评分 + 适配度评分 | 无（新增） |
| **Life Curator** | 最终取舍、置信度门控、决定是否保持安静 | 提案 + 阻力评分 + 适配度 | GateDecision（输出/不输出/降级输出） | 无（新增，产品判断角色） |
| **State Understanding** | 接收用户主动提供的「此刻状态」输入，理解用户当前状态 | 用户主动输入 | 状态向量 | Emotion Agent（替代） |
| **Assistant** | 用户主动查询入口，不塑造陪伴关系 | 用户主动查询 | 响应 | Assistant Agent（保留，边界明确） |

### 关键约束

1. **Life Curator 是产品判断角色，不是技术调度角色**：Life Curator 负责「是否输出提案」「输出什么级别的提案」「是否保持安静」的产品判断，不负责技术调度
2. **State Understanding 不诊断**：仅接收用户主动输入，不做趋势分析、不做心理诊断（产品宪法 §十.3）
3. **Assistant 不扮演关系**：仅作为用户主动查询入口，不主动发起对话、不塑造陪伴关系（产品宪法 §六 P4）

## Reason

- **产品驱动**：产品宪法 §十六明确 AI 内部能力包括 Motivation Engine 与 Life Curator；§六 P5 要求「一次一份提案」；§九要求置信度门控与主动克制
- **架构约束**：ADR-0003 已确立 Agent Router 作为唯一技术入口，6 角色必须经 Router 调度，不可绕过
- **演进约束**：旧 Planner Agent 职责（生成每日计划）与产品宪法 §六 P5「一次一份提案」冲突；旧 Emotion Agent 职责（情绪分析）与 §十.3「不诊断」冲突

## Confidence Gating（恢复上游映射）

Life Curator 基于置信度对 AI 输出进行门控（产品宪法 §九，PROJECT_CONTEXT v1.3 §11）：

| 置信度 | 产品动作 | 用户体验 |
|---|---|---|
| **高** | 在用户允许的时间内主动提醒一次，清楚解释"为什么是现在" | 用户在允许时间收到一次主动提醒 + 提案 |
| **中** | 仅在用户打开 Solo 时展示，不主动打扰 | 用户打开 App 时看到提案，无主动推送 |
| **低** | 询问一个低负担问题，或不做推荐 | 用户看到低负担问题或 no_proposal |

### 置信度计算输入

- Opportunity Discovery 的候选机会可信度（ADR-0015 事实可信度模型）
- Motivation Engine 的阻力评分与适配度评分
- State Understanding 的用户状态向量
- LifeResponseMap 的历史反馈模式

## Life Curator 与 Router 的可执行边界

### 平台基础设施（不属于 6 产品角色）

以下为 AI Platform 基础设施，与 6 产品角色分离：

- **Agent Router**（ADR-0003）：唯一技术入口，所有请求经 Router 调度
- **Memory Layer**：Memory/Context 读写，6 角色通过 Memory Layer 读写记忆，不直接访问 ai_memory 表
- **Context Builder**：构建请求上下文，供 Router 编排时使用

### 调用契约

1. **Router 是唯一技术入口**：所有外部请求（用户主动查询、定时触发、通知触发）必须经 Router，6 角色 Service 不得直接暴露给外部
2. **Router 或 Workflow Orchestrator 编排多角色**：单个请求需多角色协同时（如生成提案需 Opportunity Discovery → Proposal Composer → Motivation Engine → Life Curator），由 Router 或其后的 Workflow Orchestrator 编排，角色之间不直接互调
3. **角色 Service 不直接互调**：6 角色 Service 之间禁止直接调用，必须经 Router/Orchestrator 编排
4. **Life Curator 只返回 GateDecision**：Life Curator 输出 GateDecision（OUTPUT_PROPOSAL / SHOW_ONLY_IN_APP / ASK_LOW_BURDEN_QUESTION / NO_PROPOSAL），不直接发送通知、不直接写库
5. **通知发送由通知服务执行**：通知服务在发送前再次校验授权（ADR-0016）、场景、频率上限、静默时段、置信度门槛，校验通过才发送
6. **Memory/Context 不属 6 角色**：Memory Layer、Context Builder、Router 是平台基础设施，6 角色通过接口使用，不拥有

### 典型编排流程

```
用户打开 Solo
  ↓
Router（技术入口）
  ↓
Context Builder（构建上下文：用户状态 + 历史反馈）
  ↓
Router 编排：
  ├─ Opportunity Discovery（生成候选机会）
  ├─ Proposal Composer（组织提案）
  ├─ Motivation Engine（阻力/适配度评分）
  └─ Life Curator（返回 GateDecision）
  ↓
通知服务（若 GateDecision=OUTPUT_PROPOSAL 且高置信度，校验授权/频率/静默后发送）
  ↓
App 内展示（若 GateDecision=SHOW_ONLY_IN_APP 或 ASK_LOW_BURDEN_QUESTION）
```

## Proactive Notification Boundary

主动通知必须满足（产品宪法 §九 + PROJECT_CONTEXT v1.3 §11）：

1. **频率上限**：每天最多 1 次主动提醒（用户可配置）
2. **静默时段**：用户可配置静默时段（如 22:00-08:00），静默期内不推送
3. **置信度门槛**：仅高置信度可主动提醒；中/低置信度不主动触达
4. **完全可控关闭**：用户可一键关闭某类通知，或关闭所有主动通知
5. **无催促**：不因用户未响应而重复催促；不用倒计时、红点、稀缺文案

## Role Granularity Decision

6 角色的实现粒度（独立 Agent / 服务 / 枚举）方案比较：

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. 独立 Agent 服务** | 每个角色独立微服务 | 边界清晰；可独立部署 | 部署复杂度高；调度复杂；与 ADR-0004 冲突 | 角色复杂度高、需独立扩缩容 |
| **B. 枚举 + 服务类** | AgentRole 枚举 + 每角色一个 Service 类，统一部署在 AI Platform | 复杂度可控；与 ADR-0004 一致；边界可后续调整 | 单进程内耦合；角色边界靠约定维护 | MVP 阶段、角色边界待验证 |
| **C. 单服务内函数** | 所有角色作为单服务的函数 | 实现最简 | 边界模糊；难维护；难演进 | 原型阶段 |

**MVP 阶段采用方案 B（枚举 + 服务类）**，理由：

1. **与 ADR-0004 一致**：MVP 不引入微服务，6 角色作为 AI Platform 内部实现
2. **角色边界仍需验证**：6 角色职责边界在 MVP 中需通过实际运行验证，过早拆分为独立服务难以调整
3. **未来可演进**：若某角色复杂度增长（如 Motivation Engine 需独立模型），可后续 ADR 拆分为独立服务（走 ADR-0003 路由扩展）

### 具体实现

- `AgentRole` 枚举：6 个值（OPPORTUNITY_DISCOVERY / PROPOSAL_COMPOSER / MOTIVATION_ENGINE / LIFE_CURATOR / STATE_UNDERSTANDING / ASSISTANT）
- 每个角色对应一个 Service 类：`OpportunityDiscoveryService` / `ProposalComposerService` / 等
- Life Curator Service 实现置信度门控逻辑，返回 GateDecision
- Agent Router（ADR-0003）仍为唯一技术入口，路由到对应角色 Service
- 通知服务独立于 6 角色，负责发送前校验

## Impact

### 影响模块

- AI Platform：全部角色重构，新增 Router/Orchestrator 编排逻辑
- Today：ExperienceProposal 由 Proposal Composer 生成，Life Curator 门控输出
- Explore：Opportunity Discovery 依赖 location 数据
- Mood：State Understanding 接收 mood 输入

### 需要修改的文档

- ARCHITECTURE.md：§7 AI Platform 链路、§8 Agent 列表、§21 AI Boundary（第 4 步）
- DATABASE_DESIGN.md：§7 AGENT_TYPE 枚举、ai_memory/ai_conversation 表设计（第 4 步，Sprint 5 落地）
- CODE_RULES.md：§11 AI 规则（第 6 步）
- SPRINT_PLAN.md：Sprint 5 AI Platform 任务重定义（第 5 步）
- TASK_BOARD.md：TASK-0307 Recommendation Agent 标 Blocked → 重构为 Opportunity Discovery + Proposal Composer（第 5 步）

### 需要新增/修改的代码

- Sprint 5 AI Platform 实现：
  - AgentRole 枚举
  - 6 个角色 Service 类
  - Life Curator 置信度门控逻辑（返回 GateDecision）
  - 通知服务（发送前校验授权/频率/静默/置信度）
  - Router/Orchestrator 编排逻辑
- TASK-0307（Recommendation Agent）：标记 Blocked，待 Sprint 5 重构
- 现有 Planner Agent 代码（Sprint 2 TASK-0207）：重构为 Proposal Composer

### 是否影响现有数据

- ai_memory / ai_conversation 表：未建 migration，无影响
- 现有 MockMemoryService：可保留接口，实现按新角色调整

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 ARCHITECTURE.md §7 §8 §21
2. 第 5 步：重排 SPRINT_PLAN Sprint 5，标注 TASK-0307 Blocked
3. Sprint 5 落地：实现 6 角色 + 置信度门控 + 通知边界 + Router 编排

### Follow-up ADR

- 若未来某角色需独立服务化，新建 ADR 决策拆分（走 ADR-0003 路由扩展）
- 事实可信度模型由 ADR-0015 决定
- 心理健康安全流程由 ADR-0018 决定
- LifeResponseMap 数据治理由 ADR-0019 决定

### 验证方式

- ARCHITECTURE.md §8 角色列表与本 ADR 一致
- AGENT_TYPE 枚举为 6 值
- Life Curator 置信度门控逻辑实现并测试，返回 GateDecision
- 通知服务发送前校验授权/频率/静默/置信度（测试用例覆盖）
- 角色 Service 不直接互调（代码审查 + 架构测试）
- no_proposal 响应正确返回（与 ADR-0013 ExperienceProposal 配合）
