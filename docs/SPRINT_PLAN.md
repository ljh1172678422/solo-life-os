# Solo Life OS Sprint 规划

Version: 2.2

Status: Planning

Last Update: 2026-07-28


> 本文档定义 Solo Life OS 的迭代计划，所有 AI Agent 与人类开发者按 Sprint 执行。
> 与 PROJECT_CONTEXT §9、ARCHITECTURE §6、DATABASE_DESIGN §4、AGENTS §7 完全对齐。


---

# 1. Sprint Strategy


- Sprint 按 Module（模块）组织，不按页面组织（页面可变，Module 稳定）
- 每个 Sprint 必须有 Sprint Goal + Deliverables + DoD + Risk
- 每个 Sprint 明确 Owner Agent / Reviewer Agent
- 每个 Sprint 明确依赖（Depends），避免开发顺序错误
- 术语统一使用 Module（与 ARCHITECTURE §5 一致），禁止 Service


---

# 2. Sprint Roadmap


```
Sprint 0  工程初始化           → M1 基础框架
Sprint 1  User Module          ┐
Sprint 2  Today Module         ├ M2 AI MVP
Sprint 3  Explore Module       │
Sprint 4  Mood Module          ┘
Sprint 5  AI Platform           ┐
Sprint 6  Growth Module         ├ M3 生活闭环
Sprint 7  Community Module      │
Sprint 8  Story Module         ┘
```


---

# 3. Sprint 0：工程初始化


## Sprint Goal


建立多端开发环境，使后续所有 Sprint 可启动编码。


## Deliverables


- uni-app 工程初始化（H5 / 小程序 / App 三端配置）
- Spring Boot 工程初始化（Modular Monolith 包结构）
- PostgreSQL + Redis 本地环境
- Vector DB Adapter Interface（仅接口定义，实例部署延后至 Sprint 5）
- 统一返回格式 + 全局异常处理器
- 日志框架（含 traceId 透传）
- CI/CD 基础骨架


## Agents


- Owner: Backend Agent + Frontend Agent
- Reviewer: Architecture Agent


## Depends


无（首个 Sprint）


## Risk


- 三端配置复杂度高 → 先只跑通 H5，App/小程序后续 Sprint 补
- Vector DB 选型未定 → 写 ADR-0005 决定（pgvector / Milvus / Qdrant），Sprint 0 仅定义 Adapter 接口


## DoD


- [ ] uni-app 可启动 H5 首页
- [ ] Spring Boot 可启动并访问 /health
- [ ] 数据库可连接
- [ ] CI 通过


---

# 4. Sprint 1：User Module


## Sprint Goal


完成用户系统 MVP，支持注册 / 登录 / 资料维护 / 偏好设置。


## Deliverables


- Migration：user / user_preference / tag 表
- Repository：UserRepository / UserPreferenceRepository / TagRepository
- Domain：User / Preference / Tag Entity
- Application：UserApplicationService
- Controller：UserController（注册 / 登录 / 资料 / 偏好）
- DTO：UserRegisterDTO / UserLoginDTO / UserDTO / PreferenceDTO
- 异常：AuthException / BusinessException
- 前端：登录页 / 资料页（原 Page22 / Page23）
- 测试：Domain 单元测试 + Controller API 测试


## Agents


- Owner: Backend Agent
- Reviewer: Architecture Agent + QA Agent


## Depends


Sprint 0


## Risk


- 登录凭证（JWT）策略未定 → Sprint 0 末尾写 ADR-0006
- 第三方登录暂不实现，仅邮箱 / 手机号


## DoD


- [ ] Migration 已执行
- [ ] API 通过 Swagger 文档可见
- [ ] 单元测试覆盖率 > 80%（Domain）
- [ ] 前端可完成注册 → 登录 → 设置偏好闭环
- [ ] 文档更新（CHANGELOG / AI_CHANGELOG）


---

# 5. Sprint 2：Today Module


## Sprint Goal


