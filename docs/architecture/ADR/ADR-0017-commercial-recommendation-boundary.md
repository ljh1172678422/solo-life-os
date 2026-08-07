# ADR-0017: Commercial Recommendation Boundary

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.4 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §8 决策。
> 产品宪法检验标准：「如果这个商家不付费，Solo 还会不会把它推荐给这个用户？」答案是否定的，它就不应该伪装成个人化生活建议。

---

## Decision

商业关系建模在**独立的 `commercial_campaign` + `commercial_attribution` 模型**上，**不作为 Location 的静态属性**。候选资格与自然排序完全忽略商业字段；只有本来就能自然入选的候选才允许附加商业归因；商业元数据在自然决策完成后附加；保留可审计的披露、主体、类型、有效期与来源。

## Boundary Rules（产品宪法 §十.4）

1. **商业合作不得暗中改变提案排序**：赞助、佣金、商家合作关系不得影响 Opportunity Discovery 的候选资格与可信度计算，不得影响 Life Curator 的置信度门控
2. **商业内容必须明确标识**：任何赞助、佣金或合作关系的提案必须在用户端显著标识
3. **商业内容必须经适配度校验**：商业提案必须经过 Proposal Composer 的五要素适配度校验，不得绕过
4. **推荐信任不可出售**：Life Curator 的置信度门控不可因商业合作降级或升级
5. **自然入选前置**：只有本来就能自然入选的候选才允许附加商业归因（"通过同一适配度校验" ≠ "不付费也会进入自然推荐"，必须先通过自然排序入选，再谈商业归因）

## 商业关系建模方案比较

| 方案 | 描述 | 优点 | 缺点 | 适用场景 |
|---|---|---|---|---|
| **A. Location 静态属性** | location 表新增 sponsor_id / commercial_type | 查询简单；单表即可判断 | 商业关系有时间/活动/渠道/合同范围，静态属性无法表达；一个地点可能同时或先后存在多个商业关系；居家或非地点型提案无法承载商业内容；商业字段污染地点领域模型 | 商业关系极简且与地点 1:1 绑定时（不采用） |
| **B. Proposal/Candidate 级归因** | 在 ExperienceProposal 上挂 commercial_attribution | 支持非地点型提案；归因跟随提案而非地点 | 一份提案只能有一个商业关系，无法表达「该提案同时受多个商业合同覆盖」；campaign 生命周期与提案耦合 | 商业关系简单时 |
| **C. 独立 Campaign + Attribution 模型** | 独立 commercial_campaign（合同/活动）+ commercial_attribution（候选↔campaign 关联） | 商业关系有时间/范围/合同维度；一个候选可关联多个 campaign；归因在自然决策后附加，不影响自然排序；支持非地点型提案；可审计 | 模型稍复杂，需多表关联 | 商业关系有时间范围、多渠道、需审计时（采用） |

**采用方案 C**：独立 `commercial_campaign` + `commercial_attribution` 模型。理由：
- 商业关系本质具有时间、活动、渠道和合同范围，静态属性无法表达
- 一个地点可能同时或先后存在多个商业关系（如同时有平台赞助 + 商家佣金）
- 居家或非地点型提案也可能包含商业内容（如某茶具品牌赞助居家泡茶体验提案）
- 独立模型使商业归因在自然决策后附加，物理上隔离商业字段对自然排序的影响
- 可审计的披露、主体、类型、有效期和来源需要独立实体承载

## Data Model

### 新增表：`commercial_campaign`（商业合同/活动，独立于 Location）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| sponsor_name | VARCHAR(100) | 赞助商/商家名称（用于披露文案） |
| sponsor_type | VARCHAR(20) | MERCHANT / PLATFORM / AFFILIATE / PARTNERSHIP |
| commercial_type | VARCHAR(20) | SPONSORED / AFFILIATE / PARTNERSHIP / COMMISSION |
| disclosure_text | VARCHAR(500) | 披露文案（如「该提案由 XX 赞助」），用户可见 |
| target_scope | VARCHAR(20) | LOCATION / PROPOSAL / CATEGORY（商业关系覆盖范围） |
| target_ref | VARCHAR(100) | 目标引用（target_scope=LOCATION 时为 location 逻辑标识；PROPOSAL 时为 proposal 模板标识；CATEGORY 时为品类标识） |
| valid_from | TIMESTAMP | 合同/活动开始时间 |
| valid_until | TIMESTAMP | 合同/活动结束时间（nullable 表示长期） |
| source | VARCHAR(200) | 商业关系来源（合同编号/对接人/系统录入） |
| status | VARCHAR(20) | ACTIVE / PAUSED / EXPIRED / TERMINATED |
| created_time | TIMESTAMP | |
| updated_time | TIMESTAMP | |

### 新增表：`commercial_attribution`（候选↔campaign 关联，在自然决策后附加）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| proposal_id | BIGINT | 关联 ExperienceProposal（逻辑关联，无物理 FK） |
| campaign_id | BIGINT | 关联 commercial_campaign（逻辑关联，无物理 FK） |
| attributed_at | TIMESTAMP | 归因附加时间（自然决策完成后） |
| is_disclosed | BOOLEAN | 是否已向用户披露（默认 false，输出时置 true） |
| created_time | TIMESTAMP | |

> 注：location 表**不新增**任何商业字段。商业关系完全由独立模型承载，避免污染地点领域模型（对齐 DATABASE_DESIGN §9「逻辑关联不建 FK」规则）。

### ExperienceProposal 输出扩展

ExperienceProposal 输出新增字段（自然决策完成后填充）：

