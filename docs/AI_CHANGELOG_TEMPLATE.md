# AI Change Entry Template

> 复制本模板创建 `AI_CHANGELOG.md` 条目。所有 AI Agent 行为必须按此格式记录。
> 对齐 AGENTS §10 Agent Handoff Protocol。

---

## 模板

```md
## YYYY-MM-DD

Agent:     <Agent 名称>
Task:      <TASK-XXX 或「非任务」>
Action:    <做了什么>
Reason:    <为什么做>
Impact:    <影响范围>
Reviewer:  <Human / Pending>
```

---

## 字段说明

| 字段 | 必填 | 说明 |
|------|------|------|
| Agent | ✅ | 执行 Agent 名称（Product / Architecture / Backend / Frontend / AI / QA） |
| Task | ✅ | 关联 TASK-XXXX；非任务行为填「非任务」 |
| Action | ✅ | 具体行为，动词开头。例：「创建 user 表 Migration」 |
| Reason | ✅ | 为什么做，业务或技术驱动 |
| Impact | ✅ | 影响范围：文件 / 模块 / 数据库 |
| Reviewer | ✅ | Human（已审核）/ Pending（待审核） |

---

## 示例

```md
## 2026-07-28

Agent:     Backend Agent
Task:      TASK-0004 Database Foundation
Action:    创建 user / user_preference / tag 三张表的初始 Migration
Reason:    Sprint 0 Phase 3 需要数据库基础环境，为 Sprint 1 User Module 提供前置表
Impact:    database/migrations/ 新增 3 个 SQL 文件
Reviewer:  Pending
```

---

## 规则

1. **每次行为一条记录**：不要把多个不相关行为合并到一条
2. **按时间倒序**：最新行为在文件顶部（§ 下方）
3. **必须真实**：禁止虚构行为或夸大影响
4. **必须及时**：行为发生后立即记录，不要事后补写
5. **Reviewer 字段**：Pending 表示待人工审核；Human 审核后改为 Human
