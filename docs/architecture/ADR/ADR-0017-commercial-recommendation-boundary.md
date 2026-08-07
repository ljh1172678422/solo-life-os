# ADR-0017: Commercial Recommendation Boundary

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.4 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §8 决策。

---

## Decision

建立商业推荐边界，确保商业合作不影响自然排序，商业内容必须明确披露，推荐信任不可出售。

## Boundary Rules（产品宪法 §十.4）

1. **商业合作不得暗中改变提案排序**：赞助、佣金、商家合作关系不得影响 Opportunity Discovery 的候选机会可信度计算
2. **商业内容必须明确标识**：任何赞助、佣金或合作关系的提案必须在用户端显著标识
3. **商业内容必须经适配度校验**：商业提案必须经过 Proposal Composer 的五要素适配度校验，不得绕过
4. **推荐信任不可出售**：Life Curator 的置信度门控不可因商业合作降级

## Data Model

### location 表扩展

现有 location 表新增字段（与 ADR-0015 location 扩展合并）：

| 字段 | 类型 | 说明 |
|---|---|---|
| sponsor_id | BIGINT | 赞助商 ID（nullable，null 表示非商业地点） |
| commercial_type | VARCHAR(20) | NONE / SPONSORED / AFFILIATE / PARTNERSHIP（默认 NONE） |

### ExperienceProposal 输出扩展

ExperienceProposal 输出新增字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| is_sponsored | BOOLEAN | 是否商业提案（默认 false） |
| sponsor_disclosure | STRING | 商业披露文案（is_sponsored=true 时必填，如「该地点由 XX 赞助」） |

## Pipeline Constraints

```
Opportunity Discovery
  ├─ 候选机会可信度计算（不受商业合作影响）
  ↓
Proposal Composer
  ├─ 五要素适配度校验（商业提案必须通过，不可绕过）
  ↓
Life Curator
  ├─ 置信度门控（不可因商业合作降级）
  ├─ 若 is_sponsored=true，必须附加 sponsor_disclosure
  ↓
输出 ExperienceProposal（含 is_sponsored + sponsor_disclosure）
```

### 关键约束

- Opportunity Discovery 计算 fact_confidence 时，source_weight 不受商业合作影响（OFFICIAL_API/MERCHANT_REPORTED/USER_REPORTED/INFERRED 仅反映数据可信度，不反映商业关系）
- 商业提案在 Proposal Composer 阶段必须与自然提案通过相同的五要素适配度校验，不合格的商业提案不输出
- Life Curator 不可因 is_sponsored=true 提升置信度等级
- 前端展示商业提案时必须显著标识（如「赞助」标签 + 披露文案）

## Impact

### 影响模块

- Explore：location 表新增 sponsor_id / commercial_type 字段
- AI Platform：Proposal Composer 校验商业提案，Life Curator 不降级门控
- Today：ExperienceProposal 输出含 is_sponsored / sponsor_disclosure

### 需要修改的文档

- DATABASE_DESIGN.md：location 表新增 sponsor_id / commercial_type（第 4 步，与 ADR-0015 合并）
- ARCHITECTURE.md：§18 外部集成新增「商业内容必须经 Proposal Composer 适配度校验，不得绕过」（第 4 步）
- PROJECT_CONTEXT.md：§8 产品边界已在 v1.3 完成
- CODE_RULES.md：§9 API 规范新增 is_sponsored 字段要求（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__alter_location_add_commercial_fields.sql`
- location Entity 新增 sponsorId / commercialType 字段
- Sprint 5 AI Platform：Proposal Composer 商业提案校验逻辑
- Sprint 5：ExperienceProposal 输出含 is_sponsored / sponsor_disclosure
- 前端：商业提案显著标识 UI

### 是否影响现有数据

- location 表：已有数据，新增字段设 nullable + 默认值（commercial_type 默认 NONE），不破坏现有数据

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，location 表新增商业字段
2. 第 7 步：新建 migration + Entity 字段扩展
3. Sprint 5：Proposal Composer 商业校验逻辑 + ExperienceProposal 输出
4. 前端：商业提案标识 UI

### Follow-up ADR

- 无（本 ADR 完整定义商业推荐边界）
- 未来若需引入更复杂的商业模式（如订阅制），新建 ADR

### 验证方式

- location 表新增字段 migration 执行成功，现有数据 commercial_type 默认 NONE
- Proposal Composer 商业提案校验逻辑实现并测试
- 商业提案不可绕过适配度校验（测试用例覆盖）
- Life Curator 不因 is_sponsored 提升置信度（测试用例覆盖）
- ExperienceProposal 输出含 is_sponsored / sponsor_disclosure
- 前端商业提案显著标识
