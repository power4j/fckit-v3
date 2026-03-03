#!/bin/bash
#
# release.sh - 版本发布脚本
#
# 用法：
#   正式发布：./release.sh
#   本地测试：./release.sh --local
#

set -euo pipefail

POM="pom.xml"
MASTER_BRANCH="master"
DEV_BRANCH="dev"
LOCAL_MODE=false

# ---- 解析参数 ----
for arg in "$@"; do
  case "$arg" in
    --local|-l)
      LOCAL_MODE=true
      ;;
    *)
      echo "未知参数: $arg"
      echo "用法: $0 [--local|-l]"
      exit 1
      ;;
  esac
done

# ---- git 操作封装（本地模式跳过 push/pull）----
git_pull() {
  if [[ "$LOCAL_MODE" == true ]]; then
    echo "  [本地模式] 跳过: git pull origin $1"
  else
    git pull origin "$1"
  fi
}

git_push() {
  if [[ "$LOCAL_MODE" == true ]]; then
    echo "  [本地模式] 跳过: git push origin $1"
  else
    git push origin "$1"
  fi
}

# ---- 版本号格式校验（仅允许 X.X.X）----
validate_version() {
  local ver="$1"
  if [[ ! "$ver" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "错误：版本号格式不正确，必须为 X.X.X（如 3.11.0）"
    return 1
  fi
}

# ---- 交互式输入版本号 ----
read -r -p "请输入发布版本号（格式 X.X.X，如 3.11.0）: " RELEASE_VERSION
if ! validate_version "$RELEASE_VERSION"; then
  exit 1
fi

read -r -p "请输入下一个开发版本号（格式 X.X.X，如 3.12.0）: " NEXT_BASE_VERSION
if ! validate_version "$NEXT_BASE_VERSION"; then
  exit 1
fi

TAG="v${RELEASE_VERSION}"
NEXT_VERSION="${NEXT_BASE_VERSION}-SNAPSHOT"

# ---- 环境检查 ----
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "错误：工作区有未提交的变更，请先处理后再执行发布。"
  exit 1
fi

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$CURRENT_BRANCH" != "$DEV_BRANCH" ]]; then
  echo "错误：请在 $DEV_BRANCH 分支上执行发布脚本（当前分支：$CURRENT_BRANCH）。"
  exit 1
fi

if git tag | grep -qx "$TAG"; then
  echo "错误：tag $TAG 已存在。"
  exit 1
fi

echo ""
echo "========================================"
if [[ "$LOCAL_MODE" == true ]]; then
echo "  *** 本地模式：不会执行任何 push ***"
fi
echo "  发布版本：$RELEASE_VERSION"
echo "  Git Tag： $TAG"
echo "  下一版本：$NEXT_VERSION"
echo "========================================"
read -r -p "确认继续？[y/N] " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo "已取消。"
  exit 0
fi

# ---- 第一阶段：发布正式版 ----
echo ""
echo "[1/8] 拉取最新 $DEV_BRANCH ..."
git_pull "$DEV_BRANCH"

echo "[2/8] 切换到 $MASTER_BRANCH 并合并 $DEV_BRANCH ..."
git checkout "$MASTER_BRANCH"
git_pull "$MASTER_BRANCH"
git merge "$DEV_BRANCH" --no-ff -m "feat: release $RELEASE_VERSION"

echo "[3/8] 修改版本号为 $RELEASE_VERSION ..."
./mvnw versions:set-property -Dproperty=revision -DnewVersion="$RELEASE_VERSION" -DgenerateBackupPoms=false --no-transfer-progress -q
git add "$POM"
git commit -m "chore: bump version to $RELEASE_VERSION"

echo "[4/8] 打 tag $TAG ..."
git tag "$TAG"

echo "[5/8] 推送 $MASTER_BRANCH 和 tag（触发 CI 自动发布）..."
git_push "$MASTER_BRANCH"
git_push "$TAG"

# ---- 第二阶段：开启下一开发版本 ----
echo ""
echo "[6/8] 修改版本号为 $NEXT_VERSION ..."
./mvnw versions:set-property -Dproperty=revision -DnewVersion="$NEXT_VERSION" -DgenerateBackupPoms=false --no-transfer-progress -q
git add "$POM"
git commit -m "chore: bump version to $NEXT_VERSION"

echo "[7/8] 推送 $MASTER_BRANCH ..."
git_push "$MASTER_BRANCH"

echo "[8/8] 同步回 $DEV_BRANCH ..."
git checkout "$DEV_BRANCH"
git merge "$MASTER_BRANCH" --no-ff -m "chore: sync $NEXT_VERSION from $MASTER_BRANCH"
git_push "$DEV_BRANCH"

echo ""
echo "========================================"
echo "  完成！"
if [[ "$LOCAL_MODE" == true ]]; then
  echo "  [本地模式] 所有 push 已跳过，本地 git 状态已变更"
  echo "  提示：可用 git log --oneline -8 查看提交，用 git tag 查看 tag"
else
  echo "  - tag $TAG 已推送，GitHub Actions 将自动发布 $RELEASE_VERSION 到 Maven Central"
fi
echo "  - 当前开发版本已升级为 $NEXT_VERSION"
echo "========================================"
