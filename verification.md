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

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcAnnotationModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 红测阶段执行失败，失败点符合预期：Web Advice 未解析 `@SecureBody` / `@SecureExchange`，方法级明文策略仍按默认 `REQUIRED` 解析 envelope，冲突注解也未抛出冲突错误。
- 修复后执行通过：2 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 注解策略修复后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 注解策略修复后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：17 个测试通过，新增覆盖方法级 `@SecureBody` 优先于类级 `@SecureExchange`、同一方法注解冲突拒绝。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcExceptionTranslatorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 红测阶段执行失败，失败点符合预期：存在 `SecureExchangeExceptionTranslator` Bean 时，Web 侧仍抛出原始 `SecureEnvelopeException`。
- 修复后执行通过：1 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 异常 translator 修复后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- 异常 translator 修复后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：18 个测试通过，新增覆盖自定义 `SecureExchangeExceptionTranslator` 转换 Web 侧 SDE 异常。

### `git diff --check`

- 新增 `docs/public/develop/sde-protocol.md` 后执行通过，无空白错误。

### `rg -n '你|您|同学|“|”|payloadDigest|keyId|@SecureQuery|SecureQuery' docs/public/develop/sde-protocol.md CHANGELOG.md`

- 文档排版和禁用术语扫描仅命中 `payloadDigest` 的否定说明：`不使用 payloadDigest`。
- 未发现第二人称、中文双引号、`keyId`、`@SecureQuery` 或 `SecureQuery`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcOptionalModeTest,SdeWebMvcPlainModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 红测阶段执行失败，失败点符合预期：`OPTIONAL` 和 `PLAIN` 模式会把缺少 `sign` 的 envelope-like JSON 当作明文放行到控制器。
- 修复后执行通过：6 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- envelope-like 缺字段修复后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- envelope-like 缺字段修复后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：20 个测试通过，新增覆盖 `OPTIONAL` 和 `PLAIN` 模式拒绝缺字段 envelope-like 请求。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 红测阶段先因测试辅助方法误 catch `IOException` 编译失败，修正测试后空 body 用例失败，失败点符合预期：Spring MVC 直接返回 `HttpMessageNotReadableException`，未走 SDE required body 异常。
- 修复后执行通过：6 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。
- 新增覆盖 required 模式空 body、缺少 `sign` 字段和错误签名。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcModeTest,SdeWebMvcPlainModeTest,SdeWebMvcOptionalModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 执行通过：6 个测试通过，0 failures，0 errors，0 skipped，Reactor 输出 `BUILD SUCCESS`。
- 覆盖 `OPTIONAL`、`PLAIN` 及 envelope-like 缺字段拒绝回归。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- required body 失败分支修复后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`

- required body 失败分支修复后按默认参数执行失败。
- 根因定位为当前 Windows 跨盘环境触发 surefire manifest-only jar classpath 问题：dumpstream 输出 `Boot Manifest-JAR contains absolute paths`、`'other' has different root`。
- 单独执行 `SdeWebMvcTest` 可通过，说明不是代码路径或新增用例本身失败。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml "-Dsurefire.useManifestOnlyJar=false" clean test`

- required body 失败分支修复后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，5 个测试通过。
- `fist-sde-boot-starter`：23 个测试通过，新增覆盖 required 模式空 body、缺签名和错误签名。

### `rg -n "payloadDigest|keyId|@SecureQuery|SecureQuery|spring\.factories|List\.of|Map\.of|Set\.of|Stream\.toList|\bvar\b|\brecord\b" fist-kit-infra/fist-sde/fist-sde-core/src/main fist-kit-infra/fist-sde/fist-sde-extra/src/main fist-kit-infra/fist-sde/fist-sde-web/src/main fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main`

- 执行无命中，退出码 1 表示未发现匹配项。
- 未发现禁用术语、正式 Query、正式 Feign 注册入口、新增 `spring.factories` 或 core/extra 主源码 Java 9+ API / 语法。

### `git diff --check`

- 执行通过，无空白错误。

### BOM 集成复查

