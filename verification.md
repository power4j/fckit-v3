# fist-sde 验证记录

- 日期：2026-05-08
- 执行者：Codex
- worktree：`D:\git-repo\fist-dev\fist-cloud-kit-v3\.worktrees\cj-fist-sde`
- 分支：`cj/fist-sde`

## 验证命令

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 沙箱内首次执行失败：Maven Central 访问被拒，父级 BOM 无法解析。
- 放行后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml test`

- 沙箱内首次执行失败：Maven Central 访问被拒，父级 BOM 无法解析。
- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：4 个测试，0 failures，0 errors，0 skipped。
- `fist-sde-extra`：5 个测试，0 failures，0 errors，0 skipped。
- `fist-sde-web`：无独立测试类，Web MVC 行为由 `fist-sde-boot-starter` 测试覆盖。
- `fist-sde-boot-starter`：5 个测试，0 failures，0 errors，0 skipped。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 沙箱内首次执行失败：Maven Central 访问被拒，父级 BOM 无法解析。
- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-web`：重新编译 4 个主源码文件，无独立测试类。
- `fist-sde-boot-starter`：重新编译 4 个主源码文件，5 个测试通过。

### `.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=*PrototypeTest,FeignResponseRebuildTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 沙箱内首次执行失败：Maven Central 访问被拒，父级 BOM 无法解析。
- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- Feign SDE 原型测试 7 个通过，0 failures，0 errors，0 skipped。
- 覆盖内容：Spring Cloud OpenFeign 注解元数据可传递到 `Encoder` 侧、请求 `Encoder` 包装 secure envelope、响应 `Decoder` 验签解密后委托原 `Decoder`、Feign `Response` 重建保留状态与请求上下文、原型自动配置默认关闭且启用后可与既有 `FeignClientAutoConfiguration` 共存。

### `.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`

- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- 覆盖 `fist-cloud-rpc-feign` 及其依赖模块的模块级测试。
- `fist-cloud-rpc-feign`：Feign SDE 原型测试 7 个通过，0 failures，0 errors，0 skipped。
- Maven 生命周期中已执行相关模块的 `spring-javaformat:validate`。

## 说明

- Feign 技术验证已以测试原型形式完成，结论是当前技术方案可以支撑 Feign 注解式 Body/Response Body 加密处理；本阶段未交付正式 Feign 生产代码。
- Query 加密、Query 签名和正式 Feign 集成留待后续阶段。

## 追加验证

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcOptionalModeTest,SdeWebMvcPlainModeTest,SdeWebMvcResponseKeyRefTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 修复前执行失败，失败点符合预期：
  - `OPTIONAL` 模式明文请求被当作 secure envelope 解码。
  - `PLAIN` 模式未拒绝 secure envelope。
  - 响应封装缺少请求 `keyRef` 时未抛出预期异常。
- 修复后执行通过：4 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 放行后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：9 个测试通过，覆盖新增 Web MVC 模式回归。

### `.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`

- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-cloud-rpc-feign`：Feign SDE 原型测试 7 个通过，0 failures，0 errors，0 skipped。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcOptionalModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 修复前执行失败，失败点符合预期：入站 envelope 携带不匹配的 `policyId` 时仍被当前策略处理。
- 修复后执行通过：3 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 追加修复后再次执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 追加修复后再次执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：10 个测试通过，覆盖 `policyId` 不匹配拒绝场景。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 修复前执行失败，失败点符合预期：`cryptoEnabled=true` 且 `signatureEnabled=false`、安全交换下两者都关闭时仍可启动。
- 修复后执行通过：6 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcSignOnlyTest,SdeAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 修复前执行失败，失败点符合预期：sign-only Web 管线仍尝试解析 `CryptoHandler`。
- 修复后执行通过：7 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 本轮沙箱内首次执行失败：Maven Central 访问被拒，父级 BOM 无法解析。
- 放行后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-web`：重新编译 4 个主源码文件，无独立测试类。
- `fist-sde-boot-starter`：15 个测试通过，覆盖策略开关组合、sign-only Web MVC、`OPTIONAL`、`PLAIN`、响应 `keyRef` 和 `policyId` 不匹配拒绝场景。

### `.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`

- 放行后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-cloud-rpc-feign`：Feign SDE 原型测试 7 个通过，0 failures，0 errors，0 skipped。
- 本阶段仍未新增正式 Feign 生产代码，原型代码仅用于技术验证测试。
