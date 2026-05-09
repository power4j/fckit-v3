# fist-sde Feign 技术验证报告

> 日期：2026-05-08
> 执行者：Codex
> 依据：`docs/local/plans/20260508-sde/fist-sde-technical-solution-1.4.md`

## 结论

当前 1.4 技术方案可以实现 Feign 注解式 Body / Response Body 加密处理。

验证结果表明，Spring Cloud OpenFeign 在解析接口方法后，会将 Java 方法元数据保留在 `feign.MethodMetadata` 中，并可通过 `RequestTemplate.methodMetadata()` 在 `Encoder` 侧读取。正式实现可以基于 `@SecureExchange` 或 `@SecureBody` 的类级、方法级注解，选择 `policyId`、请求 Body 模式和响应 Body 模式。

本次验证仍属于技术验证，不交付正式 Feign 生产代码。

## 已验证能力

| 验证项 | 结论 | 对应测试 |
| --- | --- | --- |
| Feign 注解元数据传递 | `SpringMvcContract` 解析后，`@SecureExchange` 可从 `MethodMetadata.method()` 读取 | `FeignAnnotationMetadataPrototypeTest` |
| 请求加密封包 | 原始 `Encoder` 先完成 JSON 编码，SDE 包装层可将 Body 替换为 secure envelope | `SecureFeignEncoderPrototypeTest` |
| 响应解密验签 | SDE 包装层可读取 secure envelope，验签解密后交回原始 `Decoder` | `SecureFeignDecoderPrototypeTest` |
| Response 重建 | 重建后的 `feign.Response` 保留 status、reason、headers 和 request | `FeignResponseRebuildTest` |
| 重放校验 | 重复响应 nonce 可被拒绝 | `SecureFeignDecoderPrototypeTest` |
| 自动配置共存 | 原型自动配置默认关闭，启用后可与现有 `FeignClientAutoConfiguration` 共存 | `FeignSdeAutoConfigurationPrototypeTest` |

## 原型边界

原型代码仅位于测试目录：

```text
fist-kit-cloud/fist-cloud-rpc-feign/src/test/java/com/power4j/fist/cloud/rpc/feign/sde/prototype
```

生产代码未新增 Feign SDE 实现，`fist-cloud-rpc-feign` 仅增加 test scope 的 `fist-sde-core` 和 `fist-sde-extra` 依赖，用于原型验证。

本次验证不包含：

- Query 加密。
- Query 签名。
- Header 签名。
- 正式 `SecureFeignEncoder` / `SecureFeignDecoder`。
- 正式 Feign 自动配置。
- per-client 完整策略绑定。

## 正式实现建议

后续正式实现可以采用以下结构：

1. 在 `fist-cloud-rpc-feign` 中新增生产级 Feign SDE 扩展。
2. 使用 Feign `Encoder` 包装原始 `Encoder`，先委托原始编码器生成请求 Body，再按 `policyId` 加密、签名并替换为 secure envelope。
3. 使用 Feign `Decoder` 包装原始 `Decoder`，先验签、重放校验和解密，再重建 `feign.Response` 并委托原始 `Decoder`。
4. 通过 `MethodMetadata.method()` 读取 `@SecureExchange` 或 `@SecureBody`，并合并类级注解、方法级注解和配置文件策略。
5. 自动配置继续使用 Spring Boot 3 的 `AutoConfiguration.imports`，不新增 `spring.factories`。
6. 默认关闭 Feign SDE，应用显式启用后才装配。

## 需要继续验证的风险

正式实现前还需要补充以下验证：

- 多个 Feign `Encoder` / `Decoder` 包装器同时存在时的顺序规则。
- Spring Cloud OpenFeign per-client 子上下文中的 Bean 覆盖关系。
- `@FeignClient(configuration = ...)` 下用户自定义编码器与 SDE 包装器的组合行为。
- 错误上下文：client 名称、方法标识、HTTP 状态码、处理阶段和原始异常的保留方式。
- 非 JSON Body、空 Body、`void` 返回值、`ResponseEntity` 返回值和流式响应的处理边界。

## 验证命令

```powershell
.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=*PrototypeTest,FeignResponseRebuildTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
.\mvnw.cmd -U -pl fist-kit-cloud/fist-cloud-rpc-feign -am test
```

两条命令均已通过，`fist-cloud-rpc-feign` 中 Feign SDE 原型测试共 7 个，0 failures，0 errors，0 skipped。