完成 Today Module MVP，支持 AI 生成每日计划（Planner Agent 用 Mock Memory）。


## Deliverables


- Migration：daily_plan / activity 表
- Repository / Domain / Application / Controller
- DTO：DailyPlanDTO / ActivityDTO
- 前端：今日页（Page01 / Page02 / Page03 / Page05）
- AI：Planner Agent 骨架（输入：时间 / 地点 / 天气 / 心情 / 偏好）
- 测试


## Agents


- Owner: Backend Agent
- Support: Frontend Agent + AI Agent
- Reviewer: Architecture Agent


## Depends


Sprint 1（User / Preference）


## Risk


- Planner Agent 依赖 Memory，Sprint 5 才实现 → 本 Sprint 用 Mock Memory
- 天气数据需外部 API → 通过 Adapter 层调用，失败降级


## DoD


- [ ] Migration 已执行
- [ ] 用户可看到 AI 生成的今日计划
- [ ] 计划可动态调整
- [ ] Planner Agent 接口定义完成（实现可 Mock）
- [ ] 测试通过


---

# 6. Sprint 3：Explore Module


## Sprint Goal


完成 Explore Module MVP，支持地图探索 / 地点推荐 / 收藏。


## Deliverables


- Migration：location / favorite 表（不含 activity，Activity Owner 归 Today）
- Repository / Domain / Application / Controller
- DTO：LocationDTO / FavoriteDTO
- 前端：地图页 / 地点详情 / 收藏列表（Page06-09）
- AI：Recommendation Agent 骨架
- Adapter：地图 SDK（高德 / 腾讯，写 ADR-0007 决定）
- 测试


## Agents


- Owner: Backend Agent
- Support: Frontend Agent + AI Agent
- Reviewer: Architecture Agent


## Depends


Sprint 1（User / Preference）


## Risk


- 地图 SDK 选型 → 写 ADR-0007
- 推荐算法依赖 Memory → 用 Mock
- Explore 不创建 activity 表（Owner 是 Today，需跨模块经 Domain API 调用）


## DoD


- [ ] 地图可显示用户附近地点
- [ ] 用户可收藏地点（UNIQUE 约束生效）
- [ ] Recommendation Agent 接口定义完成
- [ ] 测试通过


---

# 7. Sprint 4：Mood Module


## Sprint Goal


完成 Mood Module MVP，支持心情记录 / 趋势查看 / AI 情绪洞察。


## Deliverables


- Migration：mood_record 表
- Repository / Domain / Application / Controller
- DTO：MoodRecordDTO / MoodTrendDTO
- 前端：心情记录页 / 趋势页（Page10-13）
- AI：Emotion Agent 骨架
- 事件：mood.recorded 发布
- 测试


## Agents


- Owner: Backend Agent
- Support: Frontend Agent + AI Agent
- Reviewer: Architecture Agent


## Depends


Sprint 1（User）


## Risk


- 情绪数据敏感 → 必须遵守 PROJECT_CONTEXT §13 隐私原则
- Emotion Agent 禁止心理诊断


## DoD


- [ ] 用户可记录心情
- [ ] 趋势图可显示
- [ ] mood.recorded 事件可被订阅
- [ ] 日志脱敏生效
- [ ] 测试通过


---

# 8. Sprint 5：AI Platform


## Sprint Goal


完成 AI Platform 核心，替换前 4 个 Sprint 的 Mock，建立真实 Memory + Router + Agent 体系。


## Deliverables


- Migration：ai_memory + ai_conversation 表
- Memory Layer 实现（含 Vector DB 写入）
- Conversation Layer 实现（短期对话上下文，与 Memory 长期记忆互补）
- Context Builder 实现
- Agent Router 实现
- Planner / Recommendation / Emotion / Story / Assistant Agent 实现
- LLM Provider 抽象层（写 ADR-0008 选型 GPT / GLM / Claude）
- 替换 Sprint 2/3/4 的 Mock
- 测试


