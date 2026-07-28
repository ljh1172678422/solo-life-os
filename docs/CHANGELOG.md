# Changelog


记录 Solo Life OS 所有重要变更。


格式参考 [Keep a Changelog](https://keepachangelog.com/)。

版本号遵循 [Semantic Versioning](https://semver.org/)。


---

## [Unreleased]


### Added

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
