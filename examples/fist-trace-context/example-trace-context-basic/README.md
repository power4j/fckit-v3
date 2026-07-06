# example-trace-context-basic

默认能力示例，启用后可通过 `GET /trace` 查看当前 `TraceContext`。

```bash
curl -H "X-REQ-UID: REQ-1001" http://localhost:8080/trace
```

未传 `X-REQ-UID` 时，默认 `correlation-id` item 会生成请求 ID。
