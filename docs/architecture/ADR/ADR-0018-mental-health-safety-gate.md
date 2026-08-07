# ADR-0018: Mental Health Boundary and Immediate Safety Support Flow

Date: 2026-08-07

Status: Accepted

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.3、§十三 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §13.3 决策。
> 产品宪法 §十.3：「Solo 不诊断抑郁、焦虑、社交障碍或其他心理健康问题，也不把体验提案描述为治疗方案。」「涉及即时安全风险时，应优先进入安全支持流程，而不是继续推荐活动。」

---

## Decision

建立 **Safety Gate（安全门控）**：在 AI Pipeline 中前置一层安全检测，当识别到心理健康边界信号或即时安全风险信号时，**停止普通体验推荐**，转入安全支持流程。Safety Gate 失败安全（fail-safe）：检测不确定时优先保护用户，宁可误触发安全流程也不继续推荐活动。Solo 不诊断、不治疗，只做识别、降级与转介。

## Trigger Conditions（两类信号，分级处理）

### Level 1：心理健康边界信号（长期、广泛兴趣丧失 + 明显功能变化）

触发条件（**全部满足**才触发，避免单次情绪低落误判）：

- 用户主动表达**长期、广泛**的兴趣丧失（非针对单一活动，而是对生活整体）
- **且**伴随明显的睡眠 / 食欲 / 工作能力变化（用户主动表达或行为信号）

**Level 1 动作**：
1. 停止用普通体验建议解释一切
2. 温和提供专业支持入口（如心理援助热线、专业咨询渠道，按地区配置）
3. 不诊断、不标签化、不使用治疗性语言
4. 不把体验提案描述为治疗方案
5. 用户可关闭安全提示并继续使用（不强制阻断）

### Level 2：即时安全风险信号（自伤 / 自杀 / 危机）

触发条件（**任一满足**即触发，保守原则）：

- 用户主动表达自伤、自杀意图或计划
- 用户表达即刻的危险情境（如正在受到伤害）
- 检测到危机性关键词或语义（保守匹配，宁可误触发）

**Level 2 动作**：
1. **立即停止**所有普通体验推荐
2. 优先进入安全支持流程：展示危机干预资源（紧急热线、紧急求助渠道）
3. 不输出任何体验提案（no_proposal，理由为 safety_gate）
4. 不诊断、不治疗，只转介
5. 记录触发事件供审计（脱敏，不作为偏好永久固化）

## Safety Gate 在 AI Pipeline 中的位置

```
用户输入 / 状态信号
  ↓
Safety Gate（AI Pipeline 最前置）
  ├─ Level 2 信号 → 立即停止推荐，输出安全支持流程
  ├─ Level 1 信号 → 停止普通推荐，温和提供专业支持入口
  ├─ 无信号 → 正常进入 AI Pipeline
  ↓
（仅无信号时）Opportunity Discovery → Proposal Composer → Life Curator → 输出
```

### 关键约束

1. **前置检测**：Safety Gate 在 Opportunity Discovery 之前执行，任何 Level 2 信号直接短路 Pipeline
2. **失败安全（fail-safe）**：检测不确定时，按 Level 1 处理（保守降级）；明确危机时按 Level 2 处理
3. **不诊断不治疗**：Safety Gate 只做「识别 → 降级 → 转介」，不做心理健康诊断，不输出治疗方案
4. **不固化**：一次安全事件不得永久解释为用户偏好或特征（对齐产品宪法 §十.1「不能把一次行为永久解释为用户偏好」）
5. **责任边界**：Solo 不是心理健康工具，安全支持流程的目的是转介专业资源，不替代专业帮助
6. **可审计**：Level 1/2 触发事件记录脱敏审计日志，供人工复核

## 责任边界

| 边界 | Solo 做什么 | Solo 不做什么 |
|---|---|---|
| 诊断 | 不做 | 不诊断抑郁、焦虑、社交障碍等 |
| 治疗 | 不做 | 不把体验提案描述为治疗方案 |
| 识别 | 识别长期兴趣丧失 + 功能变化信号；识别即时危机信号 | 不对单次情绪低落做判断 |
| 降级 | 停止普通推荐；温和提供专业支持入口 | 不强制阻断用户（Level 1）；Level 2 停止推荐 |
| 转介 | 提供心理援助热线、专业咨询渠道 | 不替代专业帮助 |
| 数据 | 脱敏审计日志，不作为偏好永久固化 | 不把安全事件作为用户画像永久存储 |

