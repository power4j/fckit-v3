# fist-sde 技术方案（1.4）

> 生成日期：2026-05-08  
> 执行者：Codex  
> 基于版本：`fist-sde` 技术方案 1.3、1.3 评审报告、后续术语与签名契约讨论

## 修订说明

1.4 版本在 1.3 版本基础上做出以下调整：

- 明确 `version` 的兼容约束，避免未来 canonicalization 规则升级时形成解析循环依赖。
- 补充 `SecureEnvelopeContext` 和 `SecureExchangeContext` 的字段边界。
- 明确可选字段为空字符串时视为不存在，不参与签名；必填字段为空字符串时视为无效 envelope。
- 收紧 `SecureEnvelopeCodec.encodeToBody` 返回值约定。
- 补充 `SecureKeyResolver` 对 `keyRef + usage` 到密钥材料映射的职责边界。

1.3 已完成的关键调整在 1.4 中继续保留：

- 删除 `payloadDigest`，签名输入直接包含规范化后的密文 `payload`。
- 将 `keyId` 改为 `keyRef`。
- 将 `policy` 改为 `policyId`。
- 将 `SecureMessageCodec` 改名为 `SecureEnvelopeCodec`。

## 目标

`fist-sde` 面向服务间和应用接口的数据安全交换，提供 Request Body 解密验签、Response Body 加密签名、客户端侧请求加密签名和响应解密验签等能力。

模块设计应保持低侵入、可配置、可测试，并符合 FIST Kit v3 的 Maven 多模块、Spring Boot 3 自动配置和现有 Feign 集成边界。

## 非目标

以下能力不进入 1.4 方案范围：

- 不做 URL Query 参数加密。
- 不做 URL Query 参数签名。
- 不提供透明 `HttpServletRequestWrapper` 替换 Query 参数。
- 不支持 `multipart/form-data` 请求体加密。
- 不把 HTTP method、path、query string、header 纳入默认签名输入。
- 不定义 `payloadDigest`。
- 不在 starter 中隐式引入默认算法实现或测试级实现。
- 不在第一阶段交付 Feign 正式实现。

Query 不再作为安全报文域。新接口如需传输敏感参数，应使用 Request Body 或 Header；既有 GET 接口如确实存在敏感 Query，建议通过接口改造迁移，而不是在 URL 上叠加加密。

## 术语定义

| 术语 | 语义 |
| --- | --- |
| `version` | SDE envelope 协议格式版本，用于 envelope 字段结构、canonicalization 规则、时间格式等协议层升级 |
| `scope` | 报文域，当前只允许 `body` 和 `responseBody` |
| `payload` | 传输态密文文本，通常为 Base64URL 或其他策略约定的安全文本 |
| `signature` | 对 canonicalized signing input 的签名结果 |
| `keyRef` | 密钥引用，用于由 `SecureKeyResolver` 定位本报文需要的密钥材料 |
| `policyId` | 安全处理策略 ID，用于定位本地安全处理策略 |
| `algorithm` | 算法标识，用于说明或选择本报文使用的加密与签名算法族 |
| `metadata` | 进入 envelope 传输的扩展字段，存在即参与签名 |

字段职责：

```text
version  决定 envelope 协议格式
policyId 决定怎么处理
keyRef   决定用哪组密钥材料
algorithm 决定算法标识
```

`keyRef` 不是密钥规格，不承载密钥材料，也不等同于 Java / JCA 中的 `KeySpec`。它可以带有版本语义，但本质是密钥引用。

`version` 的解析规则必须保持最小稳定性。主版本内，`version` 字段名、基础文本格式和在 canonical text 中的表达方式必须向后兼容。未来如果需要改变字段排序、分隔符或基础编码，应通过兼容解析器、外部协议协商或新 endpoint 处理，不应依赖尚未验签的复杂 envelope 规则来选择 canonicalization 版本。

示例：

```text
keyRef = tenant-a.sde.2026-05
keyRef = payment-prod-sde-v3
keyRef = partner-foo-body-2026q2
```

`policyId` 不是接口资源名。它指向一组本地安全处理配置。

示例：

```text
policyId = body-strict-v1
policyId = partner-default-sde-v1
policyId = payment-body-strict-v1
```

如果策略需要按 URL 绑定，应通过外部映射完成：

```yaml
fist:
  sde:
    web:
      mappings:
        /api/payment/**: payment-body-strict-v1
        /api/internal/**: body-strict-v1
```

## 设计结论

采用「核心协议模型 + 默认实现模块 + Web 服务端集成 + Spring Boot 自动配置 + Feign 技术验证」的方案。

核心结论：

- 只处理 `body` 和 `responseBody` 两个报文域。
- 签名输入直接包含密文 `payload` 的规范化文本，不再定义 `payloadDigest`。
- 签名算法、摘要算法和验签算法完全由 `SignatureHandler` 决定。
- 处理顺序为先验签、再重放校验、后解密。
- Request Body 使用 `RequestBodyAdvice` 解密并重新包装 `HttpInputMessage`。
- Response Body 使用 `ResponseBodyAdvice` 序列化、加密、封装并签名。
- Response Body 不绑定请求 `nonce`，响应只保证自身完整性与来源。
- 签名输入不包含 HTTP method、path、query string 和 header。
- Feign 侧先通过测试验证 `Encoder`、`Decoder`、`Response` 重构、错误上下文保留和自动配置包装顺序。
- `fist-sde-boot-starter` 不依赖 `fist-sde-extra`。
- 新增自动配置只使用 Spring Boot 3 的 `AutoConfiguration.imports`。

## 参考实现观察

本方案参考 `D:\git-repo\mica\mica\mica-api-encrypt` 中的 Spring MVC 集成方式，但不照搬其协议和自动配置机制。

### 可借鉴部分

