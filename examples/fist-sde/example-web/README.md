# example-sde-web

本示例演示 Spring MVC 服务端接入 `fist-sde-boot-starter` 后，使用方需要完成的关键任务：

1. 显式引入 `fist-sde-boot-starter` 和 `fist-sde-client`。
2. 如需使用内置演示算法，显式引入 `fist-sde-extra`。
3. 在 `application.yml` 中开启 `fist.sde.enabled` 和 `fist.sde.web.enabled`。
4. 配置 `body-strict-v1` 策略。
5. 显式声明 `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`NonceGenerator` 和 `ReplayGuard` Bean。
6. Controller 仍然只处理普通 POJO，客户端 envelope 编解码使用 `SecureExchangeOperations`。

运行：

```powershell
.\mvnw.cmd -Pexamples -pl examples/fist-sde/example-web -am spring-boot:run
```

控制台重点观察以下日志：

- `Client business request POJO`
- `Client sends request envelope`
- `Controller received decrypted request POJO`
- `Controller returns response POJO before SDE response advice`
- `Client receives response envelope`
- `Client decrypted response POJO`

示例中的 `StaticSecureKeyResolver` 和 `InMemoryReplayGuard` 只用于本地演示。生产应用应接入自己的密钥管理和共享重放校验存储。
