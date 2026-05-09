# FIST SDE Client Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将示例中的客户端 envelope 固定流程沉入正式 SDK，使 Feign、RestClient、WebClient 用户能以少量配置接入 SDE。

**Architecture:** 新增单模块 `fist-sde-client`，在模块内提供客户端无关的 `SecureExchangeOperations`，再分别提供 Feign、RestClient、WebClient 适配。HTTP 客户端相关依赖使用 optional，starter 通过 `@ConditionalOnClass` 条件注册对应 Bean。

**Tech Stack:** Java 8 兼容源码、Spring Boot 3 自动配置、Spring MVC、Spring RestClient、Spring WebClient、Spring Cloud OpenFeign、JUnit 5。

---

## 元数据

- 日期：2026-05-09
- 执行者：Codex
- 目标分支：`cj/sde-client`
- 基线分支：`sde`
- 相关现状：
  - `fist-sde-core` 已提供 envelope、策略、签名规范化、密钥、nonce、重放校验和异常契约。
  - `fist-sde-web` 已提供 Spring MVC `RequestBodyAdvice` / `ResponseBodyAdvice`。
  - `fist-sde-extra` 已提供 AES-GCM、SM4-GCM、HMAC-SHA256、SM3、HMAC-SM3、随机 nonce、静态密钥解析和内存 replay guard。
  - `examples/fist-sde/example-feign` 和 `example-web` 中仍有客户端 envelope 固定流程代码，需要迁移为正式 SDK。

## 设计决策

### 1. 单模块承载所有 HTTP 客户端

新增 `fist-kit-infra/fist-sde/fist-sde-client`，统一放置客户端通用能力和 HTTP 客户端适配。Feign、RestClient、WebClient 外部依赖在该模块中声明为 optional，避免下游应用被动拉入未使用的客户端栈。

### 2. 命名统一

- 标准版实现统一使用 `Standard` 前缀。
- 国密实现继续使用 `Gm` 前缀。
- 测试和示例实现使用 `Test` 前缀，避免被误认为生产默认实现。

首阶段不强制重命名已有 `fist-sde-extra` 类，以降低破坏面；新增客户端预设类使用新命名，并可委托现有实现。

### 3. starter 与自动配置

`fist-sde-boot-starter` 增加对 `fist-sde-client` 的依赖。新增自动配置只使用 Spring Boot 3 的 `AutoConfiguration.imports`，不新增 `spring.factories`。

自动配置拆分：

- `SdeClientAutoConfiguration`：客户端通用 `SecureExchangeOperations`。
- `SdeFeignClientAutoConfiguration`：classpath 存在 Feign 时注册 Feign Encoder / Decoder。
- `SdeRestClientAutoConfiguration`：classpath 存在 `RestClient` 时注册 RestClient 拦截器或定制器。
- `SdeWebClientAutoConfiguration`：classpath 存在 `WebClient` 时注册 `ExchangeFilterFunction`。

### 4. 安全默认值

starter 不隐式带入生产算法、静态密钥或测试级 replay guard。标准版、国密版和测试版预设配置需要用户显式 import 或通过属性显式启用。示例可以使用测试级密钥和 replay guard，但必须在 README 中说明生产替换要求。

### 5. 客户端日志

客户端 SDK 提供统一日志钩子，默认不打印明文 body。示例项目通过配置打开演示日志，输出 raw body、request envelope、response envelope 和 decrypted response，便于观察流程。

## 文件结构

### 新增模块

- Create: `fist-kit-infra/fist-sde/fist-sde-client/pom.xml`
- Modify: `fist-kit-infra/fist-sde/pom.xml`
- Modify: `fist-kit-infra/fist-sde/fist-sde-boot-starter/pom.xml`
- Modify: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### 客户端核心

- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureExchangeOperations.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/DefaultSecureExchangeOperations.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureExchangeClientContext.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureExchangeClientProperties.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureExchangeClientLogger.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureRequestEnvelopeEncoder.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/SecureResponseEnvelopeDecoder.java`

### 算法预设

- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/preset/StandardSecureExchangeClientConfiguration.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/preset/GmSecureExchangeClientConfiguration.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/preset/TestSecureExchangeClientConfiguration.java`