| mica 实现 | 对 `fist-sde` 的启发 |
| --- | --- |
| `ApiDecryptRequestBodyAdvice` 使用 `RequestBodyAdvice` 读取请求体、解密后返回新的 `HttpInputMessage` | Request Body 解密验签应放在 MVC 反序列化之前，并通过新的 `HttpInputMessage` 交给原有 `HttpMessageConverter` |
| `ApiEncryptResponseBodyAdvice` 使用 `ResponseBodyAdvice` 对控制器返回值做加密 | Response Body 加密签名应放在 MVC 写出响应之前，复用 Spring MVC 返回值处理流程 |
| `DecryptHttpInputMessage` 仅替换 body，保留 headers | 解密后应保留原始请求头，并统一调整明文 `Content-Type` |
| `ApiDecryptParamResolver` 使用 `HandlerMethodArgumentResolver` 处理单个加密参数 | 若未来恢复参数级能力，应优先考虑参数级 resolver，而不是透明改写整个 Query |
| `ISecretKeyResolver` 可由默认实现或租户实现替换 | `SecureKeyResolver` 应保留可插拔设计，并支持多租户、密钥引用和用途区分 |
| 自动配置使用 `@ConditionalOnProperty` 和 `@ConditionalOnMissingBean` | SDE 默认关闭，显式启用；默认处理管线允许用户覆盖 |

### 不采用部分

| mica 做法 | 不采用原因 |
| --- | --- |
| 依赖 `mica-auto` 编译期生成 `spring.factories` | FIST Kit v3 基于 Spring Boot 3，应使用 `AutoConfiguration.imports`，不新增编译期生成机制 |
| URL 参数 `data` 解密 | 本方案明确不做 Query 加密和 Query 签名 |
| `InputStream.available()` 判断请求体是否为空 | 该方法不能可靠判断 HTTP Body 是否存在 |
| 只加密不签名 | `fist-sde` 必须区分篡改、重放、解密失败等错误边界 |
| 在注解中直接选择算法和密钥 | SDE 使用 `policyId` 承载算法、密钥解析、重放校验和 envelope 配置，注解只做策略选择和少量覆盖 |

## 模块结构

```text
fist-sde/
  fist-sde-core
  fist-sde-extra
  fist-sde-web
  fist-sde-boot-starter
```

| 模块 | 职责 | 是否首阶段交付 |
| --- | --- | --- |
| `fist-sde-core` | 协议模型、策略模型、核心接口、异常模型、签名规范化、nonce 与密钥解析契约 | 是 |
| `fist-sde-extra` | AES-GCM、HMAC-SHA256、国密算法适配、默认 nonce 生成器、测试级 replay guard | 是 |
| `fist-sde-web` | Spring MVC Request Body 和 Response Body 集成 | 是 |
| `fist-sde-boot-starter` | Spring Boot 自动配置、属性绑定、条件装配 | 是 |
| `fist-cloud-rpc-feign` | Feign 侧 SDE 扩展，正式实现前必须完成技术验证 | 否 |

模块依赖方向：

```text
fist-sde-extra -> fist-sde-core
fist-sde-web -> fist-sde-core
fist-sde-boot-starter -> fist-sde-core + fist-sde-web
fist-cloud-rpc-feign -> fist-sde-core
```

`fist-sde-boot-starter` 不依赖 `fist-sde-extra`。默认算法实现由应用显式引入并注册，避免测试级或演示级实现被隐式带入生产应用。

## 核心设计原则

### 只处理 Body 域

1.4 版本只支持：

- `body`：请求 Body。
- `responseBody`：响应 Body。

不再支持：

- `query` 报文域。
- `@SecureQuery`。
- Query canonicalization。
- Query `RequestInterceptor`。
- `HttpServletRequestWrapper` 透明替换 Query 参数。

### 不绑定 method 和 path

签名输入不包含 HTTP method、path、query string 和 header。

理由：

- 服务网关、灰度路由、前缀重写和服务发现可能改变 path。
- header 透传、认证和追踪字段经常由网关或客户端中间件补充。
- method 和 path 更适合由接口访问控制、路由和业务鉴权处理，不应与报文完整性签名混在一起。

如业务需要绑定接口身份，应通过 `policyId`、业务字段或上层鉴权机制表达。

### 先验签后解密

签名基于密文 `payload` 的传输态文本计算。接收端处理顺序固定为：

```text
读取 envelope -> 规范化签名输入 -> 验签 -> 校验时间戳和 nonce -> 解密 -> JSON 反序列化
```

该顺序能够在不解密的前提下识别篡改报文，并将签名失败、重放失败和解密失败区分为不同异常。

### 策略优先

策略 ID 承载复杂配置。注解只选择策略或覆盖请求、响应是否启用。

策略包含：

- 报文模式。
- 加密处理器。
- 签名处理器。
- 密钥解析器。
- nonce 生成器。
- 重放校验器。
- envelope 字段配置。
- 时间戳窗口。

## 交换方向

Request Body 入站：

```text
安全报文 -> 规范化签名输入 -> 验签 -> 重放校验 -> 解密 -> 明文 JSON -> MVC 参数绑定
```

Response Body 出站：

```text
控制器返回对象 -> JSON 序列化 -> 加密 -> 构造 responseBody envelope -> 规范化签名输入 -> 签名 -> HTTP 响应
```

Feign 客户端请求方向，进入技术验证阶段：

```text
接口入参 -> JSON 序列化 -> 加密 -> 构造 body envelope -> 规范化签名输入 -> 签名 -> Feign Encoder 写出
```

Feign 客户端响应方向，进入技术验证阶段：

```text
安全响应 -> 规范化签名输入 -> 验签 -> 重放校验 -> 解密 -> 重构 Feign Response -> 原始 Decoder 反序列化
```

## 交换模式

### 输入模式

```java
public enum SecureInputMode {
    INHERIT,
    DISABLED,
    OPTIONAL,
    REQUIRED,
    PLAIN
}
```

