# Solo Life OS 编码规范

Version: 2.0

Status: Active

Last Update: 2026-07-28


> 本文档定义 Solo Life OS 的代码生成与提交约束，所有 AI Agent 与人类开发者必须遵守。
> 与 ARCHITECTURE §2 §16 §19 §20、DATABASE_DESIGN v2.0、AGENTS.md 完全对齐。


---

# 1. General Principles


- 简单优先，不过度工程（遵循 TRAE 系统提示「避免过度工程」）
- 一个类一个职责，一个方法一个职责
- 命名清晰胜过注释，注释只在逻辑非自明时写
- 不为假设的未来需求做抽象
- 修改前先读懂上下文，不在不理解的代码上叠加


---

# 2. Frontend


技术栈：Vue3 + TypeScript + Pinia + uni-app


## 2.1 类型约束


- 禁止 `any`
- 禁止 `as any`
- 未知类型优先使用 `unknown`，使用前必须收窄
- 所有 props / emit / ref 必须显式类型定义


## 2.2 请求层约束


```
views
  ↓
api/
  ↓
request.ts（统一封装 axios）
  ↓
Backend
```


规则：


- 禁止在组件中直接使用 axios
- 禁止组件直接请求 AI（ARCHITECTURE §11）
- 所有请求经 `api/` 目录封装，返回类型显式定义


## 2.3 组件约束


- 一个组件一个职责
- 组件名 PascalCase，文件名与组件名一致
- 公共组件放 `components/`，页面组件放 `pages/` 或 `views/`


---

# 3. Backend


技术栈：Java + Spring Boot


## 3.1 分层架构


```
Controller
  ↓
Application Service
  ↓
Domain Service
  ↓
Repository Interface
  ↓
Infrastructure
```


| 层 | 职责 | 禁止 |
|----|------|------|
| Controller | 接收请求 / 参数校验 / 返回封装 | 写业务逻辑 |
| Application Service | 协调用例 / 事务边界 | 写 SQL |
| Domain Service | 业务规则 | 依赖 Spring（保持纯 POJO） |
| Repository Interface | 持久化抽象 | 写实现 |
| Infrastructure | 持久化实现 / 外部适配器 | 被 Domain 直接引用 |


与 ARCHITECTURE §2 完全一致。


## 3.2 命名规范


| 类型 | 命名 | 示例 |
|------|------|------|
| Controller | `<Entity>Controller` | UserController |
| Application Service | `<Entity>ApplicationService` | UserApplicationService |
| Domain Service | `<Entity>DomainService` | UserDomainService |
| Repository Interface | `<Entity>Repository` | UserRepository |
| Repository Impl | `<Entity>RepositoryImpl` | UserRepositoryImpl |


禁止使用：


- `Manager` / `Biz` / `Helper` / `Util` 作为业务类后缀
- `ServiceImpl2` 等带数字后缀
- `service` / `services` / `biz` 作为包名


## 3.3 依赖注入


- 字段注入禁止，使用构造器注入
- 所有 Bean 必须显式声明（@Service / @Component / @Repository）


---

# 4. Package Convention


与 ARCHITECTURE §19 完全一致：


```
backend/
└── solo-server/
    └── com/sololifeos/
        ├── user/
        │   ├── controller/
        │   ├── application/
        │   ├── domain/
        │   │   ├── model/
        │   │   └── service/
        │   ├── repository/
        │   └── infrastructure/
        ├── today/
        ├── explore/
        ├── mood/
        ├── growth/
        ├── community/
        ├── story/
        └── ai/
            ├── orchestrator/
            ├── agents/
            ├── memory/
            └── llm/
```


规则：


- 模块间禁止直接 import 对方内部类
- 跨模块访问必须通过 Domain API（ARCHITECTURE §22）
- 新增模块必须先在 ARCHITECTURE §6 + §12 + 本节登记


---

# 5. DTO / Entity / VO Rules


