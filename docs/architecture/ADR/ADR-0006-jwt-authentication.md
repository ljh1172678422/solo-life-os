# ADR-0006: JWT Authentication Strategy

Date: 2026-07-30

Status: Accepted

## Decision

采用 JWT（HS256）作为 MVP 阶段认证方案：BCrypt 哈希密码、自签发 JWT token、自定义 JwtAuthFilter 拦截请求，不引入完整 Spring Security 框架。

## Reason

- **业务驱动**：SPRINT_PLAN Sprint 1 DoD 要求"注册→登录→设置偏好"闭环，必须有 login 端点与 token 机制
- **技术约束**：Modular Monolith 无状态认证，JWT 自包含、无需服务端 session，适合单实例 MVP
- **团队约束**：避免 Spring Security 全套配置的复杂度（SecurityFilterChain 链 / UserDetailsService / OAuth2），MVP 仅需 login + token 验证，自写 Filter 更可控

## Impact

- **影响模块**：User Module（加 password 字段 + AuthController）/ common（新增 security 子包）
- **数据库**：user 表增加 `password` 字段（varchar(100)，nullable，BCrypt 哈希）
- **新增代码**：
  - `common/security/`：JwtService（签发/验证）/ JwtAuthFilter（请求拦截）/ JwtProperties（配置）
  - `user/application/AuthService`：登录用例
  - `user/controller/AuthController`：POST /api/auth/login
  - `user/dto/`：LoginRequest / LoginResponse
- **配置**：application.yml 增加 `jwt.secret` + `jwt.expiration`（环境变量注入）
- **不影响现有数据**：password 字段 nullable，存量用户无密码需重新设置

## Migration / Follow-up

- **Migration 步骤**：V20260730_001__add_password_to_user.sql（ALTER TABLE "user" ADD COLUMN password varchar(100)）
- **Follow-up**：Sprint 5+ 可升级为 Spring Security + OAuth2（第三方登录）；当前 ADR 不锁定后续升级路径
- **验证方式**：CI 编译通过 + 注册后用 login 端点获取 token + 带 token 访问受保护端点