| 模式 | 说明 |
| --- | --- |
| `INHERIT` | 从更高层级或默认策略继承 |
| `DISABLED` | 不处理安全报文，也不拒绝明文 |
| `OPTIONAL` | 明文和安全报文都接受 |
| `REQUIRED` | 必须是安全报文 |
| `PLAIN` | 必须是明文，安全报文会被拒绝 |

### 响应模式

```java
public enum SecureResponseMode {
    INHERIT,
    DISABLED,
    ENABLED,
    FOLLOW_REQUEST
}
```

| 模式 | 说明 |
| --- | --- |
| `INHERIT` | 从更高层级或默认策略继承 |
| `DISABLED` | 不封装响应 |
| `ENABLED` | 响应始终封装为安全报文 |
| `FOLLOW_REQUEST` | 请求为安全报文时封装响应 |

`null` 响应默认不封装，即使响应模式为 `FOLLOW_REQUEST`。如确实需要封装 `null`，后续可增加显式配置，例如 `wrap-null-response=true`，但不进入首阶段。

## 安全报文

核心层定义 `SecureEnvelope`，字段名由 envelope 配置决定，处理逻辑不写死字段名。

建议模型：

```text
version       协议版本
scope         报文域：body、responseBody
payload       密文或封装后的传输文本
signature     签名结果
timestamp     请求或响应时间
nonce         随机串或报文唯一标识
keyRef        密钥引用
algorithm     算法标识，可选
policyId      策略 ID，可选
metadata      扩展字段，可选
```

字段规则：

- `version`、`scope`、`payload`、`signature`、`timestamp`、`nonce`、`keyRef` 为推荐基础字段。
- `scope` 只允许 `body` 和 `responseBody`。
- `algorithm`、`policyId`、`metadata` 如果出现在 envelope 中，必须参与签名。
- 如果 `algorithm` 和 `policyId` 完全由服务端策略固定解析，可以不出现在 envelope 中。
- `metadata` 只用于进入报文传输的扩展信息。本地上下文不应写入 `metadata`。
- `payload` 以传输态文本参与签名输入，不再计算 `payloadDigest`。

配置示例：

```yaml
fist:
  sde:
    envelopes:
      default:
        version-field: version
        scope-field: scope
        payload-field: data
        signature-field: sign
        timestamp-field: timestamp
        nonce-field: nonce
        key-ref-field: keyRef
        algorithm-field: algorithm
        policy-id-field: policyId
        metadata-field: metadata
```

## 签名契约

签名输入由 `SignatureCanonicalizer` 统一生成。Body 和 Response Body 分域定义。

### 通用规则

- 使用 UTF-8。
- 按逻辑字段名的 ASCII 字典序排序。
- `signature` 字段本身不参与签名。
- `payload` 直接参与签名输入，不使用 `payloadDigest`。
- 不包含 HTTP method、path、query string 和 header。
- `algorithm`、`policyId`、`metadata` 存在即签。
- 未出现在 envelope 中的可选字段不参与签名。
- 必填字段缺失时抛出 `SecureEnvelopeException`。
- 必填字段为空字符串时视为无效 envelope。
- 可选字段为 `null` 或空字符串时视为不存在，不参与签名输入。
- 文本字段不得包含 CR 或 LF；如未来支持复杂 metadata，应先编码为稳定 JSON 文本。

### Body 签名输入字段

```text
scope=body
version
payload
timestamp
nonce
keyRef
algorithm
policyId
metadata
```

### Response Body 签名输入字段

```text
scope=responseBody
version
payload
timestamp
nonce
keyRef
algorithm
policyId
metadata
```

Response Body 不绑定请求 `nonce`。响应只保证自身完整性与来源。

### Canonical Text 格式

建议 canonical text 使用 LF 分隔，每行格式为：

```text
fieldName=fieldValue
```

字段按逻辑字段名排序，不按 envelope 配置后的传输字段名排序。

示例 envelope：

```json
{
  "version": "1",
  "scope": "body",
  "data": "A1x9Kk8uJ1w0sYpK",
  "timestamp": "2026-05-08T12:00:00Z",
  "nonce": "Q2ZTeVJqM3B0X2R5c2R4Yg",
  "keyRef": "tenant-a.sde.2026-05",
  "algorithm": "AES-GCM+HMAC-SHA256",
  "policyId": "body-strict-v1",
  "sign": ""
}
```

`data` 先映射回逻辑字段 `payload`。`signature` 不参与签名。

Canonical text：

```text
algorithm=AES-GCM+HMAC-SHA256
keyRef=tenant-a.sde.2026-05
nonce=Q2ZTeVJqM3B0X2R5c2R4Yg
payload=A1x9Kk8uJ1w0sYpK
policyId=body-strict-v1
scope=body
timestamp=2026-05-08T12:00:00Z
version=1
```

`SignatureCanonicalizer` 输出上述文本的 UTF-8 字节：

```java
byte[] signingInput = canonicalizer.canonicalize(envelope, context);
byte[] signature = signatureHandler.sign(signingInput, signContext);
```

签名算法、摘要算法和验签算法完全由 `SignatureHandler` 决定。例如 HMAC-SM3、HMAC-SHA256、SM2 签名都可以通过不同 `SignatureHandler` 实现。

## JSON 序列化契约

出站加密前的 JSON 字节由统一序列化组件处理。

建议定义：

```java
public interface SecureJsonCodec {
    byte[] serialize(Object value, Type valueType);
}
```

规则：

- 使用 UTF-8。
- 默认实现基于项目既有 Jackson 配置。
- 日期、枚举、null 值和字段命名遵循接入应用当前 Jackson 配置。
- 签名只覆盖密文 payload，不要求明文 JSON 具备跨语言 canonical JSON 格式。
- Request Body 入站解密后的明文字节继续交给 Spring MVC 原有 `HttpMessageConverter`，不通过 `SecureJsonCodec.deserialize` 反序列化。
- 如果客户端不是 Java，需要在协议规范文档中明确 JSON 生成约定和示例。

Request Body 入站不使用 `SecureJsonCodec.deserialize` 的原因是保留 Spring MVC 原有绑定、校验、自定义 converter 和应用级反序列化行为。

