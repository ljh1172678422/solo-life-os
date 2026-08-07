# ADR-0014: AI Platform Six Roles and Confidence Gating

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §六、§九、§十六 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §11 决策。

---

## Decision

将 AI Platform 角色从旧的 6 Agent（Planner / Recommendation / Emotion / Story / Assistant / Memory Service）重构为 6 角色：

| 角色 | 职责 | 输入 | 输出 | 替代的旧角色 |
|---|---|---|---|---|
| **Opportunity Discovery** | 发现真实、时效性的候选机会（ExperienceOpportunity） | 位置/天气/时间/外部事实 | 候选机会列表 + 可信度 | Recommendation Agent（部分） |
| **Proposal Composer** | 将候选机会组织成五要素体验提案（ExperienceProposal） | 候选机会 + 用户反应地图 | 单个提案（或 no_proposal） | Planner Agent |
| **Motivation Engine** | 判断行动阻力、时机适配度、启动意愿 | 提案 + 用户状态 + 历史 | 阻力评分 + 适配度评分 | 无（新增） |
| **Life Curator** | 最终取舍、置信度门控、决定是否保持安静 | 提案 + 阻力评分 + 适配度 | 输出/不输出/降级输出 | 无（新增，产品判断角色） |
| **State Understanding** | 接收用户主动提供的「此刻状态」输入，理解用户当前状态 | 用户主动输入 | 状态向量 | Emotion Agent（替代） |
| **Assistant** | 用户主动查询入口，不塑造陪伴关系 | 用户主动查询 | 响应 | Assistant Agent（保留，边界明确） |

### 关键约束

1. **Life Curator 是产品判断角色，不是技术调度角色**：Life Curator 负责「是否输出提案」「输出什么级别的提案」「是否保持安静」的产品判断，不负责技术调度
2. **Life Curator 不替代 Router**：Agent Router（ADR-0003）是技术调度能力，负责请求路由；Life Curator 是产品判断能力，负责输出门控。两者职责不同，调用关系由 AI 架构实现决定（本 ADR 不锁定调用顺序）
3. **State Understanding 不诊断**：仅接收用户主动输入，不做趋势分析、不做心理诊断（产品宪法 §十.3）
4. **Assistant 不扮演关系**：仅作为用户主动查询入口，不主动发起对话、不塑造陪伴关系（产品宪法 §六 P4）

## Confidence Gating

Life Curator 基于置信度对 AI 输出进行门控（产品宪法 §九）：

| 置信度 | 产品动作 | 用户体验 |
|---|---|---|
| **高** | 主动推送单个提案 | 用户收到一份体验提案 |
| **中** | 询问一个低负担问题 | 用户收到一个简单问题（如「现在更想动一下还是安静一下？」） |
| **低** | 保持安静（no_proposal） | 用户看到「今天没有合适建议，可以休息」 |

### 置信度计算输入

- Opportunity Discovery 的候选机会可信度（ADR-0015 事实可信度模型）
- Motivation Engine 的阻力评分与适配度评分
- State Understanding 的用户状态向量
- LifeResponseMap 的历史反馈模式

## Proactive Notification Boundary

主动通知必须满足（产品宪法 §九）：

1. **频率上限**：每天最多 1 次主动推送（用户可配置）
2. **静默时段**：用户可配置静默时段（如 22:00-08:00），静默期内不推送
3. **置信度门槛**：仅高置信度可主动推送；中/低置信度不主动触达
4. **完全可控关闭**：用户可一键关闭某类通知，或关闭所有主动通知
5. **无催促**：不因用户未响应而重复催促；不用倒计时、红点、稀缺文案

## Role Granularity Decision

6 角色的实现粒度（独立 Agent / 服务 / 枚举）决策：

**MVP 阶段采用「枚举 + 服务」粒度**，理由：

1. **MVP 复杂度可控**：6 个独立 Agent 服务会显著增加部署与调度复杂度
2. **角色边界仍需验证**：6 角色的职责边界在 MVP 中需通过实际运行验证，过早拆分为独立服务难以调整
3. **与 ADR-0004 一致**：MVP 不引入微服务，6 角色作为 AI Platform 内部的角色枚举 + 服务类实现
4. **未来可演进**：若某角色复杂度增长（如 Motivation Engine 需独立模型），可后续 ADR 拆分为独立服务

### 具体实现

- `AgentRole` 枚举：6 个值（OPPORTUNITY_DISCOVERY / PROPOSAL_COMPOSER / MOTIVATION_ENGINE / LIFE_CURATOR / STATE_UNDERSTANDING / ASSISTANT）
- 每个角色对应一个 Service 类：`OpportunityDiscoveryService` / `ProposalComposerService` / 等
- Life Curator Service 实现置信度门控逻辑
- Agent Router（ADR-0003）仍负责技术调度，路由到对应角色 Service

## Impact

### 影响模块

- AI Platform：全部角色重构
- Today：ExperienceProposal 由 Proposal Composer 生成
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
  - Life Curator 置信度门控逻辑
  - 主动通知频率/静默/置信度控制
- TASK-0307（Recommendation Agent）：标记 Blocked，待 Sprint 5 重构
- 现有 Planner Agent 代码（Sprint 2 TASK-0207）：重构为 Proposal Composer

### 是否影响现有数据

- ai_memory / ai_conversation 表：未建 migration，无影响
- 现有 MockMemoryService：可保留接口，实现按新角色调整

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 ARCHITECTURE.md §7 §8 §21
2. 第 5 步：重排 SPRINT_PLAN Sprint 5，标注 TASK-0307 Blocked
3. Sprint 5 落地：实现 6 角色 + 置信度门控 + 通知边界

### Follow-up ADR

- 若未来某角色需独立服务化，新建 ADR 决策拆分
- 事实可信度模型由 ADR-0015 决定

### 验证方式

- ARCHITECTURE.md §8 角色列表与本 ADR 一致
- AGENT_TYPE 枚举为 6 值
- Life Curator 置信度门控逻辑实现并测试
- no_proposal 响应正确返回（与 ADR-0013 ExperienceProposal 配合）
- 主动通知频率/静默/置信度控制实现并测试