## Agents


- Owner: AI Agent
- Support: Backend Agent
- Reviewer: Architecture Agent


## Depends


Sprint 2 / 3 / 4（需要被替换的 Mock）


## Risk


- LLM 调用延迟与成本 → 必须满足 NFR（P95 < 5s）
- Prompt 漂移 → 遵守 AGENTS.md §11，所有 Prompt 经 PR
- Memory 数据量增长 → 分页 + 重要度排序


## DoD


- [ ] Memory 可写入 Vector DB 并检索
- [ ] Router 可路由到 5 个 Agent
- [ ] Planner / Recommendation / Emotion 不再依赖 Mock
- [ ] LLM Provider 可切换（至少 2 个实现）
- [ ] 测试通过


---

# 9. Sprint 6：Growth Module


## Sprint Goal


完成 Growth Module MVP，支持目标 / 习惯 / 成长统计。


## Deliverables


- Migration：goal 表
- Repository / Domain / Application / Controller
- DTO：GoalDTO / GoalProgressDTO
- 前端：目标页 / 习惯页 / 成长统计页
- 事件：goal.progressed 发布
- 测试


## Agents


- Owner: Backend Agent
- Support: Frontend Agent
- Reviewer: Architecture Agent


## Depends


Sprint 5（Memory，用于成长洞察）


## Risk


- 习惯打卡逻辑复杂 → MVP 仅支持每日打卡 + 连续天数


## DoD


- [ ] 用户可创建 / 进度更新目标
- [ ] 习惯可打卡
- [ ] goal.progressed 事件可被订阅
- [ ] 测试通过


---

# 10. Sprint 7：Community Module


## Sprint Goal


完成 Community Module MVP，支持活动发现 / 报名 / 交流。


## Deliverables


- Migration：community_event / registration 表（独立领域实体，不复用 activity）
- Repository / Domain / Application / Controller
- DTO：CommunityEventDTO / RegistrationDTO
- 前端：活动列表 / 活动详情 / 报名页
- Adapter：支付 SDK（写 ADR-0009）
- 测试


## Agents


- Owner: Backend Agent
- Support: Frontend Agent
- Reviewer: Architecture Agent


## Depends


Sprint 3（Explore，Location 复用）+ Sprint 6（Growth，用户体系成熟）


## Risk


- 支付合规 → MVP 仅支持免费活动，付费活动后续 Sprint
- community_event 是独立领域实体，禁止复用 activity 表（ADR-0011）
- Location 复用 Explore 模块（经 Domain API）


## DoD


- [ ] 活动可创建 / 报名
- [ ] Location 复用 Explore 模块（不重复建表）
- [ ] 测试通过


---

# 11. Sprint 8：Story Module


## Sprint Goal


完成 Story Module MVP，支持年度回顾 / AI 人生叙事。


## Deliverables


- Story Agent 实现（基于 Memory / Goal / Mood / Activity 聚合）
- Controller：StoryController（年度故事生成 / 查看）
- DTO：StoryDTO / StoryChapterDTO
- 前端：年度回顾页 / 故事详情页
- 测试


## Agents


- Owner: AI Agent
- Support: Backend Agent + Frontend Agent
- Reviewer: Architecture Agent


## Depends


Sprint 5（AI Platform）+ Sprint 6（Growth）+ Sprint 4（Mood）


## Risk


- 数据量不足时故事生成质量差 → 需至少 3 个月数据积累，MVP 阶段用占位文案
- Story Agent 禁止编造未发生事件


## DoD


- [ ] 可聚合用户全年 Memory / Goal / Mood / Activity
- [ ] 可生成分章节叙事
- [ ] 测试通过
- [ ] 隐私脱敏生效


---

# 12. Milestones


| Milestone | 范围 | 含义 |
|-----------|------|------|
| M1 基础框架 | Sprint 0–1 | 可注册登录的多端骨架 |
| M2 AI MVP | Sprint 2–5 | Today / Explore / Mood + AI Platform 真实运行 |
| M3 生活闭环 | Sprint 6–8 | Growth / Community / Story 形成长期资产 |


