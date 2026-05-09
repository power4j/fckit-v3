# example-sde-feign

本示例演示 Feign 注解式 Body 加密处理的正式 SDK 集成方式。

示例要点：

1. Feign 接口方法使用 `@SecureExchange("body-strict-v1")` 标注策略。
2. `SecureFeignEncoder` 读取 Feign 方法元数据，将原始 JSON Body 加密签名为 SDE request envelope。
3. 服务端仍通过 `fist-sde-boot-starter` 的 Spring MVC `RequestBodyAdvice` 解密并交给 Controller POJO。
4. 服务端响应由 `ResponseBodyAdvice` 封装成 SDE response envelope。
5. `SecureFeignDecoder` 验签、重放校验并解密响应，再交给调用方业务对象。

运行：

```powershell
.\mvnw.cmd -Pexamples -pl examples/fist-sde/example-feign -am spring-boot:run
```

控制台重点观察以下日志：

- `Feign caller business request POJO`
- `Feign encoder raw request body`
- `Feign encoder request envelope`
- `Server Controller received decrypted Feign request POJO`
- `Server Controller returns response POJO before SDE response advice`
- `Feign decoder response envelope`
- `Feign decoder decrypted response body`
- `Feign caller business response POJO`

边界说明：

- Feign Encoder / Decoder 来自 `fist-sde-client`，示例只保留 Spring Cloud OpenFeign 的 delegate 配置。
- 示例只覆盖 Request Body 和 Response Body，不覆盖 Query 加密或 Query 签名。
- 示例中的 `StaticSecureKeyResolver` 和 `InMemoryReplayGuard` 只用于本地演示。
