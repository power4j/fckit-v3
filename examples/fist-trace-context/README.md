# FIST Trace Context Examples

本目录提供 `fist-trace-context` 的两个最小示例。

| 示例 | 重点 |
| --- | --- |
| `example-trace-context-basic` | 默认 `X-REQ-UID`、`requestId`、`systemCode`、`spanId` 能力 |
| `example-trace-context-extension` | 自定义 `TraceContextItemFactory` 和自定义 item 配置 |

示例仅用于演示接入方式和扩展路径，不包含生产日志规范、网关路由或跨服务完整调用链。
