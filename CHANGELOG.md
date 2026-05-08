# Changelog

本文件记录 FIST Kit v3 的用户可见变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，并遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- 新增 `fist-sde` 首阶段模块，提供安全 envelope 编解码、签名输入规范化、基础加密签名扩展、Spring MVC Request Body 解密验签、Response Body 加密签名，以及 Spring Boot 3 自动配置入口。
- 新增 `fist-sde` secure envelope 跨语言协议规范文档，说明字段语义、payload 编码、签名输入、请求/响应示例和排查建议。
- 新增 `fist-sde` 首阶段 artifact 的 BOM 版本管理条目。
- 新增 `fist-sde-extra` 国密 SM4-GCM 加解密和 HMAC-SM3 签名实现。
- 新增 `fist-sde` 模块接入说明，覆盖服务端启用、默认算法、国密 Provider、必需 Bean、异常转换和首阶段边界。

### Fixed

- 修正 `fist-sde-extra` 测试级内存重放校验器，使其优先使用当前交换上下文的时间窗口。
