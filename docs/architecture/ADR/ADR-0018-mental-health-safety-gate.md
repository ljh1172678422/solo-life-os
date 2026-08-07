# ADR-0018: Mental Health Boundary and Immediate Safety Support Flow

Date: 2026-08-07

Status: Accepted（产品安全边界）；分类器规则与阈值为 Implementation Pending

> 本 ADR 基于产品宪法 [Solo_Product_Principles.md](../../Solo_Product_Principles.md) v1.1 §十.3、§十三 与 [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) v1.3 §13.3 决策。
> 产品宪法 §十.3：「Solo 不诊断抑郁、焦虑、社交障碍或其他心理健康问题，也不把体验提案描述为治疗方案。」「涉及即时安全风险时，应优先进入安全支持流程，而不是继续推荐活动。」

---

## Decision

本 ADR 分为两层：

- **产品安全边界（Accepted）**：确立 Safety Gate 在 AI Pipeline 中的前置位置、停止普通推荐的产品行为、不诊断不治疗的责任边界。这一层是产品宪法要求的直接落地，不依赖具体分类器实现。
- **分类器规则与阈值（Implementation Pending）**：具体的信号识别规则、触发阈值、误触发/漏触发策略，须经专业心理健康安全评审、离线评测、发布 Gate 验证后才可实施。在通过专业评审前，MVP 仅处理用户本轮主动输入的文本信号。

## 产品安全边界（Accepted）

### Safety Gate 定位

在 AI Pipeline 中前置一层安全检测（ADR-0020 调用链最前置），当识别到心理健康边界信号或即时安全风险信号时，**停止普通体验推荐**，转入安全支持流程。Safety Gate 失败安全（fail-safe）：检测不确定时优先保护用户。Solo 不诊断、不治疗，只做识别、降级与转介。

### MVP 范围（仅用户本轮主动输入）

MVP 阶段 Safety Gate **仅处理用户本轮主动输入的文本信号**（如 Mood 主动输入、Assistant 主动查询中的用户文本）。**不使用行为信号**（如使用频率、停留时长、位置模式等），因为行为信号与产品宪法 §十.2「禁止隐性监控」存在冲突，且行为模式识别须另行授权和 ADR。

### Level 1：心理健康边界信号（长期、广泛兴趣丧失 + 明显功能变化）

触发条件（**全部满足**才触发，且**仅基于用户主动表达**）：

- 用户主动表达**长期、广泛**的兴趣丧失（非针对单一活动，而是对生活整体）
- **且**用户主动伴随表达明显的睡眠 / 食欲 / 工作能力变化

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
- 用户输入中检测到危机性关键词或语义（保守匹配，宁可误触发）

**Level 2 动作**：
1. **立即停止**所有普通体验推荐
2. 优先进入安全支持流程：展示危机干预资源（紧急热线、紧急求助渠道）
3. 不输出任何体验提案（no_proposal，理由为 safety_gate）
4. 不诊断、不治疗，只转介
5. 记录触发事件供审计（见 §safety_event_log 敏感性说明）

## 分类器规则与阈值（Implementation Pending）

> 以下规则与阈值**尚未达到 Accepted 标准**，须通过专业评审 Gate 后方可实施。

### 为什么 Implementation Pending

心理健康安全流程属高风险场景，精确触发条件、误触发策略、保留期均需要：
- **专业心理健康安全评审**：由心理健康专业人员及有亲身经历的人共同参与设计（对齐 WHO 2026 年指导：心理健康 AI 应由专业人员及有亲身经历的人共同设计，并建立危机转介和问责框架）
- **离线评测**：在真实/模拟数据集上评测误触发率（false positive）和漏触发率（false negative）
- **发布 Gate**：专业评审通过 + 离线评测达标后才可上线
- **持续监控**：上线后持续监控误触发/漏触发率，定期复评

### 待专业评审确认的事项