## Data Model

### 新增表：`safety_event_log`（脱敏审计日志，不作为偏好存储）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | 用户 ID（逻辑关联，无物理 FK） |
| trigger_level | VARCHAR(10) | L1 / L2 |
| trigger_signal | VARCHAR(50) | 信号类型（ANHEDONIA_LONG_TERM / CRISIS_KEYWORD / SELF_HARM / OTHER） |
| triggered_at | TIMESTAMP | 触发时间 |
| action_taken | VARCHAR(50) | 采取动作（PROFESSIONAL_SUPPORT / CRISIS_INTERVENTION / NO_PROPOSAL） |
| resolved_at | TIMESTAMP | 用户关闭安全提示时间（nullable） |
| created_time | TIMESTAMP | |

> 注：safety_event_log 是**审计日志**，不是用户偏好。不得用于推荐排序，不得作为 ai_memory 的 PREFERENCE 永久固化。保留期限由 ADR-0019 数据治理决定。

### 不新增的字段

- ai_memory **不存储** safety_event_log 关联记录作为 PREFERENCE（防止一次安全事件永久影响推荐）
- 用户画像 **不标记** 「心理健康风险用户」等标签

## Reason

- **产品驱动**：产品宪法 §十.3 明确要求「涉及即时安全风险时，应优先进入安全支持流程，而不是继续推荐活动」；六个 ADR（0012~0017）未定义 Safety Gate、失败安全和责任边界
- **架构约束**：PROJECT_CONTEXT v1.3 §13.3 已确立心理健康边界；Safety Gate 必须在 AI Pipeline 前置，否则普通推荐会越过边界
- **失败安全驱动**：心理健康信号天然不确定，必须 fail-safe（宁可误触发也不漏触发），不能用确定性逻辑处理
- **责任边界驱动**：Solo 不是心理健康工具，必须明确「识别 → 降级 → 转介」边界，避免演变为诊断或治疗工具

## Impact

### 影响模块

- AI Platform：AI Pipeline 最前置新增 Safety Gate 检测层；Level 2 信号短路 Pipeline
- Mood：State Understanding 角色接收的用户主动输入是 Safety Gate 的重要信号源
- Today：Safety Gate 触发时，ExperienceProposal 输出 no_proposal（理由 safety_gate）
- 通知：Safety Gate 触发时不发送普通主动提醒

### 需要修改的文档

- ARCHITECTURE.md：§7 AI Pipeline 新增 Safety Gate 前置层（第 4 步）；§17 Security Boundary 新增心理健康安全边界
- DATABASE_DESIGN.md：新增 safety_event_log 表（第 4 步）
- PROJECT_CONTEXT.md：§13.3 已确立边界，本 ADR 落地实现方式
- CODE_RULES.md：§6 异常处理新增 safety_gate 短路逻辑（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_safety_event_log.sql`
- 新建 SafetyEventLog Entity / Repository
- Sprint 5 AI Platform：SafetyGateService（信号识别 + 分级 + Pipeline 短路）
- 危机干预资源配置（按地区，脱敏）
- 测试：Level 1/2 触发、fail-safe、不固化偏好、Pipeline 短路

### 是否影响现有数据

- safety_event_log 表：新建，无影响
- ai_memory：不存储安全事件作为 PREFERENCE，无影响

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 safety_event_log 表设计
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：SafetyGateService 实现 + 危机干预资源配置 + 测试

### Follow-up ADR

- 无（本 ADR 完整定义 Safety Gate 触发条件、失败安全、责任边界）
- 未来若需引入更精细的心理健康信号识别（如行为模式分析），新建 ADR；行为模式分析必须经人工产品负责人审核，避免演变为隐性监控

### 验证方式

- safety_event_log 表 migration 执行成功
- Safety Gate 在 Opportunity Discovery 之前执行（Pipeline 顺序测试）
- Level 2 信号立即短路 Pipeline，输出 no_proposal（理由 safety_gate）
- Level 1 信号停止普通推荐，温和提供专业支持入口
- fail-safe：不确定信号按 Level 1 处理（保守降级测试）
- safety_event_log 不作为 ai_memory PREFERENCE 存储（不固化偏好测试）
- safety_event_log 不用于推荐排序（代码审查 + 测试）
- 不诊断、不标签化、不使用治疗性语言（文案审查）
- 危机干预资源按地区正确展示
