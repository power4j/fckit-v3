# operations-log

## 2026-05-08

- 工具：`rg --files docs\local\plans\20260508-sde`  
  摘要：确认目录包含 `fist-sde-technical-solution-1.1.md` 和 `fist-sde-technical-solution-1.1-review.md`。
- 工具：`Get-Content`  
  摘要：读取 1.1 方案、评审报告和关键段落。
- 工具：`rg --files D:\git-repo\mica\mica`  
  摘要：定位 `mica-api-encrypt` 模块源码。
- 工具：`Get-Content`  
  摘要：读取 `ApiDecryptRequestBodyAdvice`、`ApiEncryptResponseBodyAdvice`、`ApiDecryptParamResolver`、`ApiEncryptConfiguration`、`ApiEncryptParamConfiguration`、`ApiEncryptProperties`、`ApiCryptoUtil`、`build.gradle` 和 `MODULE.md`。
- 决策：1.2 方案删除 Query 能力，保留 Body 和 Response Body；Feign 先做技术验证；Spring MVC 集成借鉴 mica 的 Advice 扩展点，但不采用 `mica-auto` 和 URL 参数解密。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.2.md`。
- 工具：`Get-Content`、`rg`  
  摘要：读取 `fist-sde-technical-solution-1.2-review.md` 和 1.2 方案关键段落，核对评审意见。
- 决策：1.3 删除 `payloadDigest`，改为直接签 `payload`；`keyId` 改为 `keyRef`；`policy` 改为 `policyId`；`SecureMessageCodec` 改为 `SecureEnvelopeCodec`。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.3.md`。
- 工具：`Get-Content`、`rg`  
  摘要：读取 `fist-sde-technical-solution-1.3-review.md`，核对 1.3 评审新增的 5 条建议。
- 决策：1.4 只收敛 1.3 评审新增建议，不改变 1.3 主体架构；清理旧版本方案和评审报告，只保留 1.4 方案。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.4.md`，删除 1.1、1.2、1.3 旧文档和评审报告。
- 工具：`git branch --show-current`、`git status --short`、`Get-Content`、`rg`
  摘要：在 worktree `D:\git-repo\fist-dev\fist-cloud-kit-v3\.worktrees\cj-fist-sde` 确认分支为 `cj/fist-sde`，读取根 `AGENTS.md`、`fist-kit-infra/pom.xml`、1.4 技术方案和新增 `fist-sde` POM，复查无 `payloadDigest`、`keyId`、`@SecureQuery`、正式 Feign 代码或 `spring.factories`。
- 产物：更新 `CHANGELOG.md` 的 `Unreleased` 区段，记录 `fist-sde` 首阶段用户可见新增能力；新增 `.codex/testing.md` 记录本地验证结果。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`
  摘要：沙箱内首次因 Maven Central 访问被拒失败；放行后 `fist-sde` reactor 全模块 `SUCCESS`，`BUILD SUCCESS`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml test`
  摘要：沙箱内首次因 Maven Central 访问被拒失败；放行后测试通过，core 4 个、extra 5 个、boot-starter 5 个测试均 0 failures，`BUILD SUCCESS`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`、`rg`
  摘要：沙箱内首次因 Maven Central 访问被拒失败；放行后从干净 target 重新编译并测试通过，其中 core 43 个主源码文件和 extra 8 个主源码文件均以 `release 8` 编译。复查新增 Java 主源码未发现 `List.of`、`Map.of`、`Stream.toList`、`record`、`var` 等 Java 9+ API 或语法。
- 工具：`rg`
  摘要：复查 `fist-sde` 内未发现 `payloadDigest`、`keyId`、`@SecureQuery`、`SecureQuery` 或新 `spring.factories`；Feign SDE 相关新增代码仅位于 `fist-cloud-rpc-feign` 的测试原型包。
