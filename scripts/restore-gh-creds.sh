#!/usr/bin/env bash
# TRAE 沙箱重启后恢复 GitHub 凭证
# 用法: bash scripts/restore-gh-creds.sh
#
# 持久化凭证存储在 /data/user/.gh-creds/（沙箱持久挂载卷，重启不丢失）
# 本脚本将 git credential helper 指向持久化凭证文件，并 source GH_TOKEN 环境变量
#
# ⚠️ 安全提示:
#   - 凭证文件 .git-credentials 和 env.sh 权限为 600，仅 root 可读
#   - 如需轮换 token: 删除 /data/user/.gh-creds/ 下文件，重新执行 gh auth login

set -e

CRED_DIR="/data/user/.gh-creds"
CRED_FILE="$CRED_DIR/.git-credentials"
ENV_FILE="$CRED_DIR/env.sh"

if [[ ! -f "$CRED_FILE" || ! -f "$ENV_FILE" ]]; then
  echo "ERROR: 持久化凭证文件不存在于 $CRED_DIR"
  echo "请通过对话让 AI 重新配置凭证（或手动 gh auth login）"
  exit 1
fi

# 配置 git credential helper 指向持久化文件
git config --global credential.helper "store --file=$CRED_FILE"
echo "OK: git credential.helper -> $CRED_FILE"

# 导出 GH_TOKEN 供 gh CLI 使用
# shellcheck disable=SC1090
source "$ENV_FILE"
echo "OK: GH_TOKEN 已导出（gh CLI 可用）"

# 验证
echo "--- 验证 gh auth status ---"
gh auth status 2>&1 | head -5
echo "--- 验证 git push (dry-run, 需在 git 仓库内) ---"
echo "（如需验证 push: cd <repo> && git push --dry-run origin <branch>）"
