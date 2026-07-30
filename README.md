# Solo Life OS

> 一个基于 AI 的个人生活操作系统（AI Personal Life Operating System）。

AI 不只是回答问题，而是理解用户的生活，并主动帮助用户创造更好的生活。

---

## 项目状态

Phase 0：基础平台已完成，进入业务模块开发

当前 Sprint：Sprint 1（User Module）— ✅ Done (Closed 2026-07-30)

下一阶段：Sprint 2 待启动

### 已完成

**Sprint 0 — Foundation（Done 2026-07-29）**

- ✅ Architecture Foundation（8 模块 + AI Platform 边界冻结）
- ✅ Backend Foundation（Spring Boot 3.2.5 + Java 17 Modular Monolith）
- ✅ Frontend Foundation（uni-app + Vue3 + TypeScript H5）
- ✅ AI Foundation（6 个核心 Interface：Agent / Router / Memory / Conversation / VectorStoreAdapter / LLMProvider）
- ✅ Database Foundation（PostgreSQL 16 + Redis 7 + Flyway + 三表初始化）
- ✅ CI/CD Foundation（Backend Maven + Frontend npm 双流水线）
- ✅ Documentation Foundation（ADR Index + 模板 + 版本同步规则）

**Sprint 1 — User Module（Done 2026-07-30）**

- ✅ User Migration Review（TASK-0101，PR #10）
- ✅ User Domain Layer（Entity / Repository / Domain Service，PR #12）
- ✅ User Application Service（注册 / 资料 / 偏好 / 标签用例，PR #13）
- ✅ User Controller + DTO（7 DTO + Assembler + 4 Controller，PR #14）
- ✅ Authentication（ADR-0006 JWT：BCrypt + JwtAuthFilter + /api/auth/login，PR #15）
- ✅ User Frontend（注册 / 登录 / 资料 / 偏好四页面，PR #16）
- ✅ User Test Suite（JUnit 5 + Mockito + MockMvc，40 tests，PR #17）

### 进行中

无（Sprint 1 已关闭，Sprint 2 待启动）

详见 [docs/TASK_BOARD.md](docs/TASK_BOARD.md)。

---

## 文档导航

| 文档 | 用途 |
|------|------|
| [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md) | 项目上下文 / 产品宪法（最高级文档） |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | 系统架构与服务设计 |
| [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md) | 核心数据表设计 |
| [docs/AGENTS.md](docs/AGENTS.md) | AI Agent 协作规范与 Git 协议 |
| [docs/CODE_RULES.md](docs/CODE_RULES.md) | 编码规范 |
| [docs/SPRINT_PLAN.md](docs/SPRINT_PLAN.md) | Sprint 规划 |
| [docs/TASK_BOARD.md](docs/TASK_BOARD.md) | 当前任务看板 |
| [docs/AI_CHANGELOG.md](docs/AI_CHANGELOG.md) | AI 开发行为日志 |
| [docs/CHANGELOG.md](docs/CHANGELOG.md) | 项目变更记录 |
| [AGENTS.md](AGENTS.md) | AI Agent 根目录入口 |

---

## 技术栈

- **客户端**：uni-app + Vue3 + TypeScript（H5 / 微信小程序 / App）
- **后端**：Java + Spring Boot（模块化单体架构）
- **数据库**：PostgreSQL + Redis + Vector DB
- **AI**：LLM + Agent + Memory（Orchestrator 模式）

详见 [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md) §10 技术方向。

---

## 分支策略

采用 **Git Flow 精简版**：

```
main        产品稳定版本，仅通过 PR 合并，禁止直接提交
 │
develop     研发集成分支，所有 Agent 开发结果最终汇入
 │
feature/*    每任务一分支，AI Agent 必须走 feature → PR → merge
```

分支命名：`feature/<模块>-<任务>`，例如 `feature/backend-user-module`。

详见 [docs/AGENTS.md](docs/AGENTS.md) §5 Git 协作规范。

---

## AI Agent 开发协议

本仓库由多个 AI Agent 长期协作开发。任何 AI Agent 在动手前**必须先阅读** [docs/AGENTS.md](docs/AGENTS.md)。

核心约束：

- AI 禁止直接提交到 `develop` 或 `main`
- 必须创建 `feature/*` 分支 → 提交 → 创建 PR → 人工审核 → merge
- Commit 遵循 Conventional Commits：`type(scope): description`
- 编码前必读 `docs/PROJECT_CONTEXT.md` 与 `docs/ARCHITECTURE.md`
- 禁止创建重复 Entity、修改核心数据结构、引入未经批准的大型框架

详见 [docs/AGENTS.md](docs/AGENTS.md)。

---

## License

私有项目，保留所有权利。