### HTTP 客户端适配

- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/feign/SecureFeignEncoder.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/feign/SecureFeignDecoder.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/restclient/SecureRestClientInterceptor.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/webclient/SecureWebClientExchangeFilterFunction.java`

### 自动配置

- Create: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/java/com/power4j/fist/sde/boot/autoconfigure/client/SdeClientAutoConfiguration.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/java/com/power4j/fist/sde/boot/autoconfigure/client/SdeFeignClientAutoConfiguration.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/java/com/power4j/fist/sde/boot/autoconfigure/client/SdeRestClientAutoConfiguration.java`
- Create: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/java/com/power4j/fist/sde/boot/autoconfigure/client/SdeWebClientAutoConfiguration.java`

### 示例

- Modify: `examples/fist-sde/example-web/**`
- Modify: `examples/fist-sde/example-feign/**`
- Create: `examples/fist-sde/example-restclient/**`
- Create: `examples/fist-sde/example-webclient/**`
- Modify: `examples/fist-sde/pom.xml`
- Create: `examples/fist-sde/README.md`

## 实施任务

### Task 1: 新增计划文档并提交

**Files:**
- Create: `docs/local/plans/20260509-sde-client/fist-sde-client-implementation-plan-1.0.md`

- [ ] **Step 1: 保存本文档**

使用 `apply_patch` 新增计划文档。

- [ ] **Step 2: 检查工作区**

Run: `git status --short`

Expected: 只出现计划文档新增。

- [ ] **Step 3: 提交计划文档**

Run:

```powershell
git add docs/local/plans/20260509-sde-client/fist-sde-client-implementation-plan-1.0.md
git commit -m "docs: plan sde client integration"
```

Expected: commit 成功。

### Task 2: 新增 `fist-sde-client` 模块骨架

**Files:**
- Modify: `fist-kit-infra/fist-sde/pom.xml`
- Create: `fist-kit-infra/fist-sde/fist-sde-client/pom.xml`

- [ ] **Step 1: 写模块构建文件**

`fist-sde-client` 依赖 `fist-sde-core`，外部客户端依赖使用 optional。测试依赖可以使用 `fist-sde-extra`、`spring-boot-starter-test`、`spring-cloud-starter-openfeign`、`spring-boot-starter-webflux`。

- [ ] **Step 2: 验证 Maven 可识别模块**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am test`

Expected: 新模块参与 reactor，暂无测试或测试通过。

- [ ] **Step 3: 提交模块骨架**

Run:

```powershell
git add fist-kit-infra/fist-sde/pom.xml fist-kit-infra/fist-sde/fist-sde-client/pom.xml
git commit -m "feat: add sde client module"
```

### Task 3: 实现客户端核心编排

**Files:**
- Create core files under `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/`
- Test: `fist-kit-infra/fist-sde/fist-sde-client/src/test/java/com/power4j/fist/sde/client/DefaultSecureExchangeOperationsTest.java`

- [ ] **Step 1: 写失败测试**

测试应覆盖：

- 编码 request body 得到 `SecureEnvelope` JSON。
- request envelope 包含 `version=1`、`scope=body`、`keyRef`、`policyId`。
- 解码 response envelope 后返回原始 JSON 字节。
- 日志默认不打印明文，开启后打印 raw body 和 envelope。

- [ ] **Step 2: 运行测试观察失败**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am -Dtest=DefaultSecureExchangeOperationsTest test`

Expected: 编译失败或测试失败，原因是核心类不存在。

- [ ] **Step 3: 实现核心类**

实现要求：

- Java 8 源码兼容，禁止 `var`、`record`、`List.of`、`Map.of`、`Stream.toList`。
- 使用已有 `SecurePolicyRegistry` 获取策略。
- 使用已有 `SecureEnvelopeCodec` 编解码 envelope。
- 使用已有 `SignatureCanonicalizer` 生成稳定签名输入。
- 签名输入继续直接包含密文 payload，不引入 `payloadDigest`。
- 使用 `keyRef` 和 `policyId`。
- 不让 `SecureEnvelopeCodec` 承担验签、重放校验、加解密职责。

- [ ] **Step 4: 运行测试通过**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am test`

Expected: `BUILD SUCCESS`。

- [ ] **Step 5: 提交核心编排**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-client
git commit -m "feat: add sde client operations"
```

### Task 4: 实现标准版、国密版和测试版预设

**Files:**
- Create preset files under `fist-kit-infra/fist-sde/fist-sde-client/src/main/java/com/power4j/fist/sde/client/preset/`
- Test: `fist-kit-infra/fist-sde/fist-sde-client/src/test/java/com/power4j/fist/sde/client/preset/SecureExchangeClientPresetTest.java`

- [ ] **Step 1: 写失败测试**

测试应验证：

- 标准版配置注册 AES-GCM、HMAC-SHA256、随机 nonce。
- 国密版配置注册 SM4-GCM、HMAC-SM3。
- 测试版配置注册静态密钥和内存 replay guard。
- 预设配置不会在 starter 默认路径中自动启用。

- [ ] **Step 2: 实现预设配置**

标准版命名统一使用 `Standard`，国密版使用 `Gm`，测试版使用 `Test`。

- [ ] **Step 3: 运行测试**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am test`

- [ ] **Step 4: 提交预设**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-client
git commit -m "feat: add sde client presets"
```

### Task 5: 正式化 Feign 客户端集成

**Files:**
- Create: `SecureFeignEncoder.java`
- Create: `SecureFeignDecoder.java`
- Test: `SecureFeignClientTest.java`

- [ ] **Step 1: 写失败测试**

测试应使用真实 Feign self-call，验证：

- `@SecureExchange("body-strict-v1")` 被读取。
- Feign request body 被编码为 SDE request envelope。
- 服务端 MVC advice 解密后 Controller 收到 POJO。
- response envelope 被 Feign decoder 解密为业务对象。

- [ ] **Step 2: 实现 Feign Encoder / Decoder**

复用 `SecureExchangeOperations`，不要复制 `ExampleFeignEnvelopeSupport` 逻辑。

- [ ] **Step 3: 运行 Feign 测试**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am -Dtest=SecureFeignClientTest test`

- [ ] **Step 4: 提交 Feign 集成**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-client
git commit -m "feat: support sde feign client"
```

### Task 6: 实现 RestClient 客户端集成

**Files:**
- Create: `SecureRestClientInterceptor.java`
- Test: `SecureRestClientTest.java`

- [ ] **Step 1: 写失败测试**

测试应使用真实 Spring Boot server 和 `RestClient`，验证 request envelope 和 response envelope 处理。

- [ ] **Step 2: 实现 RestClient 拦截器**

复用 `SecureExchangeOperations`。只处理 JSON Body，不处理 multipart，不处理 Query。

- [ ] **Step 3: 运行测试**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am -Dtest=SecureRestClientTest test`

- [ ] **Step 4: 提交 RestClient 集成**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-client
git commit -m "feat: support sde restclient"
```

### Task 7: 实现 WebClient 客户端集成

**Files:**
- Create: `SecureWebClientExchangeFilterFunction.java`
- Test: `SecureWebClientTest.java`

- [ ] **Step 1: 写失败测试**

测试应使用 `WebClient` 调用本地 Spring Boot server，验证 request envelope 和 response envelope 处理。

- [ ] **Step 2: 实现 ExchangeFilterFunction**

注意释放或重建响应 body，避免 DataBuffer 泄漏。只处理 JSON Body，不处理 multipart，不处理 Query。

- [ ] **Step 3: 运行测试**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client -am -Dtest=SecureWebClientTest test`

- [ ] **Step 4: 提交 WebClient 集成**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-client
git commit -m "feat: support sde webclient"
```

### Task 8: 接入 starter 自动配置

**Files:**
- Modify: `fist-kit-infra/fist-sde/fist-sde-boot-starter/pom.xml`
- Create auto-config classes under `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/java/com/power4j/fist/sde/boot/autoconfigure/client/`
- Modify: `fist-kit-infra/fist-sde/fist-sde-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Test: `SdeClientAutoConfigurationTest.java`

- [ ] **Step 1: 写失败测试**

使用 `ApplicationContextRunner` 验证：

- 未启用 `fist.sde.enabled` 时不注册客户端核心 Bean。
- 启用后注册 `SecureExchangeOperations`。
- classpath 有 Feign / RestClient / WebClient 时才注册对应适配 Bean。
- 不自动注册测试级密钥解析和内存 replay guard。

- [ ] **Step 2: 实现自动配置**

条件类使用 `@ConditionalOnClass(name = "...")`，避免 optional 依赖缺失时类加载失败。

- [ ] **Step 3: 运行 starter 测试**

Run: `cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-boot-starter -am test`

- [ ] **Step 4: 提交自动配置**

Run:

```powershell
git add fist-kit-infra/fist-sde/fist-sde-boot-starter
git commit -m "feat: auto configure sde clients"
```

### Task 9: 改造并新增 examples

**Files:**
- Modify: `examples/fist-sde/example-web/**`
- Modify: `examples/fist-sde/example-feign/**`
- Create: `examples/fist-sde/example-restclient/**`
- Create: `examples/fist-sde/example-webclient/**`
- Modify: `examples/fist-sde/pom.xml`
- Create: `examples/fist-sde/README.md`

- [ ] **Step 1: 改造 `example-web`**

删除或弱化 `ExampleSecureEnvelopeClient` 固定套路，改用正式 `SecureExchangeOperations`。

- [ ] **Step 2: 改造 `example-feign`**

删除示例内 `DemoSecureFeignEncoder`、`DemoSecureFeignDecoder`、`ExampleFeignEnvelopeSupport`，改用正式 SDK。

- [ ] **Step 3: 新增 `example-restclient`**

展示 `RestClient` 同步调用方式。

- [ ] **Step 4: 新增 `example-webclient`**

展示 `WebClient` 响应式调用方式。

- [ ] **Step 5: 更新 README**

总 README 说明每个示例适用对象，明确演示日志和生产替换项。

- [ ] **Step 6: 运行示例测试**

Run:

```powershell
cmd /c mvnw.cmd -Pexamples -pl examples/fist-sde/example-web,examples/fist-sde/example-feign,examples/fist-sde/example-restclient,examples/fist-sde/example-webclient -am test
```

- [ ] **Step 7: 提交示例改造**

Run:

```powershell
git add examples/fist-sde
git commit -m "feat: demonstrate sde clients"
```

### Task 10: 最终验证

**Files:**
- All changed files.

- [ ] **Step 1: 运行模块级测试**

Run:

```powershell
cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client,fist-kit-infra/fist-sde/fist-sde-boot-starter -am test
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 2: 运行示例测试**

Run:

```powershell
cmd /c mvnw.cmd -Pexamples -pl examples/fist-sde/example-web,examples/fist-sde/example-feign,examples/fist-sde/example-restclient,examples/fist-sde/example-webclient -am test
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: 运行格式检查**

Run:

```powershell
cmd /c mvnw.cmd -pl fist-kit-infra/fist-sde/fist-sde-client,fist-kit-infra/fist-sde/fist-sde-boot-starter spring-javaformat:validate
```

Expected: `BUILD SUCCESS`。如失败，运行对应模块 `spring-javaformat:apply` 后重新验证并提交格式化结果。

- [ ] **Step 4: 检查 diff**

Run: `git diff --check`

Expected: 无错误。

- [ ] **Step 5: 汇总结论**

交付说明必须包含：

- worktree 路径和分支名。
- 新增模块与示例。
- 每次提交列表。
- 已执行验证命令及结果。
- Feign 是否已从原型升级为正式 SDK 能力。
- RestClient / WebClient 剩余风险。

## 风险与约束

- WebClient 响应 body 重写涉及 reactive buffer 生命周期，必须单独测试。
- optional 依赖的自动配置类必须避免直接类加载缺失依赖。
- 标准版和国密版预设不能隐式启用测试级密钥或 replay guard。
- 不实现 Query 加密或签名。
- 不修改 envelope 协议字段：继续使用 `keyRef`、`policyId`，`version` 只表示协议格式版本。
- `SecureEnvelopeCodec` 继续只负责 envelope 编解码。
- 新增 infra 代码保持 JDK 8 源码兼容。