## nonce 与重放校验

core 层定义 `NonceGenerator`：

```java
public interface NonceGenerator {
    String generate(NonceContext context);
}
```

nonce 生成要求：

- 至少 128 bit 随机性。
- 推荐使用 Base64URL 或 hex 编码。
- 同一 `keyRef + policyId + scope` 下不得复用。
- 不允许使用自增 ID、时间戳或低熵随机数直接作为 nonce。

`ReplayGuard` 由 core 定义接口，不绑定具体存储。

```java
public interface ReplayGuard {
    void checkAndMark(ReplayContext context);
}
```

推荐重放键：

```text
keyRef + policyId + scope + nonce
```

如果 `policyId` 不出现在 envelope 中，可使用：

```text
keyRef + scope + nonce
```

服务端 Request Body 默认执行重放校验。Response Body 客户端是否执行重放校验由策略配置决定；Feign 技术验证阶段必须覆盖该行为。

`fist-sde-extra` 可以提供 `InMemoryReplayGuard`，但只适合测试和演示。该实现必须使用并发安全结构，并具备基于时间窗口的过期清理能力。

## 密钥解析与轮换

core 层定义 `SecureKeyResolver`：

```java
public interface SecureKeyResolver {
    SecureKey resolve(SecureKeyContext context);
}
```

`SecureKeyContext` 至少包含：

```text
keyRef
policyId
scope
direction
usage
algorithm
metadata
```

`usage` 用于区分：

```text
SIGN
VERIFY
ENCRYPT
DECRYPT
```

`SecureKeyResolver` 的实现决定 `keyRef + usage` 到密钥材料的映射关系。core 层不假设同一 `keyRef` 下不同 usage 使用同一密钥，也不要求必须分离加密密钥和签名密钥。接入项目可以根据合规、租户、方向和算法要求，在同一 `keyRef` 下返回同一组密钥材料或按 usage 返回不同密钥材料。

密钥轮换规则：

- 发送端使用当前 active `keyRef`。
- 接收端按 `keyRef` 解析密钥材料，不应只依赖默认密钥。
- 轮换过渡期内，接收端同时保留 active 和 previous 密钥引用。
- 过渡期长度不得小于最大时间戳窗口、最大消息延迟和调用方发布窗口之和。
- 过渡期结束后，旧 `keyRef` 应从 resolver 中移除。
- 缺少 `keyRef` 或 `keyRef` 不存在时抛出明确的 `SecureKeyResolveException`。

典型轮换流程：

```text
T0  接收端发布 K2，保留 K1 和 K2
T1  发送端切换 active keyRef 到 K2
T2  观察所有调用方已切换，且超过最大重放窗口
T3  接收端移除 K1
```

## core 层设计

建议包名：

```text
com.power4j.fist.sde.core
com.power4j.fist.sde.core.annotation
com.power4j.fist.sde.core.codec
com.power4j.fist.sde.core.crypto
com.power4j.fist.sde.core.signature
com.power4j.fist.sde.core.replay
com.power4j.fist.sde.core.key
com.power4j.fist.sde.core.nonce
com.power4j.fist.sde.core.exception
```

最终包名前缀需要在实施前结合模块落点确认。如果模块落在 `fist-kit-infra` 下，可评估是否使用 `com.power4j.fist.infra.sde`，但不应在不同模块间混用两个命名空间。

核心接口：

```java
public interface SecureEnvelopeCodec {
    SecureEnvelope decode(byte[] input, SecureEnvelopeContext context);
    byte[] encodeToBytes(SecureEnvelope envelope, SecureEnvelopeContext context);
    Object encodeToBody(SecureEnvelope envelope, SecureEnvelopeContext context);
}
```

职责：

- `decode` 只负责把传输字节解析为 `SecureEnvelope`。
- `encodeToBytes` 只负责把 `SecureEnvelope` 编码为完整传输字节。
- `encodeToBody` 只负责生成适合 `HttpMessageConverter` 写出的对象，例如 `Map` 或 Jackson `ObjectNode`。
- 不做验签。
- 不做重放校验。
- 不做加密或解密。
- 不解析或选择密钥。

`SecureEnvelopeContext` 至少包含：

```text
envelopeName
fieldMapping
charset
mediaType
targetBodyType
selectedConverterType
```

用途：

- `decode` 使用 `fieldMapping` 将传输字段名映射为逻辑字段名。
- `encodeToBytes` 使用 `fieldMapping` 输出完整 envelope 字节。
- `encodeToBody` 根据 `targetBodyType` 和 `selectedConverterType` 生成适配当前 `HttpMessageConverter` 的返回对象。

`SecureExchangeContext` 至少包含：

```text
scope
direction
policyId
algorithm
keyRef
timestampWindow
requestContext
```

用途：

- `SignatureCanonicalizer` 使用它确定报文域、方向、策略和规范化约束。
- `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`ReplayGuard` 可复用其中的方向、策略、算法和请求上下文摘要。

```java
public interface CryptoHandler {
    byte[] encrypt(byte[] plain, CryptoContext context);
    byte[] decrypt(byte[] cipher, CryptoContext context);
}
```

```java
public interface SignatureHandler {
    byte[] sign(byte[] input, SignContext context);
    boolean verify(byte[] input, byte[] signature, SignContext context);
}
```

```java
public interface SignatureCanonicalizer {
    byte[] canonicalize(SecureEnvelope envelope, SecureExchangeContext context);
}
```

```java
public interface SecureKeyResolver {
    SecureKey resolve(SecureKeyContext context);
}
```

```java
public interface NonceGenerator {
    String generate(NonceContext context);
}
```

```java
public interface ReplayGuard {
    void checkAndMark(ReplayContext context);
}
```

```java
public interface SecureExchangeExceptionTranslator {
    RuntimeException translate(SecureExchangeException exception, SecureExchangeContext context);
}
```

`SecureExchangeExceptionTranslator` 建议通过 `ObjectProvider<SecureExchangeExceptionTranslator>` 注入。没有自定义 translator 时，直接抛出原始 `SecureExchangeException` 子类。

## 策略模型

建议模型：

```text
SecurePolicy
  id
  requestBodyMode
  responseBodyMode
  cryptoEnabled
  signatureEnabled
  cryptoHandlerName
  signatureHandlerName
  keyResolverName
  nonceGeneratorName
  replayGuardName
  envelopeName
  timestampWindow