与 PROJECT_CONTEXT §9 产品发展阶段对应：


| Phase | Milestone |
|-------|----------|
| Phase 0 基础平台 | M1 |
| Phase 1 MVP | M2 |
| Phase 2 数据积累 | M3 |
| Phase 3 真实连接 | M3（Community 部分） |
| Phase 4 人生资产 | M3（Story 部分） |


---

# 13. Dependencies


```
Sprint 0
  ↓
Sprint 1 (User)
  ↓
  ├─ Sprint 2 (Today)     ─┐
  ├─ Sprint 3 (Explore)   │
  └─ Sprint 4 (Mood)      ┤
                           │
Sprint 5 (AI Platform)    ←┘  (替换 Mock)
  ↓
  ├─ Sprint 6 (Growth)
  ├─ Sprint 7 (Community) ← Sprint 3 + Sprint 6
  └─ Sprint 8 (Story)     ← Sprint 5 + 6 + 4
```


规则：


- 依赖未完成的 Sprint 不可启动
- 如需并行，被依赖模块的接口必须先定义（Mock 实现）


---

# 14. Definition of Done


每个 Sprint 完成必须满足以下全部条件，否则不算 Done：


## 代码层


- [ ] Migration 已执行且幂等
- [ ] Repository / Domain / Application / Controller 全部实现
- [ ] 遵守 Package Convention（ARCHITECTURE §19）
- [ ] 遵守 DTO/Entity 边界（CODE_RULES §5）
- [ ] 无 System.out.println，日志含 traceId


## 测试层


- [ ] Domain Service 单元测试覆盖率 > 80%
- [ ] Controller API 测试通过
- [ ] 不依赖外部真实服务（Mock / Testcontainers）


## 文档层


- [ ] docs/CHANGELOG.md 已更新
- [ ] docs/AI_CHANGELOG.md 已更新（含 Handoff 信息）
- [ ] docs/TASK_BOARD.md 状态已流转到 Done
- [ ] 涉及架构变更已写 ADR


## 架构层


- [ ] 未违反 ARCHITECTURE §1–§22 任何一条
- [ ] 未创建重复 Entity
- [ ] 未跨模块直连数据库
- [ ] AGENTS.md §9 自检清单全部通过


---

# 15. Sprint Lifecycle & Risk


## Sprint 生命周期


与 AGENTS.md §7 任务生命周期一致：


```
Planning
  ↓
Ready
  ↓
In Progress
  ↓
Review
  ↓
Done
  ↓
Archived
```


当前状态：Sprint 0 处于 Planning


## Risk 管理规则


每个 Sprint 必须在 Planning 阶段识别风险并记录：


- 技术风险（选型未定 / 性能 / 依赖）→ 写 ADR
- 数据风险（隐私 / 迁移）→ 遵守 PROJECT_CONTEXT §13
- 范围风险（功能蔓延）→ 严格遵守 Sprint Goal，新增功能进 Backlog
- 依赖风险（被依赖 Sprint 延期）→ 接口先行，实现 Mock


---

# 16. ADR Roadmap


ADR 生命周期与 Sprint 生命周期一致，禁止一次性批量创建。仅在对应 Sprint 启动时创建：


