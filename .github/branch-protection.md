# Solo Life OS Branch Protection Rules
# TASK-0006 CI/CD Foundation
#
# 对齐 AGENTS §5.2（AI Agent 严禁直接操作 develop/main）
#         AGENTS §15.6（PR 合并条件）
#
# 本文件记录分支保护规则，需通过 GitHub API 或 Web UI 配置。
# 配置命令见文末。

## main 分支保护规则

- ✅ Require pull request before merging
- ✅ Require approvals: 1
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require status checks to pass: backend-ci, frontend-ci
- ✅ Require branches to be up to date before merging
- ✅ Do not allow bypassing the above settings
- ✅ Restrict who can push: nobody（仅 PR）

## develop 分支保护规则

- ✅ Require pull request before merging
- ✅ Require approvals: 1
- ✅ Require status checks to pass: backend-ci, frontend-ci
- ✅ Require branches to be up to date before merging
- ✅ Do not allow bypassing the above settings

## 配置命令（需 admin 权限 + gh CLI 认证）

```bash
# main 分支保护
gh api -X PUT repos/ljh1172678422/solo-life-os/branches/main/protection \
  -f required_pull_request_reviews[required_approving_review_count]=1 \
  -f required_pull_request_reviews[dismiss_stale_reviews]=true \
  -f required_status_checks[strict]=true \
  -f required_status_checks[contexts][]=backend-ci \
  -f required_status_checks[contexts][]=frontend-ci \
  -f enforce_admins=true \
  -f restrictions=

# develop 分支保护
gh api -X PUT repos/ljh1172678422/solo-life-os/branches/develop/protection \
  -f required_pull_request_reviews[required_approving_review_count]=1 \
  -f required_status_checks[strict]=true \
  -f required_status_checks[contexts][]=backend-ci \
  -f required_status_checks[contexts][]=frontend-ci \
  -f enforce_admins=true \
  -f restrictions=
```

## 注意

- 分支保护规则一旦设置，AI Agent 无法直接 push 到 main/develop
- 合并 PR 必须等 CI 通过（backend-ci + frontend-ci 两个 status check）
- Sprint 0 阶段 CI 中的 test/build 步骤使用 `continue-on-error: true`，
  因为业务测试和完整构建依赖尚未补全；Sprint 1 起收紧为必须通过
