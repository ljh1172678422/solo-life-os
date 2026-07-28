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
