# fist-sde-boot-starter

`fist-sde-boot-starter` 提供 SDE 服务端和客户端的 Spring Boot 3 自动配置。服务端基于 Spring MVC `RequestBodyAdvice` / `ResponseBodyAdvice`，客户端覆盖 Feign、RestClient 和 WebClient。

starter 只注册编排类和适配类，不隐式注册生产算法、静态密钥或测试级 replay guard。算法、密钥解析和重放校验需要由应用显式提供。

## 依赖

服务端、RestClient 和 WebClient 基础依赖：

```xml
<dependency>
  <groupId>com.power4j.fist3</groupId>
  <artifactId>fist-sde-boot-starter</artifactId>
</dependency>
```

如使用 SDK 提供的标准版或国密版算法预设，再显式引入：

```xml
<dependency>
  <groupId>com.power4j.fist3</groupId>
  <artifactId>fist-sde-extra</artifactId>
</dependency>
```

Feign 客户端还需要引入 OpenFeign：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

WebClient 客户端还需要引入 WebFlux：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## 服务端

最小开发内容：

1. 在 Controller 方法或类型上标注 `@SecureExchange("body-strict-v1")`。
2. 提供 `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`NonceGenerator` 和 `ReplayGuard` Bean。
3. Controller 继续使用普通 `@RequestBody` POJO，不需要手动解密。

最小配置：

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
        crypto-handler: aesGcmCryptoHandler
        signature-handler: hmacSha256SignatureHandler
        key-resolver: secureKeyResolver
        nonce-generator: secureRandomNonceGenerator
        replay-guard: replayGuard
```

## Feign 客户端

最小开发内容：

1. 在 Feign 接口方法或类型上标注 `@SecureExchange("body-strict-v1")`。
2. 提供 Feign `Encoder` / `Decoder`，用 `SecureFeignEncoder` / `SecureFeignDecoder` 包装原有 JSON 编解码器。
3. 提供服务端同一套策略、算法、密钥解析和 replay guard Bean。

示例：

```java
@Bean
Encoder secureFeignEncoder(ObjectFactory<HttpMessageConverters> converters,
		SecureExchangeOperations operations) {
	return new SecureFeignEncoder(new SpringEncoder(converters), operations);
}

@Bean
Decoder secureFeignDecoder(ObjectFactory<HttpMessageConverters> converters,
		SecureExchangeOperations operations) {
	return new SecureFeignDecoder(new SpringDecoder(converters), operations);
}
```

配置：

```yaml
fist:
  sde:
    enabled: true
    client:
      enabled: true
      default-policy-id: body-strict-v1
      default-key-ref: tenant-a
    policies:
      body-strict-v1:
        request-body-mode: REQUIRED
        response-body-mode: FOLLOW_REQUEST
```

## RestClient 客户端

最小开发内容：

1. 注入 Spring Boot 提供的 `RestClient.Builder`。
2. 使用该 builder 创建 `RestClient`，starter 会通过 `RestClientCustomizer` 添加 SDE 拦截器。
3. 业务代码继续传入普通 POJO。

配置同 Feign 客户端的 `fist.sde.client` 和 `fist.sde.policies`。

## WebClient 客户端

最小开发内容：

1. 注入 Spring Boot 提供的 `WebClient.Builder`。
2. 使用该 builder 创建 `WebClient`，starter 会通过 `WebClientCustomizer` 添加 SDE filter。
3. 业务代码继续传入普通 POJO。

配置同 Feign 客户端的 `fist.sde.client` 和 `fist.sde.policies`。

## 日志

默认不打印明文 body。演示或排查时可打开：

```yaml
fist:
  sde:
    client:
      log-payload: true
```

生产环境不建议打印明文请求或响应。