| 事项 | 当前状态 | 待确认 |
|---|---|---|
| Level 1/2 精确触发阈值 | Implementation Pending | 专业评审确认关键词/语义匹配规则与阈值 |
| 误触发策略 | Implementation Pending | 专业评审确认误触发后的用户恢复流程 |
| 漏触发兜底 | Implementation Pending | 专业评审确认漏触发时的兜底机制 |
| safety_event_log 保留期 | Implementation Pending | 专业评审确认保留期限（当前暂定 365 天，须专业评审确认） |
| 行为模式识别（未来） | 不在 MVP 范围 | 须另行授权 + 新建 ADR，且经专业评审 |

## Safety Gate 在 AI Pipeline 中的位置

```
外部请求
  ↓
Safety Gate（AI Pipeline 最前置，ADR-0020）
  ├─ Level 2 信号 → 立即停止推荐，输出安全支持流程
  ├─ Level 1 信号 → 停止普通推荐，温和提供专业支持入口
  ├─ 无信号 → 正常进入 AI Pipeline（Router → Context Builder → 角色...）
  ↓
（仅无信号时）Router → Context Builder → Orchestrator 编排角色 → 输出
```

### 关键约束

1. **前置检测**：Safety Gate 在 Router 之前执行（ADR-0020 调用链），任何 Level 2 信号直接短路 Pipeline
2. **失败安全（fail-safe）**：检测不确定时，按 Level 1 处理（保守降级）；明确危机时按 Level 2 处理
3. **不诊断不治疗**：Safety Gate 只做「识别 → 降级 → 转介」，不做心理健康诊断，不输出治疗方案
4. **不固化**：一次安全事件不得永久解释为用户偏好或特征（对齐产品宪法 §十.1）
5. **责任边界**：Solo 不是心理健康工具，安全支持流程的目的是转介专业资源，不替代专业帮助
6. **MVP 仅主动输入**：MVP 仅处理用户本轮主动输入的文本信号，不使用行为信号

## 责任边界

| 边界 | Solo 做什么 | Solo 不做什么 |
|---|---|---|
| 诊断 | 不做 | 不诊断抑郁、焦虑、社交障碍等 |
| 治疗 | 不做 | 不把体验提案描述为治疗方案 |
| 识别 | MVP 仅识别用户主动输入中的 Level 1/2 信号 | 不使用行为信号；不对单次情绪低落做判断 |
| 降级 | 停止普通推荐；温和提供专业支持入口 | 不强制阻断用户（Level 1）；Level 2 停止推荐 |
| 转介 | 提供心理援助热线、专业咨询渠道 | 不替代专业帮助 |
| 数据 | 审计日志（见敏感性说明），不作为偏好永久固化 | 不把安全事件作为用户画像永久存储 |

## safety_event_log 敏感性说明

`safety_event_log` 保存 `user_id + trigger_level + trigger_signal`，**是可关联用户的高度敏感记录**，不是「脱敏日志」。处置规则：

- **访问控制**：仅限人工安全审计访问，不向 AI 推荐系统暴露，不用于推荐排序
- **不作为偏好**：不得作为 ai_memory 的 PREFERENCE / INFERENCE 存储（ADR-0019）
- **保留期限**：暂定 365 天，**须经专业评审确认**；过期后硬删除（不是软删除）
- **用户知情**：用户可得知安全事件被记录（透明原则），但为保护用户安全不提供自助删除（防止危机用户删除求助痕迹）；用户可向人工申请删除
- **最小化**：仅记录 trigger_level / trigger_signal / triggered_at / action_taken，不记录用户输入原文

### 新增表：`safety_event_log`（高度敏感审计日志）

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

> 注：不记录用户输入原文（最小化原则）。访问控制仅限人工安全审计。

## 危机干预资源管理

### 资源配置要求

