# 版本发布流程

## 概述

本项目使用 `${revision}` 统一管理版本号，通过 Git tag 触发 GitHub Actions 自动发布到 Maven Central。

## 分支策略

| 分支 | 用途 |
|---|---|
| `master` | 正式发布基线，每次发布打 tag |
| `dev` | 日常开发集成分支 |
| `snapshot` | 推送到此分支自动发布 SNAPSHOT |
| 功能分支 | 从 `dev` 拉出，完成后合并回 `dev` |

## CI 触发规则

| 触发条件 | 执行动作 |
|---|---|
| push 到 `master` / `dev` | 编译 + 测试（ci.yml） |
| push 到 `snapshot` 分支 | 发布 SNAPSHOT 到 Maven Central（oss-deploy.yml） |
| push 任意 tag | 发布正式版到 Maven Central（oss-deploy.yml） |

---

## 正式版发布流程

以发布 `X.Y.0` 为例，当前开发版本为 `X.Y-SNAPSHOT`。

### 第一阶段：发布正式版

**Step 1：确保 dev 分支测试全部通过**

```bash
git checkout dev
mvn test
```

**Step 2：dev 合并到 master**

```bash
git checkout master
git pull origin master
git merge dev --no-ff -m "feat: release X.Y.0"
```

**Step 3：修改版本号为正式版**

编辑根 `pom.xml`：

```xml
<revision>X.Y.0</revision>
```

**Step 4：提交版本变更**

```bash
git add pom.xml
git commit -m "chore: bump version to X.Y.0"
```

**Step 5：打 tag 并推送（触发自动发布）**

```bash
git tag vX.Y.0
git push origin master
git push origin vX.Y.0
```

推送 tag 后，GitHub Actions 的 `oss-deploy.yml` 会自动执行 GPG 签名并发布到 Maven Central。

---

### 第二阶段：开启下一开发版本

**Step 6：master 上升版本为下一 SNAPSHOT**

编辑根 `pom.xml`：

```xml
<revision>X.Z-SNAPSHOT</revision>
```

> 通常 Z = Y + 1，例如发布 3.11.0 后升为 3.12-SNAPSHOT。

**Step 7：提交并推送**

```bash
git add pom.xml
git commit -m "chore: bump version to X.Z-SNAPSHOT"
git push origin master
```

**Step 8：同步回 dev**

```bash
git checkout dev
git merge master --no-ff -m "chore: sync X.Z-SNAPSHOT from master"
git push origin dev
```

---

## 流程图

```
功能分支 ──merge──► dev ──merge──► master
                               │
                    [修改版本 X.Y.0，打 tag]
                               │
                         vX.Y.0 ──push──► GitHub Actions 自动发布
                               │
                    [修改版本 X.Z-SNAPSHOT]
                               │
                         push master ──merge──► dev
```

---

## 注意事项

- 打 tag 前必须确认 `pom.xml` 中版本号已改为正式版（不含 `-SNAPSHOT`）
- `examples/` 和 `benchmarks/` 模块配置了 `maven.deploy.skip=true`，不会被发布
- 发布需要仓库中配置以下 Secrets：`MAVEN_GPG_KEY`、`MAVEN_GPG_PASSPHRASE`、`MAVEN_CENTRAL_USERNAME`、`MAVEN_CENTRAL_PASSWORD`
- 脚本自动化见项目根目录 `release.sh`，直接运行后按提示输入版本号即可：
  ```bash
  ./release.sh
  # 提示输入发布版本（如 3.11.0），脚本自动添加 v 前缀生成 tag
  # 提示输入下一版本基础号（如 3.12.0），脚本自动添加 -SNAPSHOT 后缀
  ```
