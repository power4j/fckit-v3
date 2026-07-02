# example-trace-context-extension

自定义 item 示例，注册 `tenant-id` processor，并通过配置把 `X-TENANT-ID` 写入 `TraceContext` 和 MDC。

```bash
curl -H "X-REQ-UID: REQ-1001" -H "X-TENANT-ID: tenant-a" http://localhost:8080/trace
```

新增一种追踪上下文项的最短路径：

1. 实现 `TraceContextItemFactory`。
2. 继承 `AbstractPropDrivenTraceContextItem` 或 `AbstractSingleValueTraceContextItem`。
3. 将 factory 注册为 Spring Bean。
4. 在 `fist.trace-context.items` 中配置 item ID、`processor` 和该 processor 需要的 `props`。