- 工具：`.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=*PrototypeTest,FeignResponseRebuildTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：沙箱内首次因 Maven Central 访问被拒失败；放行后 Feign SDE 原型 7 个测试通过，覆盖注解元数据、Encoder、Decoder、Response 重建和原型自动配置，Reactor `BUILD SUCCESS`。
- 工具：`.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`
  摘要：放行后 `fist-cloud-rpc-feign` 及其依赖模块测试通过，其中 Feign SDE 原型 7 个测试通过，Reactor `BUILD SUCCESS`；Maven 生命周期中执行了相关模块的 `spring-javaformat:validate`。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-feign-technical-validation.md`，记录 Feign 注解式 Body / Response Body 加密的技术验证结论、测试覆盖和正式实现前的剩余风险。
- 工具：`Get-Content`、`rg`、`git diff`
  摘要：提交后复查 `SecureRequestBodyAdvice`、`SecureResponseBodyAdvice`、`SecureWebExchangeService`、自动配置和 MVC 测试，确认 `OPTIONAL`、`PLAIN` 和响应 `keyRef` 处理存在边界缺口。
- 产物：修复 `SecureRequestBodyAdvice` 的 `OPTIONAL` 明文放行和 `PLAIN` 安全报文拒绝逻辑；移除响应加密路径中的硬编码 `tenant-a` 回退，缺少响应 `keyRef` 时抛出明确 `SecureKeyResolveException`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcOptionalModeTest,SdeWebMvcPlainModeTest,SdeWebMvcResponseKeyRefTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：新增回归测试修复前失败，修复后 4 个测试通过，覆盖 `OPTIONAL` 明文、`OPTIONAL` 安全请求响应封装、`PLAIN` 拒绝安全 envelope 和响应缺少 `keyRef`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`、`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`、`.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`
  摘要：全部放行后通过；`fist-sde` 全模块格式校验和测试 `BUILD SUCCESS`，Feign 原型模块及依赖测试 `BUILD SUCCESS`。
- 工具：`Get-Content`、`rg`
  摘要：继续复查 `policyId` 入站处理，确认 envelope 中已签名的 `policyId` 未与当前处理策略校验。
- 产物：新增 `SdeWebMvcOptionalModeTest.shouldRejectEnvelopeWhenPolicyIdDoesNotMatchCurrentPolicy`，并修复 `SecureWebExchangeService`：当入站 envelope 显式携带 `policyId` 时必须匹配当前策略 ID；未携带时仍允许固定服务端策略处理。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcOptionalModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：修复前新增测试失败并复现策略不匹配仍被放行；修复后 3 个测试通过，Reactor `BUILD SUCCESS`。
- 工具：`rg`、`Get-Content`
  摘要：继续复查 `cryptoEnabled` / `signatureEnabled`，确认策略开关已绑定但缺少 1.4 方案要求的组合校验和 sign-only 运行期语义。
- 产物：新增 `SdeAutoConfigurationTest` 策略开关组合测试；新增 `SdeWebMvcSignOnlyTest` 验证无 `CryptoHandler` 时签名-only 请求和响应仍可处理；修复 `SdeCoreAutoConfiguration` 策略校验和 `SecureWebExchangeService` sign-only 路径。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：修复前非法策略组合测试失败，证明当前实现未拒绝；修复后 6 个测试通过，Reactor `BUILD SUCCESS`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcSignOnlyTest,SdeAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：修复前 sign-only MVC 测试因缺少 `CryptoHandler` 失败；修复后 7 个测试通过，Reactor `BUILD SUCCESS`。
- 工具：`rg`、`git diff --check`
  摘要：复查 SDE 生产源码未发现 `payloadDigest`、`keyId`、`@SecureQuery`、`SecureQuery`、新增 `spring.factories` 或 Java 9+ API / 语法；`fist-sde-extra` 仅作为 boot-starter 测试依赖出现；diff 空白检查通过。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`、`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`、`.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test`
  摘要：提交前验证通过；SDE 全模块格式校验 `BUILD SUCCESS`，SDE `clean test` 通过并确认 core/extra 主源码 `release 8` 编译，Feign 模块全量测试通过且原型测试 7 个通过。