| 字段 | 类型 | 说明 |
|---|---|---|
| is_commercial | BOOLEAN | 是否含商业归因（默认 false） |
| commercial_disclosures | LIST<Disclosure> | 商业披露列表（is_commercial=true 时必填，可多条） |

`Disclosure` 结构：
- sponsor_name：赞助商名称
- commercial_type：商业类型
- disclosure_text：披露文案
- valid_until：有效期

## Pipeline Constraints（自然决策后附加商业归因）

```
Opportunity Discovery
  ├─ 候选资格计算（完全忽略 commercial_campaign / commercial_attribution）
  ├─ fact_confidence 计算（source_weight 不受商业合作影响）
  ↓
Proposal Composer
  ├─ 五要素适配度校验（候选与商业关系无关，纯自然适配）
  ↓
Life Curator
  ├─ 置信度门控（不可因商业合作降级或升级）
  ├─ 自然决策完成 → 选定最终提案
  ↓
Commercial Attribution（自然决策后附加）
  ├─ 查询该提案关联的 ACTIVE commercial_campaign（valid_until 未过期）
  ├─ 仅附加，不改变已选定的提案与排序
  ├─ 填充 is_commercial + commercial_disclosures
  ↓
输出 ExperienceProposal（含商业披露，不影响自然排序）
```

### 关键约束

1. **候选资格完全忽略商业字段**：Opportunity Discovery 阶段不得读取 commercial_campaign / commercial_attribution
2. **自然排序完全忽略商业字段**：Proposal Composer / Life Curator 不得读取商业字段
3. **只有自然入选的候选才允许附加商业归因**：商业归因在 Life Curator 选定最终提案后附加，不可反向影响自然决策
4. **商业元数据在自然决策后附加**：Commercial Attribution 是 Pipeline 末端的「附加层」，不是决策层
5. **可审计**：commercial_campaign 保留 sponsor_name / sponsor_type / commercial_type / valid_from / valid_until / source，commercial_attribution 保留 attributed_at / is_disclosed
6. **不付费也会推荐检验**：实现时必须有测试用例验证「移除所有 commercial_campaign 后，自然排序结果不变」

## Reason

- **产品驱动**：产品宪法 §十.4「商业合作不得暗中改变提案排序」「推荐信任不可出售」；检验标准「如果这个商家不付费，Solo 还会不会把它推荐给这个用户？」要求商业字段物理隔离于自然决策
- **领域驱动**：商业关系具有时间、活动、渠道和合同范围，是独立领域概念，不应作为 Location 的静态属性污染地点模型；一个地点可能同时或先后存在多个商业关系；居家或非地点型提案也可能包含商业内容
- **审计驱动**：商业内容必须可审计（披露、主体、类型、有效期、来源），独立模型才能完整承载审计字段
- **"通过同一适配度校验" ≠ "不付费也会推荐"**：原方案 A 仅要求商业提案通过适配度校验，但通过校验不等于自然入选；方案 C 要求先自然入选再附加归因，真正满足宪法检验

## Impact

### 影响模块

- AI Platform：Pipeline 末端新增 Commercial Attribution 附加层；Opportunity Discovery / Proposal Composer / Life Curator 不得读取商业字段
- Today：ExperienceProposal 输出含 is_commercial / commercial_disclosures
- Explore：location 表**不新增**商业字段（原方案 A 的 sponsor_id / commercial_type 已废弃）

### 需要修改的文档

- DATABASE_DESIGN.md：新增 commercial_campaign / commercial_attribution 表（第 4 步）；location 表不新增商业字段
- ARCHITECTURE.md：§18 外部集成新增「商业归因在自然决策后附加，不影响自然排序」（第 4 步）；§7 AI Pipeline 新增 Commercial Attribution 附加层
- PROJECT_CONTEXT.md：§8 产品边界已在 v1.3 完成
- CODE_RULES.md：§9 API 规范新增 is_commercial / commercial_disclosures 字段要求（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_commercial_campaign_attribution.sql`（创建 2 张表）
- 新建 CommercialCampaign / CommercialAttribution Entity / Repository
- Sprint 5 AI Platform：Commercial Attribution 附加层逻辑（自然决策后查询 ACTIVE campaign 并附加）
- Sprint 5：ExperienceProposal 输出含 is_commercial / commercial_disclosures
- 前端：商业提案显著标识 UI（赞助标签 + 披露文案列表）
- 测试：移除所有 commercial_campaign 后自然排序不变的测试用例

### 是否影响现有数据

- commercial_campaign / commercial_attribution 表：新建，无影响
- location 表：**不新增**商业字段，现有数据不受影响

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 commercial_campaign / commercial_attribution 表设计
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：Commercial Attribution 附加层逻辑 + ExperienceProposal 输出
4. 前端：商业提案标识 UI

### Follow-up ADR

- 无（本 ADR 完整定义商业推荐边界与归因模型）
- 未来若需引入更复杂的商业模式（如订阅制、竞价排序），新建 ADR；竞价排序与本 ADR「自然排序忽略商业字段」根本冲突，必须经人工产品负责人审核是否修订产品宪法

### 验证方式

- commercial_campaign / commercial_attribution 表 migration 执行成功
- Opportunity Discovery / Proposal Composer / Life Curator 代码不读取商业字段（代码审查 + 测试）
- Commercial Attribution 附加层在自然决策完成后执行（测试用例覆盖）
- 移除所有 commercial_campaign 后，自然排序结果不变（宪法检验测试用例）
- Life Curator 不因 is_commercial 提升或降级置信度（测试用例覆盖）
- ExperienceProposal 输出含 is_commercial / commercial_disclosures
- 前端商业提案显著标识（赞助标签 + 披露文案）
- commercial_campaign 过期后（valid_until < now）不附加归因（测试用例覆盖）
