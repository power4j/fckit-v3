# example-sde-server

本示例演示 Spring MVC 服务端接入 `fist-sde-boot-starter` 后，如何使用原始 SDE envelope 请求验证服务端处理流程。该示例适合在开发自研 HTTP 客户端、网关适配器或其他非 SDK 客户端时作为本地测试服务端。

1. 显式引入 `fist-sde-boot-starter` 和 `fist-sde-client`。
2. 如需使用内置演示算法，显式引入 `fist-sde-extra`。
3. 在 `application.yml` 中开启 `fist.sde.enabled` 和 `fist.sde.web.enabled`。
4. 配置 `body-strict-v1` 策略。
5. 显式声明 `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`NonceGenerator` 和 `ReplayGuard` Bean。
6. Controller 仍然只处理普通 POJO，客户端 envelope 编解码使用 `SecureExchangeOperations`。

运行：

```powershell
.\mvnw.cmd -Pexamples -pl examples/fist-sde/example-server -am spring-boot:run
```

应用启动后会自动构造一次 SDE request envelope 并请求本地 `/orders`，不需要人工触发请求。

控制台重点观察以下日志：

- `Client business request POJO`
- `Client sends request envelope`
- `Controller received decrypted request POJO`
- `Controller returns response POJO before SDE response advice`
- `Client receives response envelope`
- `Client decrypted response POJO`

示例中的 `StaticSecureKeyResolver` 和 `InMemoryReplayGuard` 只用于本地演示。生产应用应接入自己的密钥管理和共享重放校验存储。