```

Query 相关字段不再出现。

### 策略开关组合

首阶段只支持以下组合：

| `cryptoEnabled` | `signatureEnabled` | 是否允许 | 说明 |
| --- | --- | --- | --- |
| `true` | `true` | 允许 | 加密 + 签名，默认推荐 |
| `false` | `true` | 允许 | 只签名，用于只要求完整性和来源校验的场景 |
| `true` | `false` | 禁止 | 只加密不签名无法区分篡改和解密失败 |
| `false` | `false` | 有条件允许 | 仅当 request / response 模式均为 `DISABLED` 或 `PLAIN` 时允许，否则视为配置错误 |

## 注解设计

策略 ID 承载复杂配置，注解只做接口级选择和少量覆盖。

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureExchange {
    String value() default "";
    SecureInputMode requestBody() default SecureInputMode.INHERIT;
    SecureResponseMode responseBody() default SecureResponseMode.INHERIT;
}
```

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureBody {
    String value() default "";
    SecureInputMode request() default SecureInputMode.INHERIT;
    SecureResponseMode response() default SecureResponseMode.INHERIT;
}
```

不再定义 `@SecureQuery`。

### 注解优先级

优先级从高到低：

1. 方法级 `@SecureBody`。
2. 方法级 `@SecureExchange`。
3. 类级 `@SecureBody`。
4. 类级 `@SecureExchange`。
5. Web 默认策略配置。
6. 全局默认策略配置。

冲突检测范围：

- 同一方法上同时标注 `@SecureBody` 和 `@SecureExchange`，视为方法级冲突。
- 同一类上同时标注 `@SecureBody` 和 `@SecureExchange`，视为类级冲突。
- 方法级专用注解覆盖类级通用注解，不视为冲突。
- 类级专用注解与方法级通用注解按优先级覆盖，不视为冲突。

冲突应在启动期扫描发现；如果无法在启动期发现，运行期必须抛出明确异常，不做隐式合并。

## fist-sde-extra 设计

`fist-sde-extra` 承载可复用默认实现，但不参与自动装配。

建议包含：

- `JacksonSecureJsonCodec`。
- `AesGcmCryptoHandler`。
- `HmacSha256SignatureHandler`。
- `SecureRandomNonceGenerator`。
- `InMemoryReplayGuard`。
- `StaticSecureKeyResolver`，仅用于测试或演示。
- 国密算法适配类。

### JDK 默认实现

首选 JDK 标准能力：

- AES-GCM。
- HMAC-SHA256。
- `SecureRandom`。

### 国密默认实现

国密能力放在 extra 中：

- SM4。
- SM3。
- HMAC-SM3。

依赖策略：

- Bouncy Castle 依赖使用 optional。
- starter 不传递引入国密 Provider。
- 应用需要国密时显式引入并注册 Provider。
- Provider 不存在时抛出 `SecureAlgorithmUnavailableException`。

## Web 服务端设计

### Spring MVC 集成方式

借鉴 `mica-api-encrypt` 的 Spring MVC 扩展点，SDE Web 侧使用：

- `RequestBodyAdvice` 处理入站 Request Body。
- `ResponseBodyAdvice` 处理出站 Response Body。

不使用：

- Servlet Filter 透明读取和替换 Body。
- Query `HttpServletRequestWrapper`。
- 编译期生成自动配置文件。

### Request Body

`SecureRequestBodyAdvice` 处理带 `@RequestBody` 的请求：

1. 基于控制器类、方法和策略配置判断是否处理。
2. 排除 `multipart/form-data`。
3. 读取请求 Body 字节，不使用 `InputStream.available()` 判断是否为空。
4. 按 `SecureEnvelopeCodec` 解析 envelope。
5. 校验 `scope=body`。
6. 通过 `SignatureCanonicalizer` 生成签名输入。
7. 使用 `SignatureHandler` 验签。
8. 使用 `ReplayGuard` 校验时间戳和 nonce。
9. 使用 `CryptoHandler` 解密。
10. 将明文字节重新包装为 `HttpInputMessage`。
11. 交给 Spring MVC 原有 `HttpMessageConverter` 反序列化。

注意事项：

- 明文 JSON 进入原有 `HttpMessageConverter`，不绕过 Spring MVC 的数据绑定和校验流程。
- 解密后的 `HttpInputMessage` 保留原始 headers。
- 包装消息中的 `Content-Type` 应统一设置为 `application/json`。
- 空 Body 在 `REQUIRED` 模式下应报错，在 `OPTIONAL` 和 `DISABLED` 模式下按策略放行。

### Response Body

`SecureResponseBodyAdvice` 处理控制器返回值：

1. 基于控制器类、方法、请求处理结果和策略配置判断是否处理。
2. `null` 响应默认不封装。
3. 使用 `SecureJsonCodec` 将返回对象序列化为 JSON 字节。
4. 使用 `NonceGenerator` 生成响应 nonce。
5. 加密生成密文 payload。
6. 构造 `scope=responseBody` envelope。
7. 通过 `SignatureCanonicalizer` 生成签名输入。
8. 使用 `SignatureHandler` 签名。
9. 按已选中的 `HttpMessageConverter` 类型选择写出形态。

写出策略：

| 已选 converter | `beforeBodyWrite` 返回值 | 说明 |
| --- | --- | --- |
| `MappingJackson2HttpMessageConverter` 或兼容 JSON converter | `SecureEnvelopeCodec.encodeToBody(...)` | 返回 `Map<String, Object>` 或 Jackson `ObjectNode`，字段名由 envelope 配置控制 |
| `StringHttpMessageConverter` | `SecureEnvelopeCodec.encodeToBytes(...)` 转为 UTF-8 `String` | 用于原始返回值为 `String` 的控制器，避免被 Jackson 再次加引号；不得调用 `encodeToBody` |
| `ByteArrayHttpMessageConverter` | `SecureEnvelopeCodec.encodeToBytes(...)` | 用于原始返回值为 `byte[]` 的控制器；不得调用 `encodeToBody` |
| 其他 converter | 默认拒绝或要求显式适配 | 避免不可预期的写出行为 |

响应 envelope 输出 `Content-Type` 统一为 `application/json`。

Response Body 相关测试必须覆盖普通 POJO、`String`、`byte[]`、`null` 和异常响应。

### 异常响应

默认不对异常响应做 SDE 封装。

原因：

- 异常响应通常由接入项目已有 `@ControllerAdvice` 或统一错误模型接管。
- 在异常处理阶段再次加密可能掩盖原始异常。
- 加密失败会造成二次异常，增加排查难度。

如接入项目要求错误响应也封装为安全报文，应通过显式策略启用，并在文档中说明统一异常处理器的顺序。

### Advice 注册方式

`fist-sde-web` 提供 Advice 类，`fist-sde-boot-starter` 通过自动配置显式注册 Bean。

建议：

```java
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "fist.sde.web", name = "enabled", havingValue = "true")
public class SdeWebAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    SecureRequestBodyAdvice secureRequestBodyAdvice(...) {
        return new SecureRequestBodyAdvice(...);
    }

    @Bean
    @ConditionalOnMissingBean
    SecureResponseBodyAdvice secureResponseBodyAdvice(...) {
        return new SecureResponseBodyAdvice(...);
    }
}
```

Advice 类可以标注 `@ControllerAdvice`，但不依赖应用侧组件扫描覆盖 SDE 包名。

## Feign 技术验证设计

Feign 没有可直接借鉴的成功案例，正式实现前必须先完成技术验证。

### 验证目标

验证以下问题：

- Feign `Encoder` 能否在不破坏原始编码器的前提下写出安全 envelope。
- Feign `Decoder` 能否消费安全响应后，构造新的 `feign.Response` 并交回原始 Decoder。
- 新 `Response` 是否保留 HTTP 状态码、reason、headers、request 和 charset。
- 响应验签失败、重放失败和解密失败时，异常能否保留 client 名称、方法标识、HTTP 状态码和处理阶段。
- 多个 `Encoder` / `Decoder` 包装时，SDE 包装顺序是否可控。
- SDE Feign 自动配置能否与现有 `FeignClientAutoConfiguration` 共存。
- 现有 `RelayInterceptor` 顺序是否影响 SDE 请求签名。1.4 默认不做 Query，也不签 header，因此第一阶段不需要 SDE `RequestInterceptor`。

### 验证范围

先在 `fist-cloud-rpc-feign` 或独立测试夹具中完成：

- `SecureFeignEncoderPrototypeTest`。
- `SecureFeignDecoderPrototypeTest`。
- `FeignResponseRebuildTest`。
- `FeignSdeAutoConfigurationPrototypeTest`。

验证通过后，再把 Feign 方案纳入正式实施阶段。

### Decoder 响应重构约定

如果验证通过，正式 `SecureFeignDecoder` 应遵循：

1. 读取原始 `Response.Body`。
2. 识别安全 responseBody envelope。
3. 生成签名输入并验签。
4. 执行重放校验和解密。
5. 使用明文 JSON 字节构造新的 `Response.Body`。
6. 复用原始 `status`、`reason`、`headers` 和 `request`。
7. 将 `Content-Type` 调整为 `application/json`，除非策略指定其他类型。
8. 调用原始 Decoder 完成反序列化。

如该链路无法稳定验证，则 Feign 正式实现应推迟，不进入首阶段交付。

### Feign 阶段边界

1.4 阶段不交付：

- Feign Query 加密。
- Feign Query 签名。
- Feign `RequestInterceptor` 安全 Query 改写。
- 方法级 Feign 策略解析。

1.4 阶段只要求完成技术验证并记录结论。

## 自动配置设计

SDE 默认不启用。

```yaml
fist:
  sde:
    enabled: false