- 工具：`rg`、`Get-Content`
  摘要：继续对照 1.4 方案复查注解优先级，确认 `@SecureBody` / `@SecureExchange` 已定义但 Web Advice 始终使用默认策略，未实现方法级、类级注解选择和冲突处理。
- 产物：新增 `SdeWebMvcAnnotationModeTest` 红测，覆盖方法级 `@SecureBody` 覆盖类级 `@SecureExchange` 以及同一方法注解冲突。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcAnnotationModeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：红测阶段失败原因符合预期：明文请求仍被默认 required 策略当作 envelope，冲突注解未返回冲突错误；实现 `SecureWebExchangeService.policy(MethodParameter)` 后 2 个测试通过，Reactor `BUILD SUCCESS`。
- 产物：修复 `SecureRequestBodyAdvice` 和 `SecureResponseBodyAdvice`，按方案优先级解析方法/类级 `@SecureBody`、`@SecureExchange`，并在选中策略基础上应用 request / response 显式覆盖；同一层级双注解抛出明确 `SecureEnvelopeException`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`、`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`、`rg`、`git diff --check`
  摘要：注解策略修复提交前验证通过；SDE 全模块格式校验 `BUILD SUCCESS`，SDE `clean test` 通过，boot-starter 17 个测试通过，core/extra 主源码仍以 `release 8` 编译；约束扫描无命中，diff 空白检查通过。
- 工具：`rg`、`Get-Content`
  摘要：继续复查 `SecureExchangeExceptionTranslator`，确认 core 接口已定义且方案要求 Web 侧存在 translator Bean 时先转换异常，但自动配置和 Advice 尚未接入。
- 产物：新增 `SdeWebMvcExceptionTranslatorTest` 红测，覆盖存在 translator Bean 时入站 SDE 异常应转换为应用异常。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml -pl fist-sde-boot-starter -am "-Dtest=SdeWebMvcExceptionTranslatorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
  摘要：红测阶段失败原因符合预期：根因仍为原始 `SecureEnvelopeException`；接入 translator 后 1 个测试通过，Reactor `BUILD SUCCESS`。
- 产物：`SdeWebAutoConfiguration` 通过 `ObjectProvider<SecureExchangeExceptionTranslator>` 注入可选 translator；`SecureRequestBodyAdvice`、`SecureResponseBodyAdvice` 捕获 `SecureExchangeException` 后按当前报文域和方向交给 `SecureWebExchangeService.translate(...)`。
- 工具：`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml spring-javaformat:validate`、`.\mvnw.cmd -f fist-kit-infra/fist-sde/pom.xml clean test`、`rg`、`git diff --check`
  摘要：异常 translator 修复提交前验证通过；SDE 全模块格式校验 `BUILD SUCCESS`，SDE `clean test` 通过，boot-starter 18 个测试通过，core/extra 主源码仍以 `release 8` 编译；约束扫描无命中，diff 空白检查通过。
- 工具：`rg`
  摘要：对照 1.4 方案的协议规范文档要求，确认除本地技术方案和 Feign 验证报告外，尚无面向跨语言调用方的正式协议规范文档。
- 产物：新增 `docs/public/develop/sde-protocol.md`，覆盖 envelope 字段、`version` / `scope` / `keyRef` / `policyId` / `algorithm` 语义、payload 编码、canonical text、签名伪代码、Request Body / Response Body 示例、错误排查和首阶段边界；更新 `CHANGELOG.md` 的 `Unreleased`。
- 工具：`git diff --check`、`rg`
  摘要：文档提交前验证通过；无空白错误，未发现第二人称、中文双引号、`keyId`、`@SecureQuery` 或 `SecureQuery`；`payloadDigest` 仅作为否定说明出现。