```
Controller
  ↓
DTO（接口传输对象）
  ↓
Application Service
  ↓
Domain Entity（领域实体）
```


规则：


- DTO 仅用于接口输入输出，禁止进入 Domain
- Domain Entity 禁止直接出现在 Controller
- 禁止 `@PostMapping public User save(User user)` 这种把 Entity 当 DTO 用的写法
- Controller 与 Application Service 之间必须经 DTO 转换（Assembler / Mapper）


禁止：


- Controller 直接接收 / 返回 Entity
- Entity 携带 @RequestBody / @ResponseBody 等 Web 注解


---

# 6. Exception Handling


与 ARCHITECTURE §20 完全一致。


## 6.1 异常类层级


```
RuntimeException
  └── SoloException
       ├── BusinessException       业务异常
       ├── ValidationException      参数校验异常
       ├── AIException              AI 调用异常
       ├── ExternalException        外部依赖异常
       └── AuthException            认证授权异常
```


## 6.2 规则


- 禁止 `throw new Exception()`（必须用具体子类）
- 禁止 `throw new RuntimeException("xxx")`
- 业务异常必须带错误码（DATABASE_DESIGN §7 的枚举或 USER-ERR-001 形式）
- 所有异常由全局异常处理器捕获，禁止在 Controller 手写 try-catch 返回


---

# 7. Logging Rules


与 ARCHITECTURE §16 完全一致。


## 7.1 日志框架


- 必须 `@Slf4j` + `log.info()` / `log.warn()` / `log.error()`
- 禁止 `System.out.println` / `e.printStackTrace()`


## 7.2 必含字段


所有日志必须携带：


- traceId（贯穿前端 → Backend → AI）
- userId（已登录场景）
- module（模块名）


## 7.3 敏感数据脱敏


- 手机号 / 邮箱 / 位置 / 情绪数据日志输出前必须脱敏
- 禁止把 token / 密码 / 凭证写入日志


---

# 8. Database Rules


与 DATABASE_DESIGN v2.0 完全一致。


## 8.1 命名


- 表名 snake_case 单数
- 字段名 snake_case
- 主键 `id` bigint 自增
- 外键 `<表>_id`，逻辑关联不建 FK


## 8.2 审计字段


所有业务表必须包含：


- `created_time` datetime NOT NULL
- `updated_time` datetime NOT NULL


可软删除的表包含：


- `deleted_time` datetime NULL（NULL 表示未删除）


## 8.3 软删除


- 优先软删除（deleted_time）
- 查询必须过滤 `deleted_time IS NULL`


## 8.4 枚举


- 枚举字段禁止自由字符串，必须使用 DATABASE_DESIGN §7 显式定义的值
- Java 侧用 enum，禁止用魔法字符串


## 8.5 迁移


- 迁移脚本由 Backend Agent 执行（AGENTS.md §3）
- 命名 `V<日期>_<序号>__<描述>.sql`
- 必须幂等，破坏性变更必须先写 ADR


---

# 9. API Rules


## 9.1 统一返回格式


```json
{
  "code": 0,
  "message": "",
  "data": {},
  "traceId": ""
}
```


- code=0 表示成功
- 业务异常 code=1001 等，HTTP 400
- 系统异常 code=5000，HTTP 500
- 禁止向客户端返回堆栈


## 9.2 REST 规范


- 路径小写复数：`/api/users` / `/api/daily-plans`
- 资源操作用 HTTP 方法：GET / POST / PUT / PATCH / DELETE
- 查询参数 camelCase，与前端一致


## 9.3 鉴权


- 所有非登录接口必须验证 token
- token 由 User Module 统一签发（ARCHITECTURE §17）


---

# 10. Testing Rules


## 10.1 测试分层


| 层 | 测试类型 | 工具 |
|----|---------|------|
| Domain | 单元测试 | JUnit 5 + Mockito |
| Application | 集成测试 | Spring Boot Test |
| Controller | API 测试 | MockMvc / WebTestClient |
| Repository | 仓储测试 | @DataJpaTest / Testcontainers |