```

自动配置条件：

```java
@ConditionalOnProperty(prefix = "fist.sde", name = "enabled", havingValue = "true")
```

Web 侧：

```text
SdeCoreAutoConfiguration
SdeWebAutoConfiguration
SdeCodecAutoConfiguration
```

Feign 验证侧：

```text
SdeFeignPrototypeConfiguration
```

注册文件只使用 Spring Boot 3 方式：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

不新增 `META-INF/spring.factories`。

现有 `fist-cloud-rpc-feign` 模块仍存在 `spring.factories`。SDE 新增自动配置不得继续扩大该模式。正式进入 Feign 实现前，应优先评估是否同步把该模块现有自动配置迁移到 `AutoConfiguration.imports`，或在方案中明确存量配置与新增配置并存的边界。

自动配置规则：

- 使用 `@AutoConfiguration`。
- 对 Web 侧使用 `@ConditionalOnWebApplication(type = SERVLET)`。
- 对 Feign 验证侧使用 `@ConditionalOnClass` 检查 OpenFeign 类型。
- 对默认处理管线使用 `@ConditionalOnMissingBean`。
- 不注册生产默认 `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`ReplayGuard`。
- 可注册 `SecureJsonCodec` 和策略解析器等无密钥、无算法副作用组件。
- 使用者必须显式注册算法、密钥解析和重放校验相关 Bean。

## 配置设计

建议配置以策略为中心：