- **按地区/语言配置**：危机干预资源（紧急热线、求助渠道）按地区和语言配置
- **版本管理**：资源内容有版本号，更新时记录变更历史
- **责任归属**：资源内容由人工产品负责人维护，AI Agent 不得自行修改危机干预资源
- **兜底机制**：当地区/语言资源未配置或加载失败时，展示默认兜底资源（如国际通用危机热线），并记录资源缺失告警

## Reason

- **产品驱动**：产品宪法 §十.3 明确要求「涉及即时安全风险时，应优先进入安全支持流程」
- **MVP 约束**：行为信号与产品宪法 §十.2「禁止隐性监控」冲突；MVP 仅处理用户主动输入，行为模式识别须另行授权 + ADR
- **安全驱动**：心理健康安全流程属高风险场景，分类器规则须经专业评审、离线评测、发布 Gate，不能直接 Accepted
- **WHO 指导**：WHO 2026 年指导强调心理健康 AI 应由专业人员及有亲身经历的人共同设计，并建立危机转介和问责框架
- **责任边界驱动**：Solo 不是心理健康工具，必须明确「识别 → 降级 → 转介」边界

## Impact

### 影响模块

- AI Platform：AI Pipeline 最前置新增 Safety Gate（ADR-0020 调用链）；MVP 仅处理用户主动输入文本
- Mood：State Understanding 角色接收的用户主动输入是 Safety Gate 的信号源
- Today：Safety Gate 触发时，ExperienceProposal 输出 no_proposal（理由 safety_gate）
- 通知：Safety Gate 触发时不发送普通主动提醒

### 需要修改的文档

- ARCHITECTURE.md：§7 AI Pipeline 新增 Safety Gate 前置层（ADR-0020 调用链）（第 4 步）；§17 Security Boundary 新增心理健康安全边界
- DATABASE_DESIGN.md：新增 safety_event_log 表（第 4 步）
- PROJECT_CONTEXT.md：§13.3 已确立边界
- CODE_RULES.md：§6 异常处理新增 safety_gate 短路逻辑（第 6 步）

### 需要新增/修改的代码

- 新建 migration：`V20260807_xxx__create_safety_event_log.sql`
- 新建 SafetyEventLog Entity / Repository（访问控制仅限人工审计）
- Sprint 5 AI Platform：SafetyGateService（MVP 仅用户主动输入文本信号识别 + 分级 + Pipeline 短路）
- 危机干预资源配置（按地区/语言，含兜底 + 版本管理）
- 测试：Level 1/2 触发、fail-safe、不固化偏好、Pipeline 短路、资源兜底

### 是否影响现有数据

- safety_event_log 表：新建，无影响

## Migration / Follow-up

### Migration 步骤

1. 第 4 步：更新 DATABASE_DESIGN.md，新增 safety_event_log 表设计
2. 第 7 步：新建 migration + Entity + Repository
3. Sprint 5：SafetyGateService MVP 实现（仅用户主动输入）+ 危机干预资源配置 + 测试

### Follow-up ADR

- 分类器规则与阈值经专业评审后，新建 ADR 确认精确触发条件、误触发策略、保留期
- 未来若需引入行为模式识别（如使用频率、位置模式），必须另行授权 + 新建 ADR + 专业评审

### 验证方式

- safety_event_log 表 migration 执行成功
- Safety Gate 在 Router 之前执行（ADR-0020 调用链顺序测试）
- Level 2 信号立即短路 Pipeline，输出 no_proposal（理由 safety_gate）
- Level 1 信号停止普通推荐，温和提供专业支持入口
- MVP 仅处理用户主动输入文本（不使用行为信号，代码审查 + 测试）
- fail-safe：不确定信号按 Level 1 处理（保守降级测试）
- safety_event_log 不作为 ai_memory PREFERENCE / INFERENCE 存储
- safety_event_log 不用于推荐排序（访问控制 + 代码审查）
- safety_event_log 不记录用户输入原文（最小化原则）
- 危机干预资源按地区/语言正确展示；资源缺失时展示兜底资源
- 分类器规则与阈值标记为 Implementation Pending（未通过专业评审前不实施）
