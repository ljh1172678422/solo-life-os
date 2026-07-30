# PR: User Test Suite (TASK-0106)

## 创建地址

```
https://github.com/ljh1172678422/solo-life-os/compare/develop...feature/user-test-suite
```

## Title

```
test(user): TASK-0106 User Test Suite (JUnit 5 + Mockito + MockMvc, 40 tests)
```

## Description (复制以下内容)

```markdown
## Summary

完成 TASK-0106 User Test Suite，建立 User Module 单元测试套件，覆盖 Domain Service / Application Service / Security 组件 / Controller 全部分层。对齐 Sprint 0 DoD 中延期的「Backend 单元测试框架运行」与「API 测试框架运行」两项。

## Changes

- 新增 5 个测试类，40 个测试用例全部通过（0 failures / 0 errors / 0 skipped）
  - `UserDomainServiceTest`（11）：register / activate / ban / updateProfile 业务规则
  - `AuthServiceTest`（7）：login 成功（邮箱/手机）/ 账号不存在 / 密码错误 / 用户封禁
  - `JwtServiceTest`（7）：token 签发 / 解析校验 / 过期 / 篡改
  - `UserControllerTest`（10）：MockMvc 注册 / 查询 / 更新 + 参数校验 + 业务异常
  - `AuthControllerTest`（5）：MockMvc 登录 + 参数校验 + 认证异常
- Controller 测试使用 standalone MockMvc（`MockMvcBuilders.standaloneSetup`），隔离 Spring Security 自动配置
- 新增 `mockito-extensions/org.mockito.plugins.MockMaker`（mock-maker-subclass），绕开 inline mock maker 在 Java 25 上的字节码限制

## Test Layer Coverage

```
Domain Service    ─ UserDomainServiceTest   (11 tests)
Application       ─ AuthServiceTest          (7 tests)
Security          ─ JwtServiceTest           (7 tests)
Controller        ─ UserControllerTest       (10 tests)
                  ─ AuthControllerTest       (5 tests)
                                              ─────────
                                              Total: 40 tests
```

## Validation

```
mvn clean test
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Governance

- Branch: feature/user-test-suite
- TASK_BOARD: TASK-0106
- Validation: ✅ Passed (40 tests)
- 范围控制：仅新增测试代码 + Mockito 配置，未修改生产业务逻辑

## Related

TASK-0106 User Test Suite
依赖：TASK-0102 / TASK-0103 / TASK-0104 / TASK-0107
```

## Merge 设置

- Merge 方式: Squash merge
- Delete branch after merge: ✅