- `fist-sde` 已纳入 `fist-kit-infra/pom.xml` reactor。
- 补充 `fist-kit-dependencies/pom.xml` 中 `fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 的 BOM 版本管理条目。

### `.\mvnw.cmd -pl fist-kit-dependencies validate`

- BOM 集成修复后执行通过：`fist-kit-dependencies` 输出 `BUILD SUCCESS`，并执行 `spring-javaformat:validate`。

### `git diff --check`

- BOM 集成修复后执行通过，无空白错误。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-extra -am "-Dtest=InMemoryReplayGuardTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- ReplayGuard 时间窗口修复后执行通过：`InMemoryReplayGuardTest` 2 个测试通过，0 failures，0 errors，0 skipped。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- ReplayGuard 时间窗口修复后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml "-Dsurefire.useManifestOnlyJar=false" clean test`

- ReplayGuard 时间窗口修复后按该参数执行失败。
- 失败点为当前 Windows 跨盘环境下 surefire fork 加载测试 classpath：报告包含 `ClassNotFoundException: com.power4j.fist.sde.boot.autoconfigure.SdeWebMvcTest`，并非 ReplayGuard 断言失败。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml "-DforkCount=0" clean test`

- ReplayGuard 时间窗口修复后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 8 个主源码文件，6 个测试通过。
- `fist-sde-boot-starter`：23 个测试通过。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-extra -am "-Dtest=Sm4GcmCryptoHandlerTest,HmacSm3SignatureHandlerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`

- 国密 extra 实现后执行通过：`Sm4GcmCryptoHandlerTest` 和 `HmacSm3SignatureHandlerTest` 共 4 个测试通过，0 failures，0 errors，0 skipped。
- 覆盖 Provider 存在时 SM4-GCM 加解密、HMAC-SM3 签名验签，以及 Provider 缺失时的明确异常。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`

- 国密 extra 实现后执行通过：`fist-sde`、`fist-sde-core`、`fist-sde-extra`、`fist-sde-web`、`fist-sde-boot-starter` 全部 `SUCCESS`，Maven 输出 `BUILD SUCCESS`。

### `rg -n "payloadDigest|keyId|@SecureQuery|SecureQuery|spring\.factories|List\.of|Map\.of|Set\.of|Stream\.toList|\bvar\b|\brecord\b" fist-kit-infra/fist-sde/fist-sde-core/src/main fist-kit-infra/fist-sde/fist-sde-extra/src/main fist-kit-infra/fist-sde/fist-sde-web/src/main fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main`

- 执行无命中，退出码 1 表示未发现匹配项。
- 未发现禁用术语、正式 Query、正式 Feign 注册入口、新增 `spring.factories` 或 core/extra 主源码 Java 9+ API / 语法。

### `git diff --check`

- 国密 extra 实现后执行通过，无空白错误。

### `.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml "-DforkCount=0" clean test`

- 国密 extra 实现后执行通过：Reactor 输出 `BUILD SUCCESS`。
- `fist-sde-core`：以 `release 8` 重新编译 43 个主源码文件，4 个测试通过。
- `fist-sde-extra`：以 `release 8` 重新编译 9 个主源码文件，10 个测试通过。
- `fist-sde-boot-starter`：23 个测试通过。
- 继续使用 `-DforkCount=0` 避免当前 Windows 跨盘环境下 surefire fork classpath 问题。

### `Test-Path fist-kit-infra/fist-sde/README.md, docs/public/develop/sde-protocol.md`

- 模块 README 和链接目标均存在。

### `rg -n "你|您|同学|“|”|payloadDigest|keyId|@SecureQuery|SecureQuery|spring\.factories" fist-kit-infra/fist-sde/README.md CHANGELOG.md`

- 模块 README 接入说明补充后执行扫描。
- 仅命中 `payloadDigest` 的否定说明：`不使用 payloadDigest`。
- 未发现第二人称、中文双引号、`keyId`、`@SecureQuery`、`SecureQuery` 或 `spring.factories`。

### `git diff --check`

- 模块 README 接入说明补充后执行通过，无空白错误。