```yaml
fist:
  sde:
    enabled: true
    policies:
      body-strict-v1:
        request-body-mode: REQUIRED
        response-body-mode: FOLLOW_REQUEST
        crypto:
          enabled: true
          handler: aesGcmCryptoHandler
          key-resolver: defaultSecureKeyResolver
        signature:
          enabled: true
          handler: hmacSha256SignatureHandler
          key-resolver: defaultSecureKeyResolver
        nonce:
          generator: secureRandomNonceGenerator
        replay:
          enabled: true
          guard: replayGuard
          timestamp-window: 5m
        envelope: default
    envelopes:
      default:
        payload-field: data
        signature-field: sign
        timestamp-field: timestamp
        nonce-field: nonce
        key-ref-field: keyRef
```

Web 侧：

```yaml
fist:
  sde:
    web:
      enabled: true
      default-policy-id: body-strict-v1
      request-envelope: default
      response-envelope: default
      encrypt-error-response: false
      verbose: false
```

Feign 验证侧：

```yaml
fist:
  sde:
    feign:
      prototype:
        enabled: true
        clients:
          order-service:
            policy-id: body-strict-v1
```

`verbose` 只能输出处理阶段、策略 ID、字段状态和错误码，不输出明文、密钥、完整签名输入或完整密文。

## 错误模型

建议异常层次：

```text
SecureExchangeException
  SecurePolicyNotFoundException
  SecureEnvelopeException
  SecureCryptoException
  SecureSignatureException
  SecureReplayException
  SecureKeyResolveException
  SecureMessageBindingException
  SecureAlgorithmUnavailableException
  SecureNonceException
  SecureMediaTypeNotSupportedException
```

异常信息应包含错误类型、策略 ID、方向、报文域和处理阶段，不包含密钥、明文、完整密文和完整签名输入。

Web 侧默认抛出 `SecureExchangeException` 子类。如果存在 `SecureExchangeExceptionTranslator` Bean，则先转换再抛出，由接入项目已有 `@ControllerAdvice` 或统一错误处理接管。

Feign 验证侧默认抛出 `SecureExchangeException` 子类。响应解密或验签失败时，应保留 HTTP 状态码、client 名称、方法标识、报文域和处理阶段。

## 协议规范文档要求

除 Java 模块文档外，必须提供一份面向跨语言调用方的协议规范文档，至少覆盖：

- envelope 字段定义。
- 字段名配置和逻辑字段名映射规则。
- `version`、`scope`、`keyRef`、`policyId`、`algorithm` 的语义。
- `version` 兼容约束。
- payload 编码规则。
- canonical text 生成规则。
- 签名输入示例。
- 签名和验签伪代码。
- Request Body 示例。
- Response Body 示例。
- 错误码和排查建议。

## 使用文档要求

模块文档至少覆盖：

- 最小依赖引入方式。
- 服务端启用方式。
- 策略配置最小示例。
- Request Body 和 Response Body 分别如何启用。
- 明确说明不支持 Query 加密和 Query 签名。
- `fist-sde-extra` 中默认算法列表和适用范围。
- 使用国密实现时如何引入 Bouncy Castle Provider。
- 哪些 Bean 有默认处理管线，哪些 Bean 必须由接入项目提供。
- 接入项目已有全局异常处理时如何配置异常转换器。
- 明文接口、安全接口、灰度接入接口的推荐配置。
- 常见错误排查：策略缺失、密钥缺失、Provider 缺失、签名失败、解密失败、时间戳过期、重放请求、nonce 生成异常。
- Feign 仅处于技术验证阶段，正式使用前需等待验证结论。

接入项目必须自行提供或确认：

| 项目 | 是否必须 | 说明 |
| --- | --- | --- |
| `CryptoHandler` | 必须 | 可使用 `fist-sde-extra` 默认实现，也可自行实现 |
| `SignatureHandler` | 必须 | 可使用 `fist-sde-extra` 默认实现，也可自行实现 |
| `SecureKeyResolver` | 必须 | 负责按策略 ID、方向、密钥引用或租户信息解析密钥材料 |
| `NonceGenerator` | 必须 | 可使用 `fist-sde-extra` 默认实现，也可自行实现 |
| 生产级 `ReplayGuard` | 建议必须 | 单机内存实现只适合测试和演示 |
| `SecureExchangeExceptionTranslator` | 按项目需要 | 用于转换为项目统一异常类型 |
| 算法策略配置 | 必须 | 指定接口使用的加密、签名、封装和重放校验策略 |
| Bouncy Castle Provider | 使用国密时必须 | 由应用显式引入和注册 |

## 分阶段实施建议

### 第一阶段：核心协议、extra 与 Web 服务端

交付内容：

- `fist-sde-core`。
- `fist-sde-extra`。
- `fist-sde-web`。
- `fist-sde-boot-starter`。
- Request Body 解密验签。
- Response Body 加密签名。
- 安全报文封装与拆封。
- Body 与 Response Body 分域签名契约。
- `keyRef`、`policyId`、`version` 和 `scope` 语义。
- nonce 生成、重放校验和密钥轮换契约。
- 策略配置和异常模型。

### 第二阶段：Feign 技术验证

交付内容：

- Feign `Encoder` 原型测试。
- Feign `Decoder` 原型测试。
- `feign.Response` 明文重构测试。
- Feign 自动配置包装顺序测试。
- 与现有 `FeignClientAutoConfiguration` 的共存验证。
- Feign 技术验证报告。

只有第二阶段验证通过后，才进入 Feign 正式实现。

### 第三阶段：Feign 正式实现

交付内容：

- `fist-cloud-rpc-feign` 内的 SDE Feign 扩展。
- Feign `Encoder` / `Decoder`。
- per-client 策略配置。
- 客户端请求加密签名。
- 客户端响应解密验签。
- 与现有 Feign 自动配置的顺序和覆盖规则。

### 第四阶段：增强能力

交付内容：

- 方法级 Feign 策略解析。
- 生产级可插拔重放校验实现。
- 示例工程和迁移文档。
- 兼容性测试矩阵。

## 测试方案

### core 测试

