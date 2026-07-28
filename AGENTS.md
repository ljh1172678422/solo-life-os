# Solo Life OS AI Agent Entry


本文件是 AI Coding Agent（如 Trae Work / Cursor / Claude Code）的根目录入口。

详细规则：

➡ [docs/AGENTS.md](docs/AGENTS.md)


---

## 快速指引


任何 AI Agent 在动手前必须按顺序阅读：


1. [docs/PROJECT_CONTEXT.md](docs/PROJECT_CONTEXT.md) — 项目宪法
2. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — 系统架构
3. [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md) — 数据模型
4. [docs/AGENTS.md](docs/AGENTS.md) — 协作规范（核心）
5. [docs/CODE_RULES.md](docs/CODE_RULES.md) — 编码规范
6. [docs/TASK_BOARD.md](docs/TASK_BOARD.md) — 当前任务


---

## 核心约束


- 禁止直接提交到 `main` / `develop`，必须走 `feature/* → PR → merge`
- 禁止创建重复 Entity、跨模块直连数据库
- 禁止先写代码再补架构
- Commit 遵循 `type(scope): description` 规范
- 完成任务后必须更新 `docs/TASK_BOARD.md` / `docs/CHANGELOG.md` / `docs/AI_CHANGELOG.md`


完整规则见 [docs/AGENTS.md](docs/AGENTS.md)。
