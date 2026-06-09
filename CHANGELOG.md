# Changelog

本文件记录 FIST Kit v3 的用户可见变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，并遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

## [3.14.0] - 2026-06-09

### Added

- 新增 `fist-jasypt` 国密配置加密模块组，包含 `fist-jasypt-core`、`fist-jasypt-spring-boot-starter` 和不发布 Maven 的 `fist-jasypt-cli`。
- 新增 `GMENC(...)` 配置密文 envelope，使用 SM3 派生、SM4-CBC/PKCS7 加密和 HMAC-SM3 完整性校验。
- 新增 Spring Boot starter，支持通过 `fist.jasypt` 启用自定义 `jasyptStringEncryptor`，并将默认密文边界映射到 Jasypt 属性。
- 新增 CLI 命令，用于生成随机密钥、加密、解密和查看主密钥指纹。
- 新增 CLI `help`、`--help`、`-h` 帮助入口。
- 新增 `example-jasypt-basic` 示例，演示主密钥文件和 `GMENC(...)` 配置读取。

### Fixed

- 修正 `fist.jasypt.cipher-prefix` 和 `fist.jasypt.cipher-suffix` 仅影响 Jasypt 属性、不影响实际加密器输出的问题。当前 core、Spring Boot starter 和 CLI 均支持相同的可配置密文边界。

## [3.13.1] - 2026-05-09

### Changed

- 将 `tile-crypto` 升级到 `1.8.2`，并调整 `fist-support-security` 的 Bouncy Castle Provider 传递依赖排除项。

## [3.13.0] - 2026-05-09

### Added

- 新增 `fist-sde` 首阶段模块，提供安全 envelope 编解码、签名输入规范化、基础加密签名扩展、Spring MVC Request Body 解密验签、Response Body 加密签名，以及 Spring Boot 3 自动配置入口。
- 新增 `fist-sde` secure envelope 跨语言协议规范文档，说明字段语义、payload 编码、签名输入、请求/响应示例和排查建议。
- 新增 `fist-sde` 首阶段 artifact 的 BOM 版本管理条目。
- 新增 `fist-sde-extra` 国密 SM4-GCM 加解密和 HMAC-SM3 签名实现。
- 新增 `fist-sde` 模块接入说明，覆盖服务端启用、默认算法、国密 Provider、必需 Bean、异常转换和首阶段边界。
- 新增 `fist-sde-extra` SM3 摘要适配实现。

### Changed

- 将 Bouncy Castle Provider 依赖从 `bcprov-jdk15to18` 切换为 `bcprov-jdk18on`，并排除 `tile-crypto` 传递带入的旧 Provider artifact。

### Fixed

- 修正 `fist-sde-web` 入站请求未校验 envelope 协议格式版本的问题，当前实现只接受 `version` 为 `1` 的请求。
- 修正 `fist-sde-extra` 测试级内存重放校验器，使其优先使用当前交换上下文的时间窗口。

### Removed

- 移除 `fist-sde-extra` 中不可用的 SM4 占位实现，保留可用的 `Sm4GcmCryptoHandler`。