## 10.2 规则


- Domain Service 必须有单元测试
- 新增 API 必须有 Controller 测试
- 测试不依赖外部真实服务（用 Mock / Testcontainers）
- 命名：`<ClassName>Test`，方法 `should_<behavior>_when_<condition>`


---

# 11. AI Generated Code Rules


本节使 CODE_RULES 与 AGENTS.md / ARCHITECTURE 真正联动。


- AI 不得修改未授权模块（AGENTS.md §3 权限分级）
- AI 不得删除 Migration（DATABASE_DESIGN §10）
- AI 不得修改 Prompt（AGENTS.md §11，必须经 PR）
- AI 必须遵守 Package Convention（ARCHITECTURE §19）
- AI 必须遵守 §4 DTO/Entity/VO 边界，禁止 Entity 直出 Controller
- AI 不得在 Repository 写业务规则
- AI 不得跨模块直连数据库（ARCHITECTURE §1 §4 §22）
- AI 不得绕过权限直接访问数据（ARCHITECTURE §17 §21）
- AI 生成代码必须符合本规范，否则 PR 拒绝
- AI 提交前必须完成 AGENTS.md §9 自检清单


---

# 12. Git Convention


## 12.1 分支类型


与 AGENTS.md §5.1 完全一致：


| 分支 | 用途 |
|------|------|
| `main` | 产品稳定版本，仅 PR 合并 |
| `develop` | 研发集成分支 |
| `feature/*` | 新功能，命名 `feature/<模块>-<任务>` |
| `bugfix/*` | Bug 修复 |
| `hotfix/*` | 生产紧急修复（唯一可从 main 切出并 PR 回 main） |
| `docs/*` | 文档独立生命周期 |
| `refactor/*` | 重构（无功能变更） |


## 12.2 Commit 规范


格式：


```
type(scope): description
```


| type | 用途 |
|------|------|
| feat | 新增功能 |
| fix | 修复 Bug |
| refactor | 重构（无功能变更） |
| docs | 文档变更 |
| chore | 工程维护 |
| test | 测试相关 |
| perf | 性能优化 |


scope 取模块名：`user` / `today` / `mood` / `growth` / `explore` / `community` / `story` / `ai` / `repo` / `agents` / `architecture` / `database` / `context`。


## 12.3 规则


- 一个 commit 不跨多个模块
- 提交信息说明 why 而非仅 what
- 禁止直接提交到 main / develop，必须走 feature 分支 + PR
- 使用 HEREDOC 保证多行 commit 格式正确


---

# 13. Version History


| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-28 | 初始版本：前端 / Java / 数据库 / API / Git 基础规范 |
| v2.0 | 2026-07-28 | 全量升级：补充分层细化 / DTO/Entity 边界 / 异常 / 日志 / 测试 / AI 代码规则；分支类型与 AGENTS 对齐；返回格式增加 traceId |


---

# 14. Alignment


| 文档 | 对齐点 |
|------|--------|
| ARCHITECTURE §2 | 后端分层架构 |
| ARCHITECTURE §16 | 日志必含 traceId |
| ARCHITECTURE §17 | API 鉴权 |
| ARCHITECTURE §19 | Package Convention |
| ARCHITECTURE §20 | 异常类层级与错误码 |
| ARCHITECTURE §21 | AI 不得绕过 Domain API |
| ARCHITECTURE §22 | 跨模块访问经 Domain API |
| DATABASE_DESIGN v2.0 | 表名 / 审计字段 / 软删除 / 枚举 / 迁移 |
| AGENTS.md §3 | AI Agent 权限分级 |
| AGENTS.md §5.1 | Git 分支类型 |
| AGENTS.md §9 | AI 提交前自检 |
| AGENTS.md §11 | Prompt 文件受控变更 |