| ADR | 决策主题 | 创建 Sprint | 状态 | 备注 |
|-----|---------|-----------|------|------|
| ADR-0001 | 采用 Modular Monolith | 全局 | Accepted | 最高层架构约束 |
| ADR-0002 | 选择 PostgreSQL 作为主数据库（含 Redis / Vector DB / OSS 分层禁止项） | 全局 | Accepted | 存储分工 |
| ADR-0003 | AI Agent 统一经 Router 路由（含 Agent 不持有业务状态） | 全局 | Accepted | AI 架构核心 |
| ADR-0004 | MVP 阶段不使用微服务 | 全局 | Accepted | 与 ADR-0001 角度互补 |
| ADR-0005 | Vector DB Adapter Strategy（候选 pgvector / Milvus / Qdrant + Adapter 延迟绑定） | Sprint 0 | Proposed | Sprint 0 仅定接口边界 |
| ADR-0006 | JWT 策略 | Sprint 1 | Pending | User Module 启动时创建 |
| ADR-0007 | Map Provider Adapter（Provider Adapter Pattern，避免高德 / 腾讯硬绑定） | Sprint 3 | Pending | Explore 启动时创建 |
| ADR-0008 | LLM Provider Strategy（抽象层策略，不锁定具体 Provider） | Sprint 5 | Pending | AI Platform 启动时创建 |
| ADR-0009 | Payment Adapter（Community MVP 免费活动可延期） | Sprint 7 | Pending | Community 启动时创建 |
| ADR-0010 | Tag Ownership（User Module vs Shared Kernel） | Sprint 0 | Proposed | 领域边界争议需 Sprint 0 提前决策 |
| ADR-0011 | Activity Owner 归 Today，CommunityEvent 独立 | Sprint 0 | Accepted | 已是架构事实，直接 Accepted |


规则：


- ADR 必须在对应 Sprint 启动时创建，禁止提前批量创建 ADR-0006~0009
- 仅 Sprint 0 即将依赖的领域边界 ADR（ADR-0010 / ADR-0011）可提前到 Sprint 0 创建
- ADR-0011 因评审已解决（Activity 归 Today，CommunityEvent 独立），直接 Accepted
- ADR 状态变更必须经 Architecture Agent 评审
- Accepted 后的 ADR 才能据此开发
- ADR 被否决必须新建 ADR 替代，禁止直接删除


---

# 17. Version History


| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-28 | 初始版本，按页面划分 9 个 Sprint |
| v2.0 | 2026-07-28 | 全量升级：按 Module 组织；增加 Goal / DoD / Depends / Agents / Risk / Milestone / Lifecycle；统一术语 Module；Sprint 5 改名 AI Platform |
| v2.1 | 2026-07-28 | 修正 Activity Owner 冲突（归 Today）；Sprint 7 改用 community_event 独立领域实体；Sprint 0 Vector DB 延后为 Adapter Interface；Sprint 5 新增 ai_conversation；新增 §16 ADR Roadmap（ADR-0005~0011） |
| v2.2 | 2026-07-28 | §16 ADR Roadmap 重写：ADR 生命周期与 Sprint 生命周期一致；ADR-0006~0009 严格按对应 Sprint 创建（Pending）；ADR-0010 提前到 Sprint 0（Proposed）；ADR-0011 提前到 Sprint 0（Accepted，已是架构事实）；ADR-0007 改为 Provider Adapter Pattern；ADR-0008 改为抽象层策略不锁定 Provider；ADR-0009 标注 MVP 可延期 |


---

# 18. Alignment


| 文档 | 对齐点 |
|------|--------|
| PROJECT_CONTEXT §9 | 产品发展阶段 Phase 0–4 |
| PROJECT_CONTEXT §13 | 隐私原则（Mood / Story Sprint） |
| ARCHITECTURE §5 §6 | Module 命名与模块边界 |
| ARCHITECTURE §7 | AI Platform 完整链路 |
| ARCHITECTURE §18 | 外部集成通过 Adapter（地图 / 支付 / LLM） |
| ARCHITECTURE §22 | Data Ownership 决定 Migration 顺序 |
| DATABASE_DESIGN §4 | 表归属决定 Sprint 交付 |
| DATABASE_DESIGN §10 | Migration 规则 |
| AGENTS §3 | Sprint 的 Owner / Reviewer Agent 权限 |
| AGENTS §7 | Sprint 与 Task 生命周期一致 |
| AGENTS §9 | Sprint DoD 包含自检清单 |
| CODE_RULES §10 | 测试规则 |
