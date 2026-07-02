# fist-trace-context-spring-boot-starter

`fist-trace-context-spring-boot-starter` 提供追踪上下文的 Spring Boot 接入能力。默认能力覆盖 Servlet 入口请求采集、MDC 写入、RestClient 透传、异步任务恢复和 `@TraceSpan` 方法级 span。

首阶段暂不提供普通 WebFlux 应用的 starter 级入站 `WebFilter`。Reactive 场景当前由 `fist-cloud-gateway` 的 `RequestIdGlobalFilter` 写入统一 snapshot。

## 依赖

```xml
<dependency>
  <groupId>com.power4j.fist3</groupId>
  <artifactId>fist-trace-context-spring-boot-starter</artifactId>
</dependency>
```

## 最小配置

```yaml
spring:
  application:
    name: order-service

fist:
  trace-context:
    enabled: true
```

未配置 `items` 时，默认启用：

| item ID | processor | 作用 |
| --- | --- | --- |
| `correlation` | `correlation-id` | 从 `X-REQ-UID` 读取链路关联 ID，缺失时生成，写入 `TraceContext`、MDC `requestId` 和出口 header `X-REQ-UID` |
| `systemCode` | `system-code` | 从 `SystemCodeProvider` 或 `spring.application.name` 读取系统代码，写入 `TraceContext` 和 MDC `systemCode` |

`spanId` 是内建栈能力，不是 item。默认写入 MDC `spanId`，不写入 HTTP 出口 header。

## 自定义配置

```yaml
fist:
  trace-context:
    enabled: true
    items:
      request:
        enabled: true
        processor: correlation-id
        order: 0
        props:
          context-name: requestId
          inbound-header: X-REQ-UID
          outbound-header: X-REQ-UID
          mdc-name: requestId
      system:
        processor: system-code
        order: 10
        props:
          context-name: systemCode
          mdc-name: systemCode
```

配置中的 key 是 item ID，`processor` 用于选择对应的 `TraceContextItemFactory`。`props` 由 processor 自行解释。

## 日志 pattern

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId:-},%X{systemCode:-},%X{spanId:-}] %logger{36} - %msg%n
```

## span 注解

```java
@TraceSpanGroup("order")
@Service
class OrderService {

    @TraceSpan("submit")
    OrderResult submit(OrderCommand command) {
        return doSubmit(command);
    }

}
```

类上存在 `@TraceSpanGroup("order")` 时，`@TraceSpan("submit")` 生成 `order.submit`。`@TraceSpan(".remote.query")` 视为绝对 span 名，生成 `remote.query`，不拼接类级 group。

限制：

- 仅对 Spring Bean 生效。
- 自调用不生效。
- `final` 类或 `final` 方法可能不生效。
- 依赖 Spring AOP。
- `@Async` 方法上的 span 边界以实际代理执行为准。

## 自定义 item

实现并注册 `TraceContextItemFactory` Bean：

```java
@Bean
TraceContextItemFactory tenantItemFactory() {
    return new TraceContextItemFactory() {
        @Override
        public String processor() {
            return "tenant-id";
        }

        @Override
        public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
            return new AbstractPropDrivenTraceContextItem(spec) {
            };
        }
    };
}
```

```yaml
fist:
  trace-context:
    enabled: true
    items:
      tenant:
        processor: tenant-id
        props:
          context-name: tenantId
          inbound-header: X-TENANT-ID
          outbound-header: X-TENANT-ID
          mdc-name: tenantId
```

完整示例见 `examples/fist-trace-context`。
