## fist-sde 安全数据交换

`fist-sde` 提供基于安全 envelope 的 Request Body 解密验签和 Response Body 加密签名能力。首阶段只覆盖 Spring MVC Body 处理，不覆盖 Query 加密、Query 签名和正式 Feign 集成。

模块组成：

| 模块 | 作用 |
| --- | --- |
| `fist-sde-core` | 协议模型、策略模型、签名规范化、密钥、nonce、重放校验和异常契约 |
| `fist-sde-extra` | AES-GCM、SM4-GCM、HMAC-SHA256、SM3、HMAC-SM3、随机 nonce、测试级内存重放校验和静态密钥解析 |
| `fist-sde-web` | Spring MVC `RequestBodyAdvice` 和 `ResponseBodyAdvice` 集成 |
| `fist-sde-boot-starter` | Spring Boot 3 自动配置入口 |

### 依赖引入

服务端接入 Spring MVC 管线时引入 starter：

```xml
<dependency>
  <groupId>com.power4j.fist3</groupId>
  <artifactId>fist-sde-boot-starter</artifactId>
</dependency>
```

starter 不依赖 `fist-sde-extra`。如使用内置算法和测试级实现，需要显式引入：

```xml
<dependency>
  <groupId>com.power4j.fist3</groupId>
  <artifactId>fist-sde-extra</artifactId>
</dependency>
```

### 最小启用配置

SDE 默认关闭。启用 Web Body 处理需要显式配置：

```yaml
fist:
  sde:
    enabled: true
    web:
      enabled: true
      default-policy-id: body-strict-v1
    policies:
      body-strict-v1:
        request-body-mode: REQUIRED
        response-body-mode: FOLLOW_REQUEST
        crypto-enabled: true
        signature-enabled: true
        crypto-handler: aesGcmCryptoHandler
        signature-handler: hmacSha256SignatureHandler
        key-resolver: secureKeyResolver
        nonce-generator: secureRandomNonceGenerator
        replay-guard: replayGuard
        timestamp-window: 5m
```

`REQUIRED` 表示请求必须是安全 envelope；`OPTIONAL` 同时接受明文和安全 envelope；`PLAIN` 拒绝安全 envelope。响应侧可使用 `DISABLED`、`ENABLED` 或 `FOLLOW_REQUEST`。

### 必须提供的 Bean

自动配置只注册无密钥副作用的基础组件，不注册生产默认算法、密钥或重放校验 Bean。接入应用需要显式提供：

| Bean 类型 | 用途 |
| --- | --- |
| `CryptoHandler` | 加密和解密 payload |
| `SignatureHandler` | 生成和校验签名 |
| `SecureKeyResolver` | 按 `keyRef`、方向和 usage 解析密钥材料 |
| `NonceGenerator` | 生成 envelope nonce |
| `ReplayGuard` | 校验时间戳窗口和 nonce 重放 |

`fist-sde-extra` 中的 `StaticSecureKeyResolver` 和 `InMemoryReplayGuard` 只适合测试、演示或单机验证。生产应用应使用项目自己的密钥管理和共享重放校验存储。

### 默认算法和国密 Provider

`fist-sde-extra` 提供以下实现：

| 实现 | Bean 建议名 | 说明 |
| --- | --- | --- |
| `AesGcmCryptoHandler` | `aesGcmCryptoHandler` | 基于 JDK AES-GCM |
| `HmacSha256SignatureHandler` | `hmacSha256SignatureHandler` | 基于 JDK HMAC-SHA256 |
| `Sm4GcmCryptoHandler` | `sm4GcmCryptoHandler` | 需要显式注册支持 SM4-GCM 的 JCA Provider |
| `Sm3Digest` | - | 需要显式注册支持 SM3 的 JCA Provider，返回原始摘要字节 |
| `HmacSm3SignatureHandler` | `hmacSm3SignatureHandler` | 需要显式注册支持 HMAC-SM3 的 JCA Provider |
| `SecureRandomNonceGenerator` | `secureRandomNonceGenerator` | 生成 Base64URL nonce |
| `InMemoryReplayGuard` | `replayGuard` | 测试级内存重放校验 |

国密实现依赖 Bouncy Castle 时，由应用显式引入并注册 Provider。Provider 不存在时，国密实现会抛出 `SecureAlgorithmUnavailableException`。

```java
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

Security.addProvider(new BouncyCastleProvider());
```

### 注解策略

控制器类或方法可以使用 `@SecureBody` 或 `@SecureExchange` 选择策略，并覆盖请求、响应模式。方法级注解优先于类级注解；同一层级同时声明两个注解会抛出 `SecureEnvelopeException`。

```java
@SecureExchange("body-strict-v1")
class PaymentController {

    @SecureBody(value = "plain-v1", request = SecureInputMode.PLAIN, response = SecureResponseMode.DISABLED)
    Object callback(@RequestBody Map<String, Object> body) {
        return body;
    }

}
```

### 异常转换

Web 侧默认抛出 `SecureExchangeException` 子类。应用如已有统一异常模型，可注册 `SecureExchangeExceptionTranslator`，在 SDE Advice 抛出前转换异常类型。

### 明确边界

- `multipart/form-data` 不进入 SDE Body 处理。
- `null` 响应默认不封装。
- 异常响应默认不封装。
- 签名输入不包含 HTTP method、path、query string 和 header。
- 签名输入直接包含规范化后的密文 `payload`，不使用 `payloadDigest`。
- 使用 `keyRef` 表示密钥引用，使用 `policyId` 表示安全处理策略。
- `version` 只表示 envelope 协议格式版本。
- 当前实现只接受 `version` 为 `1` 的请求 envelope。
- Feign 目前仅完成技术验证测试和验证报告，正式 Feign 生产代码不属于首阶段交付。

### 常见问题

| 现象 | 检查项 |
| --- | --- |
| 策略不存在 | `fist.sde.web.default-policy-id` 和 `fist.sde.policies` 是否一致 |
| 密钥解析失败 | envelope `keyRef` 是否存在，`SecureKeyResolver` 是否按 usage 返回正确密钥 |
| 签名失败 | `payload`、`policyId`、`algorithm`、`metadata` 是否在发送端和接收端保持一致 |
| 解密失败 | 加密算法、密钥材料和 payload 编码是否匹配 |
| 时间戳过期 | `timestamp-window` 是否覆盖调用延迟和时钟偏移 |
| 重放请求 | 同一 `keyRef + policyId + scope + nonce` 是否重复提交 |
| Provider 缺失 | 使用 SM4-GCM、SM3 或 HMAC-SM3 时是否注册 JCA Provider |

跨语言 envelope 字段、canonical text 和请求响应示例见 [SDE 协议规范](../../docs/public/develop/sde-protocol.md)。