- `SecureEnvelope` 编解码。
- `SecureEnvelopeCodec` 字段名映射。
- `SecureEnvelopeContext` 和 `SecureExchangeContext` 字段边界。
- 策略合并和默认值继承。
- Body 与 Response Body 签名输入规范化。
- 签名输入直接包含 `payload`，不包含 `payloadDigest`。
- `algorithm`、`policyId`、`metadata` 存在即签。
- 可选字段为空字符串时不参与签名。
- 必填字段为空字符串时抛出 `SecureEnvelopeException`。
- 字段按 ASCII 字典序排序。
- nonce 生成约束。
- 密钥解析失败。
- 密钥轮换期间按 `keyRef` 解析。
- `SecureKeyResolver` 可按 `keyRef + usage` 返回相同或不同密钥材料。
- 异常模型映射。

### extra 测试

- AES-GCM 加密、解密正常和失败路径。
- HMAC-SHA256 签名、验签正常和失败路径。
- `SecureRandomNonceGenerator` 生成长度和字符集符合约定。
- SM4、SM3、HMAC-SM3 在 Provider 存在时可用。
- Provider 缺失时抛出明确异常。
- `InMemoryReplayGuard` 重放识别和并发访问。

### Web 测试

- `RequestBodyAdvice` 正常解密验签。
- `RequestBodyAdvice` 空 Body、非法 envelope、缺少字段、错误签名。
- `RequestBodyAdvice` 不使用 `InputStream.available()` 判断 Body。
- `RequestBodyAdvice` 解密后 `Content-Type` 为 `application/json`。
- `ResponseBodyAdvice` 正常加密签名。
- `ResponseBodyAdvice` 支持 POJO、`String` 和 `byte[]` 返回值。
- `ResponseBodyAdvice` 对 `null` 响应默认不封装。
- `REQUIRED` 模式拒绝明文请求。
- `PLAIN` 模式拒绝安全报文。
- `OPTIONAL` 模式兼容明文和安全报文。
- `multipart/form-data` 请求被明确排除。
- 异常响应默认不封装。

### Feign 验证测试

- `Encoder` 原型将请求对象输出为安全报文。
- `Decoder` 原型将安全响应还原为返回类型。
- `Decoder` 重构后的 `Response` 保留原始状态码、headers 和 request。
- 响应解密失败时保留原始状态码和错误上下文。
- per-client 策略互不影响。
- Feign 依赖不存在时不装配 Feign SDE 原型组件。

### 自动配置测试

- `fist.sde.enabled=false` 时不装配处理组件。
- 未配置 `fist.sde.enabled` 时不装配处理组件。
- `fist.sde.enabled=true` 且 `fist.sde.web.enabled=true` 时装配 Web 处理管线。
- 缺少算法 Bean 时给出明确启动失败或配置失败信息。
- 默认 Bean 可被用户自定义 Bean 覆盖。
- Web 环境和非 Web 环境装配行为正确。
- 自动配置只通过 `AutoConfiguration.imports` 注册。

## 验收标准

- 新模块纳入 Maven reactor 或明确标注为后续阶段。
- 自动配置文件符合 Spring Boot 3 约定，只使用 `AutoConfiguration.imports`。
- `fist-sde-boot-starter` 不依赖 `fist-sde-extra`。
- 首阶段不包含 Query 加密、Query 签名和 `@SecureQuery`。
- Feign 正式实现前必须存在技术验证测试和验证结论。
- 所有公共 API 有稳定包名和最小暴露面。
- Body 与 Response Body 分域签名契约明确。
- 签名输入不包含 HTTP method、path、query string 和 header。
- 签名输入直接包含规范化后的密文 `payload`，不包含 `payloadDigest`。
- `keyRef`、`policyId`、`version` 和 `scope` 语义清晰且测试覆盖。
- 本地执行模块级测试通过。
- 格式校验通过。
- 文档给出服务端、默认算法、国密 Provider 和跨语言协议规范的最小接入方式。
- 示例不包含真实密钥、真实令牌或可识别业务数据。

## 风险与决策

| 风险 | 影响 | 决策 |
| --- | --- | --- |
| Query 加密进入默认能力 | URL 日志、缓存、网关和排查成本增加，且不符合敏感数据避免进入 URL 的通用建议 | 1.4 删除 Query 能力 |
| 固定 `payloadDigest` 算法 | 在国密或其他合规场景下引入额外算法约束 | 删除 `payloadDigest`，直接签 `payload` |
| `keyId` 语义过窄 | 难以表达密钥集、用途分离和租户化密钥材料 | 改为 `keyRef` |
| `policy` 语义像资源标签 | 容易混淆资源映射和安全处理策略 | 改为 `policyId` |
| Feign 无成功案例直接实现 | 可能在 Decoder、Response 重构和自动配置顺序上返工 | 先做技术验证，再正式实现 |
| method/path 参与签名 | 网关重写导致客户端和服务端签名输入不一致 | 不纳入签名输入 |
| 签名基于明文 payload | 验签前必须先解密，错误边界不清 | 基于密文 payload 的传输态文本生成签名输入 |
| starter 隐式依赖 extra | 默认算法和测试实现被带入生产应用 | starter 不依赖 extra |
| 国密 Provider 缺失 | 运行期算法不可用 | optional 依赖，显式注册，明确异常 |
| SDE 默认启用 | 改变 HTTP 报文语义，侵入性过高 | 默认关闭，显式启用 |
| 异常响应默认加密 | 可能掩盖原始异常并造成二次失败 | 默认不封装异常响应 |

## 最终建议

1.4 版本作为新的实施依据。后续实现按「先 Body、后 Feign」推进：先完成 `core`、`extra`、`web` 和 `boot-starter`，确保 Request Body 和 Response Body 的协议、Spring MVC 集成、密钥引用、策略 ID、重放校验和签名规范化稳定；随后用独立测试验证 Feign `Encoder` / `Decoder` 的工程可行性，验证通过后再在现有 `fist-cloud-rpc-feign` 中补充正式扩展。
