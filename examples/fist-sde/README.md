# FIST SDE Examples

本目录提供 SDE 服务端和三类客户端的最小集成示例。

| 示例 | 重点 |
| --- | --- |
| `example-web` | Spring MVC `RequestBodyAdvice` / `ResponseBodyAdvice` 与 `SecureExchangeOperations` |
| `example-feign` | `SecureFeignEncoder` / `SecureFeignDecoder` |
| `example-restclient` | `SecureRestClientInterceptor` 与 Boot `RestClientCustomizer` |
| `example-webclient` | `SecureWebClientExchangeFilterFunction` 与 Boot `WebClientCustomizer` |

示例默认打开 `fist.sde.client.log-payload=true`，控制台会打印 raw body、request envelope、response envelope 和 decrypted response。示例中的静态密钥和内存 replay guard 只用于本地演示，生产应用需要替换为业务自己的密钥解析和重放校验实现。
