# fist-trace-context 首阶段落地方案

## 目标

首阶段只在 `fist-kit3` 项目内新增和调整追踪上下文能力，形成可复用核心库、Spring Boot starter、简洁使用说明和两个用户侧示例。当前落地目录为 `fist-kit3-jasypt`，它是 `fist-kit3` 项目的 git worktree。

该阶段不改造 `pac-cloud`、`pac-pms`、`pac-ssx10a` 等应用项目。允许在 `fist-kit3` 项目内对既有 Web、Feign、日志相关模块做迁移式调整，目标是消除重复实现，让原有模块最终使用 `fist-trace-context` 的核心代码。

目标版本暂定为 `3.15.0-SNAPSHOT` 后续版本线。

## 背景

现有 `fist-logback` 聚焦日志消息脱敏，已经提供 `%mask` 和 SPI 扩展机制。追踪上下文能力不并入 `fist-logback`，避免把「日志内容处理」和「上下文采集、传播、MDC 同步」混在一起。

现有 `fist-sde` 已有 Feign、RestClient、WebClient 自动配置形态，可作为 starter 接入方式参考，但职责是安全报文交换，不承担追踪上下文。

评审确认仓库内已有一套请求 ID 相关能力：

- `HeaderMdcFilter`：从 `X-REQ-UID` 读取请求 ID，写入 MDC `requestId`。
- `RelayInterceptor` / `HeaderRelayHandler`：Feign 调用时透传 `X-REQ-UID`。
- `MdcContextLifter`：reactive 场景中恢复 MDC `requestId`。
- `TraceInfo` / `TraceInfoResolver`：接口日志和异常事件使用的请求追踪信息。

因此首阶段不能另起一套默认 `X-Trace-Id` / `traceId` 体系。默认链路追踪 ID 必须对齐现有 `X-REQ-UID` / `requestId`，并明确既有组件如何迁移到 `fist-trace-context` 核心代码。

## 模块划分

首阶段新增两个模块。

### fist-trace-context

纯 Java 核心库。

职责：

- 提供只读 `TraceContext`。
- 提供 `TraceContextHolder` / `TraceContexts` 工具。
- 提供 `TraceScope`、`TraceContextSnapshot`、`pushSpan`、`withSpan`。
- 提供追踪上下文项模型、注册、排序和生命周期编排。
- 提供载体抽象、MDC 适配接口、抽象基类和工具方法。

核心库不依赖 Spring Web。MDC 适配先提供轻量接口和默认 SLF4J 实现。若后续需要无 SLF4J 场景，再拆出独立 adapter 模块。

### fist-trace-context-spring-boot-starter

Spring Boot starter。

职责：

- 依赖 `fist-trace-context`。
- 绑定 `fist.trace-context` 配置。
- 注册默认追踪上下文项工厂。
- 提供 Servlet 入口 Filter。
- 提供 RestClient 透传能力。
- 提供 `TaskDecorator` 异步上下文传递。
- 提供 `@TraceSpan` 注解支持。
- 提供 MDC 同步。

Feign 透传治理在 `fist-cloud-rpc-feign` 内完成。该模块保留现有 `RelayInterceptor`，用新的 `TraceRelayHandler` 取代 `HeaderRelayHandler`，不能在 starter 内另起一套 Feign 拦截器。

## 重复能力治理

新模块落地不是在既有体系旁边再增加一套 trace 能力，而是把现有重复实现迁移到同一套核心代码上。目标态必须满足：

- `fist-trace-context` 是追踪上下文的唯一核心实现。
- Servlet、Feign、reactive MDC、异常事件等既有能力只能复用 `fist-trace-context`，不能保留各自独立的请求 ID / MDC / header 处理逻辑。
- `fist-trace-context-spring-boot-starter` 负责接入、配置绑定和默认能力装配；其他模块不应依赖 starter 做业务逻辑二次开发。
- 原有模块可以依赖 `fist-trace-context` 核心库，或通过可选依赖和条件装配在运行期接入核心能力。
- 使用方如需要原 `HeaderMdcFilter` 能力，迁移后应引入 `fist-trace-context-spring-boot-starter` 并配置对应 item。

首阶段治理边界：

- 只修改 `fist-kit3` 项目代码，当前落地目录为 `fist-kit3-jasypt`。
- 不直接修改 `pac-cloud`、`pac-pms`、`pac-ssx10a` 等应用项目。
- 允许移除旧默认装配，或改造 `fist-kit3` 内已经被新核心能力替代的旧实现。
- 允许引入行为级破坏性更新，但 public 类型删除应采用 deprecated 过渡，避免首阶段产生不可控的二进制兼容破坏。
- 首阶段完成后，`fist-kit3` 内不能同时存在两套默认请求 ID / MDC / header 透传实现。

首阶段治理动作：

| 治理对象 | 所在模块 | 首阶段处理 | 验收要求 |
| --- | --- | --- | --- |
| `HeaderMdcFilter` | `fist-web/fist-support-web`、`fist-web/fist-boot-web-app` | 删除旧 filter 自动配置。旧 public 类型先保留并标记 deprecated，不再作为默认能力入口。入口请求上下文由 `fist-trace-context-spring-boot-starter` 的 Servlet filter 提供。 | 代码中不再默认注册旧 `HeaderMdcFilter`；引入 starter 后，默认仍支持 `X-REQ-UID` / `requestId`。 |
| `TraceInfoResolver<HttpServletRequest>` | `fist-web/fist-support-web`、`fist-web/fist-boot-web-app` | 删除旧默认 bean。public 接口先保留并标记 deprecated。异常事件需要追踪信息时，直接从 `TraceContext` 构造 `TraceInfo`。 | `AbstractExceptionHandler` 等默认消费方不再依赖 `TraceInfoResolver`；引入 starter 后异常事件可读取 `requestId`。 |
| `HeaderRelayHandler` / `TraceRelayHandler` | `fist-cloud-rpc-feign` | 保留现有 `RelayInterceptor` 和 `RelayHandler` 链。删除内部使用的 `HeaderRelayHandler`，新增 `TraceRelayHandler`。存在 `TraceContextRuntime` bean 时注册 `TraceRelayHandler`；不存在时不注册 trace handler，原用户认证 handler 继续工作。 | Feign 模块内只有一个 `RequestInterceptor` bean；启用 starter 后同一次 Feign 请求同时携带 trace header 和认证 header。 |
| `RequestIdGlobalFilter` | `fist-cloud-gateway/fist-gateway-auth-core` | 改造 reactive 写入侧。它不再向 Reactor Context 写入裸 requestId 字符串，而是写入 `TraceContextSnapshot` 或等价上下文载荷。 | `RequestIdGlobalFilter` 写入协议与 `MdcContextLifter` 读取协议一致。 |
| `MdcContextLifter` | `fist-web/fist-support-web` | 改造 reactive 读取侧。它从 Reactor Context 读取 `TraceContextSnapshot` 或等价上下文载荷，并调用 `TraceContextRuntime` 恢复 MDC。 | reactive 场景恢复 MDC 时复用统一运行时模型，不保留独立 `requestId` 字符串协议。 |
| `GlobalErrorAttributes` | `fist-web/fist-support-web` | reactive 异常事件不再直接读取 `X-REQ-UID` header，改为从 `TraceContext` 或 Reactor Context 中的 trace-context 载荷构造 requestId。 | reactive 异常事件与 Servlet 异常事件使用同一追踪上下文来源。 |
| header / MDC 固定映射 | 多处 | 固定映射迁移为默认 item 配置和默认 processor。特殊项目通过 item `props` 或自定义 processor 兼容。 | 同一请求不同时出现两套默认追踪 ID；新增追踪上下文项无需改多个模块。 |

依赖关系调整：

- `fist-trace-context` 不依赖 Spring、Servlet、Feign、Logback。
- `fist-trace-context-spring-boot-starter` 依赖 `fist-trace-context`，并接入 Servlet、MDC、RestClient、TaskDecorator、span 注解和默认 processor。
- `fist-web/fist-support-web` 可以依赖 `fist-trace-context` 核心库，用于异常事件、reactive MDC 等能力复用核心上下文模型。
- `fist-web/fist-boot-web-app` 不依赖新 starter。旧 `HeaderMdcFilter` 自动配置删除后，Web 模块不再默认提供入口 MDC filter。
- `fist-cloud-rpc-feign` 可以增加对 `fist-trace-context` 的可选依赖或普通依赖，用于实现基于 `TraceContext` 的 `TraceRelayHandler`。
- `fist-cloud-rpc-feign` 不应依赖 `fist-trace-context-spring-boot-starter`。是否注册 `TraceRelayHandler`，应通过 `TraceContextRuntime` bean 是否存在判断。
- `fist-cloud-rpc-feign` 的自动配置必须在 trace-context starter 自动配置之后评估，避免启用 starter 后 Feign 仍误走旧实现。
- `fist-cloud-gateway` / `fist-gateway-auth-core` 可以增加对 `fist-trace-context` 的可选依赖或普通依赖，用于实现 reactive 入口适配和 Reactor Context 载荷写入。

配置所有权：

- `fist.trace-context` 配置只能由 `fist-trace-context-spring-boot-starter` 绑定、解析和校验。
- `fist-trace-context` 核心库提供配置对象、item spec、item registry 和编排器，但不直接读取 Spring 配置。
- Web、Feign、reactive MDC、异常事件等模块不能各自读取 `fist.trace-context.items`，只能消费 starter 创建出的运行时组件。
- 其他模块如需判断新能力是否启用，应检测 `TraceContextRuntime`、`TraceContextRegistry` 或等价 bean，而不是重新解析配置。
- 其他模块如需参与 HTTP header、MDC 或上下文传播，应调用统一编排器，不能复制 item `props` 解释逻辑。
- `fist.trace-context.enabled=false` 时，starter 不装配 `TraceContextRuntime` 和 `TraceContextRegistry`。其他模块检测不到 runtime 时，必须回退到旧实现或不提供该能力。

### HeaderMdcFilter

现有 `HeaderMdcFilter` 由 `FistWebAutoConfiguration` 默认注册，bean 名为 `headerMdcFilter`，默认启用。

首阶段采用「移除旧默认装配，public 类型 deprecated 过渡」策略，具体实现如下：

- 从自动配置中彻底移除旧 `HeaderMdcFilter` bean。
- `HeaderMdcFilter` public 类首阶段保留并标记 `@Deprecated`，不再推荐使用，避免下游直接引用该类型时出现编译级破坏。
- 删除 `FistWebAutoConfiguration#headerMdcFilter()`。
- 新入口 filter 只由 `fist-trace-context-spring-boot-starter` 提供。
- 新 filter 使用 item 编排能力完成入口采集、缺失生成、`TraceContextHolder` 设置、MDC 写入和 `finally` 清理。
- 默认 `correlation-id` 仍使用入口 header `X-REQ-UID` 和 MDC key `requestId`。
- 未引入 starter 的应用不再自动获得旧入口 MDC 能力，这是明确的破坏性更新。

迁移说明应写入原 Web 模块的 README 或 changelog，而不是写在 `fist-trace-context-spring-boot-starter` README 中。说明内容包括：原 `HeaderMdcFilter` 默认能力已迁移到 `fist-trace-context-spring-boot-starter`；需要请求 ID 和 MDC 的应用必须引入 starter，并启用 `fist.trace-context.enabled=true`。

### RelayInterceptor

现有 `RelayInterceptor` 默认透传 `X-REQ-UID`，但它从当前 `HttpServletRequest` 读取 header，在异步、定时任务、消息消费等没有当前请求的场景不可用。

首阶段采用「保留拦截器，替换 trace handler」策略，具体实现如下：

- `fist-cloud-rpc-feign` 保留一个对外的 Feign `RequestInterceptor` 注册点，避免多个拦截器重复写入 header。
- `RelayInterceptor` 本身不需要根据 `TraceContextRuntime` 动态替换，仍按顺序执行 `RelayHandler` 列表。
- 删除 `HeaderRelayHandler`。该类只在 `fist-cloud-rpc-feign` 自动配置中内部使用，首阶段不保留 deprecated 过渡。
- 新增 `TraceRelayHandler`，实现 `RelayHandler`，内部调用 `TraceContextRuntime.outbound` 写入 trace header。
- 当运行时存在 `TraceContextRuntime` bean 时，Feign 自动配置把 `TraceRelayHandler` 加入 `RelayInterceptor` 的 handler 列表。
- 当运行时不存在 `TraceContextRuntime` bean，或 `fist.trace-context.enabled=false` 导致 runtime 未装配时，不注册 `TraceRelayHandler`。
- 原 `HeaderRelayHandler` 的 `X-REQ-UID` 透传能力由默认 `correlation-id` item 配置补齐。
- `UserRelayHandler` 这类认证信息透传能力继续保留，不纳入 `fist-trace-context`。
- 自动配置必须保证同一应用内只存在一个 `RelayInterceptor` / `RequestInterceptor` bean。
- handler 执行顺序固定为：`TraceRelayHandler` 先执行，认证类 handler 后执行，避免认证 handler 覆盖 trace header。
- `FeignClientAutoConfiguration` 必须在 trace-context starter 自动配置之后评估。实现上优先使用 `@AutoConfigureAfter(TraceContextAutoConfiguration.class)`，或采用等价方式确保 `TraceContextRuntime` bean 定义已可见。

实现上不建议让 `fist-cloud-rpc-feign` 直接依赖 starter。starter 是应用接入层，不是下层模块 API。Feign 模块应依赖 `fist-trace-context` 核心库，或通过条件类名判断避免强依赖；是否注册 `TraceRelayHandler`，依据 `TraceContextRuntime` bean 是否存在。

### MdcContextLifter

现有 `MdcContextLifter` 只处理 reactive MDC 传播。

首阶段需要基于新核心代码重新开发 reactive MDC 恢复能力，并同时治理 Reactor Context 的写入侧和读取侧协议。具体实现如下：

- 新增统一的 Reactor Context key，用于保存 `TraceContextSnapshot` 或等价上下文载荷，不再使用只保存 requestId 字符串的旧协议。
- `RequestIdGlobalFilter` 作为写入侧，负责在 reactive 入口构造或获取当前 `TraceContext`，并向 Reactor Context 写入统一载荷。
- `RequestIdGlobalFilter` 仍保留现有向下游 request header 写回 `X-REQ-UID` 的行为。写回值来自 `correlation-id` 的入口采集或缺失生成结果。
- 「不支持完整 WebFlux / Gateway 入口追踪上下文能力」不包含现有 `X-REQ-UID` 转发透传；该能力首阶段必须保留。
- `fist-support-web` 提供基于 `ServerHttpRequest` 的 `InboundTraceContext` 适配实现。核心库不依赖 Spring Web，不提供该适配。
- reactive 入口调用链为：`RequestIdGlobalFilter` 获取 `TraceContextRuntime` bean；创建基于 `ServerHttpRequest` 的 `InboundTraceContext`；调用 `runtime.inbound(...)`；立即调用 `runtime.capture()` 得到 snapshot；通过 `contextWrite` 写入 Reactor Context 统一载荷。
- 无 `TraceContextRuntime` bean 时，`RequestIdGlobalFilter` 回退到现有 requestId 字符串协议和 header 写回行为，保证 gateway 不因未启用 starter 失败。
- `MdcContextLifter` 不再固定处理 MDC `requestId`。
- `MdcContextLifter` 作为读取侧，从 Reactor Context 读取统一载荷。
- reactive 执行片段恢复 MDC 时，复用核心库的 MDC 同步规则。
- reactive 场景不能直接依赖普通 ThreadLocal 作为上下文来源。
- reactive 业务代码首阶段不提供 `TraceContextHolder` ThreadLocal 语义，只提供 Reactor Context 载荷读取和 MDC 恢复。
- `GlobalErrorAttributes` 构造错误响应时，优先从 `TraceContext` 或 Reactor Context 中的统一载荷读取 requestId，不再直接固定读取 `X-REQ-UID` header。
- 首阶段只声明支持现有 `X-REQ-UID` 转发透传、reactive MDC 恢复和 reactive 异常事件 requestId 来源统一；WebFlux / Gateway 的响应写回、采样、动态路由等高级策略不纳入首阶段。

### TraceInfo / TraceInfoResolver

现有 `TraceInfoResolver` 从 `X-REQ-UID` 解析 `TraceInfo.requestId`，接口日志和异常事件会消费该信息。

首阶段采用「移除默认 resolver，消费 TraceContext」策略，具体实现如下：

- 删除 `FistWebAutoConfiguration#requestTraceInfoResolver()`。
- `TraceInfoResolver` public 接口首阶段保留并标记 `@Deprecated`，不再作为默认扩展点使用。
- 保留 `TraceInfo` 作为事件载荷模型，避免扩大事件模型破坏范围。
- `AbstractExceptionHandler` 等消费方不再注入 resolver，而是直接读取当前 `TraceContext`。
- 若当前不存在 `TraceContext`，生成不带 requestId 的 `TraceInfo`，不再回退读取请求 header。
- 如后续需要把更多上下文项写入事件模型，应由 `TraceContext` 到 `TraceInfo` 的适配方法完成，不恢复 resolver 扩展点。

这一步完成后，接口日志、异常事件和入口 MDC 使用同一个请求 ID 来源。后续如果新增业务流水号、租户号等追踪上下文项，不需要改多个 resolver，只需要由对应 item 决定是否写入 `TraceContext`，再由事件适配方法选择是否映射到事件载荷。

## 核心概念

### TraceContext

`TraceContext` 表示当前线程或当前执行片段的只读追踪上下文视图。

能力：

- 按名称读取值。
- 导出只读 Map。
- 读取当前 span ID。
- 创建 `TraceContextSnapshot`。
- 用于 MDC 同步、HTTP 透传、异步任务恢复。

### 追踪上下文项

正式概念采用「追踪上下文项」，英文命名为 `TraceContextItem`。

追踪上下文项是参与 `TraceContext` 生命周期的扩展单元，不是普通字段。它可以自行决定：

- 是否从入口载体读取。
- 是否生成默认值。
- 是否覆盖已有值。
- 是否写入出口载体。
- 是否写入 MDC。
- 如何解释和校验私有配置。

框架不理解具体追踪上下文项的业务含义，只负责发现、创建、排序和调用生命周期方法。

### item ID 与 processor

配置中的 key 是 item ID，用来标识某个配置实例。`processor` 是处理器标识，用来找到对应 `TraceContextItemFactory` 并创建该 item。

同一个 `processor` 可以存在多个 item 实例。

```yaml
fist:
  trace-context:
    items:
      request:
        processor: correlation-id
        props:
          inbound-header: X-REQ-UID
          outbound-header: X-REQ-UID
      bankTrace:
        processor: correlation-id
        props:
          context-name: bankTraceId
          inbound-header: global_trace_id
          outbound-header: global_trace_id
          mdc-name: bankTraceId
```

## 配置模型

### 配置所有权与运行时模型

追踪上下文配置由 `fist-trace-context-spring-boot-starter` 统一读取。其他模块迁移到 `TraceContext` 时，只能消费已解析的运行时模型，不能各自实现配置读取。

原因：

- item 的 `props` 由 processor 自行解释。如果多个模块重复读取配置，就会出现同一个 item 在入口、出口、MDC、reactive 场景下解释不一致。
- header 名、MDC 名、上下文名称、是否透传、是否生成默认值等配置需要共同作用，才能让 Servlet、RestClient、Feign、日志和异常事件协同。
- 配置校验必须在启动阶段 fail-fast，不能等到某个模块首次使用时才发现配置错误。

配置处理分层：

| 层次 | 职责 | 所在模块 |
| --- | --- | --- |
| 配置绑定 | 读取 `fist.trace-context`，绑定为配置属性对象。 | `fist-trace-context-spring-boot-starter` |
| 配置解析 | 把配置属性转换为 `TraceContextItemSpec`，查找 factory，创建 item，完成排序和校验。 | starter 调用 `fist-trace-context` 核心库 |
| 运行时注册表 | 保存已启用、已排序、已校验的 item 和 span / MDC 运行时配置。 | `fist-trace-context` |
| 模块消费 | Servlet、RestClient、Feign、reactive MDC、异常事件只调用运行时注册表和编排器。 | 各接入模块 |

核心运行时组件建议命名：

- `TraceContextRuntime`：统一运行时入口，暴露 item 编排、span、MDC 同步、snapshot 等能力。
- `TraceContextRegistry`：保存已创建并排序的 `TraceContextItem`。
- `TraceContextProperties`：starter 内部配置属性对象，不作为其他模块的消费 API。

其他模块的接入约束：

- `fist-cloud-rpc-feign` 不读取 `fist.trace-context.items`。启用新透传实现时，从 `TraceContextRuntime` 获取 outbound 编排能力。
- `MdcContextLifter` 不读取 MDC 名配置。恢复 MDC 时调用 `TraceContextRuntime` 的 MDC 同步能力。
- 异常事件不读取 header 或 MDC 配置。构造 `TraceInfo` 时只读取当前 `TraceContext`。
- 如果 `TraceContextRuntime` 不存在，其他模块可以回退到旧实现或不提供该能力，但不能自己构造半套运行时。

框架只识别最小通用配置：

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
          inbound-header: X-REQ-UID
          outbound-header: X-REQ-UID
          mdc-name: requestId
          accept-upstream: true
          generate-if-missing: true
      system:
        enabled: true
        processor: system-code
        order: 100
        props:
          mdc-name: systemCode
          value: pac-pms
```

通用配置：

- `enabled`：是否启用该 item。默认 `true`。
- `processor`：item 处理器标识。必填。
- `order`：高级配置，用于处理依赖、覆盖、兼容迁移、签名等顺序敏感场景。默认 `0`。
- `props`：item 私有配置。框架不解释，但 item 必须校验自己需要的配置。

同 `order` 时按 item ID 字典序排序，保证启动结果稳定。首阶段不引入 `dependsOn`，如后续出现复杂依赖，再扩展依赖表达。

配置元数据只覆盖通用配置项。`props` 是 processor 私有配置，首阶段不承诺 IDE 对 `props` 内部 key 的自动提示；错误配置由对应 factory 在启动阶段校验并 fail-fast。

多实例冲突规则：

- 同一个 `processor` 可以创建多个 item 实例。
- 多个可写 item 使用相同 `context-name` 时，启动失败。
- 多个写入 MDC 的 item 使用相同 `mdc-name` 时，启动失败。
- 多个写入出口载体的 item 使用相同 `outbound-header` 时，启动失败，除非后续显式引入覆盖策略。
- 默认值可能导致冲突。多实例配置必须显式声明不同的 `context-name`，需要写入 MDC 时也必须显式声明不同的 `mdc-name`。

## 默认能力

首阶段默认提供两个追踪上下文项 processor，以及一个内建 span 栈能力。

### correlation-id

跨调用关联 ID。

该 processor 用于在入口请求、日志、下游调用和异常事件之间保持同一个关联 ID。它不是完整 tracing 模型中的 trace ID，也不表达 span 层级关系；span 层级由内建 span 栈能力处理。

默认行为：

- 默认入口 header：`X-REQ-UID`。
- 默认出口 header：`X-REQ-UID`。
- 默认 MDC 名：`requestId`。
- 从入口 HTTP header 读取。
- 缺失时生成。
- 写入当前 `TraceContext`。
- 写入 MDC。
- RestClient 下游调用时透传。

默认 `props`：

| 配置 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `context-name` | string | `requestId` | 写入 `TraceContext` 的名称。 |
| `inbound-header` | string | `X-REQ-UID` | 入口读取的 HTTP header。 |
| `outbound-header` | string | `X-REQ-UID` | 出口写入的 HTTP header。 |
| `mdc-name` | string | `requestId` | 写入 MDC 的 key。 |
| `accept-upstream` | boolean | `true` | 是否接收上游传入值。 |
| `generate-if-missing` | boolean | `true` | 缺失时是否生成。 |

### system-code

当前业务系统代码。

默认行为：

- 不接收上游覆盖。
- 写入当前 `TraceContext`。
- 写入 MDC。
- 可选向下游透传。

取值顺序：

1. `props.value`。
2. `SystemCodeProvider`。
3. `spring.application.name`。

如果 item 已启用但三者均无值，启动失败。

默认 `props`：

| 配置 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `context-name` | string | `systemCode` | 写入 `TraceContext` 的名称。 |
| `mdc-name` | string | `systemCode` | 写入 MDC 的 key。 |
| `value` | string | 无 | 当前系统代码。 |
| `outbound-header` | string | 无 | 配置后向下游透传。 |

### span

span 不是 `TraceContextItem`。它是 `TraceContext` 的内建栈能力。

原因：

- item 模型是线性生命周期。
- span 模型是嵌套作用域。
- `pushSpan` / `TraceScope.close()` 会频繁在业务代码中改变当前 span。

默认行为：

- 请求入口创建根 span。
- `pushSpan` 创建子 span，并刷新当前 `TraceContext` 和 MDC。
- `TraceScope.close()` 恢复上一个 span，并刷新 MDC。
- 当前 span ID 以只读视图形式暴露在 `TraceContext` 中。
- 默认 MDC 名为 `spanId`。
- 默认不向 HTTP 出口写入 span ID header，避免把本地方法级 span 语义误传为跨系统 tracing 协议。
- 如项目需要向下游传递 span ID，应后续通过独立 processor 或显式配置扩展，不在首阶段默认启用。

## span API

核心库提供作用域 API：

```java
try (TraceScope ignored = TraceContexts.pushSpan("remote.pms.query")) {
    remoteClient.query();
}
```

提供函数式 API：

```java
TraceContexts.withSpan("db.selectAccount", () -> mapper.selectById(id));
```

不暴露裸 `pop` 作为主路径。`pushSpan` 返回 `TraceScope`，由 `close()` 自动恢复上下文。

starter 提供注解：

```java
@TraceSpan("bank.submit")
public SubmitResult submit(SubmitCommand command) {
    return service.submit(command);
}
```

类级别前缀：

```java
@TraceSpanGroup("bank")
public class BankSubmitService {

    @TraceSpan("submit")
    public SubmitResult submit(SubmitCommand command) {
        return doSubmit(command);
    }

}
```

拼接规则：

- 类级别 `@TraceSpanGroup("bank")` 与方法级别 `@TraceSpan("submit")` 拼接为 `bank.submit`。
- 方法级别值以 `.` 开头时，不拼接类级别前缀。
- 首阶段不支持 SpEL。

注解限制必须写入 README：

- 仅对 Spring Bean 生效。
- 自调用不生效。
- `final` 类或 `final` 方法可能不生效。
- 依赖 Spring AOP。
- `@Async` 方法上的 span 边界以实际代理执行为准。

## 异步上下文传递

首阶段支持基础异步传递。

核心 API：

```java
TraceContextSnapshot snapshot = TraceContexts.capture();

try (TraceScope ignored = TraceContexts.restore(snapshot)) {
    runnable.run();
}
```

starter 提供 `TaskDecorator`：

- 捕获提交任务时的 `TraceContextSnapshot`。
- 在线程池执行任务前恢复上下文。
- 任务结束后用 `finally` 清理和恢复原上下文。

首阶段不处理消息队列和跨进程异步任务。Reactor 只覆盖 `MdcContextLifter` 所需的 MDC 恢复适配，不提供完整 reactive 业务上下文传播能力。

## 核心接口和基类

### 生命周期上下文

入口上下文：

```java
public interface InboundTraceContext {

    Optional<String> readHeader(String name);

    Optional<String> getValue(String name);

    void putValue(String name, String value);

}
```

出口上下文：

```java
public interface OutboundTraceContext {

    Optional<String> getValue(String name);

    void writeHeader(String name, String value);

    boolean hasHeader(String name);

}
```

MDC 上下文：

```java
public interface TraceMdcContext {

    Optional<String> getValue(String name);

    void putMdc(String name, String value);

}
```

item 按 `order` 升序执行，后序 item 可以读取前序 item 写入的值。相同 `order` 按 item ID 字典序执行。

### TraceContextItem

```java
public interface TraceContextItem {

    String processor();

    default void onInbound(InboundTraceContext context) {
    }

    default void onOutbound(OutboundTraceContext context) {
    }

    default void onMdc(TraceMdcContext context) {
    }

}
```

`TraceContextItem` 不再提供独立 `validate` 方法。配置校验由 factory 在创建 item 时完成。

### TraceContextItemSpec

```java
public interface TraceContextItemSpec {

    String id();

    String processor();

    int order();

    Map<String, String> props();

    Optional<String> optionalProp(String name);

}
```

### TraceContextItemFactory

```java
public interface TraceContextItemFactory {

    String processor();

    TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context);

}
```

`TraceContextItemFactoryContext` 提供共享组件：

- ID 生成器。
- 时钟。
- `SystemCodeProvider`。
- 类型转换工具。
- 日志器或诊断回调。
- `TraceContextBeanLocator`。

factory 必须校验自己的 `props`。配置错误抛出 `TraceContextConfigurationException`，启动 fail-fast。

`TraceContextConfigurationException` 定义在核心库中，是 unchecked exception。

### TraceContextBeanLocator

核心库不依赖 Spring，但需要给 factory 提供可选的外部对象查找能力。核心库定义自己的查找抽象：

```java
public interface TraceContextBeanLocator {

    <T> Optional<T> find(Class<T> type);

    <T> List<T> findAll(Class<T> type);

}
```

starter 提供基于 Spring `ApplicationContext` 的实现，并注入 `TraceContextItemFactoryContext`。核心库接口不暴露任何 Spring 类型。

### SystemCodeProvider

```java
public interface SystemCodeProvider {

    Optional<String> getSystemCode();

}
```

`SystemCodeProvider` 定义在核心库中。starter 通过 `TraceContextBeanLocator` 查找应用提供的实现，并交给 `system-code` factory 使用。

契约：

- 实现必须线程安全。
- 返回 `Optional.empty()` 表示未提供系统代码，factory 继续回退到 `spring.application.name`。
- 返回空字符串视为配置错误，启动 fail-fast。

### TraceContextRegistry

```java
public interface TraceContextRegistry {

    List<TraceContextItem> items();

}
```

`TraceContextRegistry` 保存 starter 已解析、已校验、已排序的 item。其他模块不得重新读取配置创建 item。

### TraceContextRuntime

```java
public interface TraceContextRuntime {

    TraceContextRegistry registry();

    TraceContext inbound(InboundTraceContext context);

    void outbound(OutboundTraceContext context);

    void syncMdc(TraceMdcContext context);

    TraceContextSnapshot capture();

    TraceScope restore(TraceContextSnapshot snapshot);

}
```

`TraceContextRuntime` 是其他模块消费追踪上下文能力的统一入口。

- Servlet Filter 调用 `inbound` 和 `syncMdc`。
- RestClient / Feign 调用 `outbound`。
- `MdcContextLifter` 调用 `restore` 和 `syncMdc`。
- 异常事件只读取当前 `TraceContext`，不直接读取配置。
- 当前线程没有 `TraceContext` 时，`outbound` 整体跳过，不写任何 header。
- `syncMdc` 的语义是：按顺序执行 item 的 `onMdc`，然后写入内建 span 当前栈顶 MDC。

### 注册优先级

核心库支持 Java SPI 注册 factory。

Spring Boot starter 支持 Spring Bean factory。

优先级规则：

1. Spring Bean factory 优先于 Java SPI factory。
2. 同一来源出现重复 factory `processor`，启动失败。
3. 跨来源出现相同 `processor` 时，Spring Bean factory 覆盖 Java SPI factory，并记录 info 日志。
4. item 配置引用不存在的 `processor`，启动失败。

### 抽象基类

必须提供基类以降低扩展成本：

- `AbstractTraceContextItem`
  - 提供空生命周期方法和配置读取工具。
- `AbstractSingleValueTraceContextItem`
  - 适合单值上下文项。
  - 封装入口读取、上下文写入、出口写入、MDC 写入模板。
- `AbstractPropDrivenTraceContextItem`
  - 继承 `AbstractSingleValueTraceContextItem`。
  - 适合配置驱动的常见上下文项。
  - 可读取 `context-name`、`inbound-header`、`outbound-header`、`mdc-name` 等私有配置。

## 编排流程

启动阶段：

1. starter 读取 `fist.trace-context.enabled`。
2. `enabled=false` 时不创建 `TraceContextRegistry` 和 `TraceContextRuntime`。
3. `enabled=true` 时读取 `fist.trace-context.items` 配置。
4. 过滤 `enabled=false` 的 item。
5. 根据 `processor` 查找 `TraceContextItemFactory`。
6. 调用 factory 创建 item，并在创建过程中校验 `props`。
7. 检测 `context-name`、`mdc-name` 等运行时冲突。
8. 配置错误、factory `processor` 注册冲突、缺失 factory、运行时名称冲突均启动失败。
9. 按 `order` 和 item ID 排序。
10. 创建 `TraceContextRegistry`。
11. 创建 `TraceContextRuntime`，作为其他模块唯一运行时入口。

入口阶段：

1. Servlet Filter 创建 `InboundTraceContext`。
2. 调用 `TraceContextRuntime.inbound`，由 runtime 按顺序执行 item 的 `onInbound`。
3. runtime 创建根 span。
4. `InboundTraceContext` 作为可变构建器收集入口值。
5. inbound 编排结束后，runtime 冻结构建器数据，创建不可变 `TraceContext`。
6. `TraceContextHolder`、`OutboundTraceContext.getValue`、`TraceMdcContext.getValue` 都读取同一份冻结数据。
7. 调用 `TraceContextRuntime.syncMdc` 同步 MDC。
8. 执行业务 FilterChain。
9. `finally` 清理本次请求写入的 MDC 和 `TraceContextHolder`。

出口阶段：

1. 获取当前 `TraceContext`。
2. 当前无 `TraceContext` 时，`TraceContextRuntime.outbound` 整体跳过，不写任何 header。
3. 当前有 `TraceContext` 时，创建 `OutboundTraceContext`。
4. `OutboundTraceContext.getValue` 读取当前不可变 `TraceContext`。
5. 调用 `TraceContextRuntime.outbound`，由 runtime 按顺序执行 item 的 `onOutbound`。
6. span ID 首阶段默认不写入 HTTP 出口 header。

其他模块只能调用 `TraceContextRuntime`，不能绕过 runtime 直接读取配置或重新编排 item。框架只管理生命周期，不管理 item 业务语义。

## MDC 清理契约

所有写入 MDC 的路径必须有对应的清理。

- Servlet Filter 使用 `try-finally` 清理请求入口写入的 MDC。
- `TraceScope.close()` 恢复进入 scope 前的上下文和 MDC。
- `TaskDecorator` 在线程池任务结束后恢复原上下文并清理 MDC。
- `MdcContextLifter` 在每次 reactive 信号回调内执行 restore 和 MDC 同步，并在同一回调内 close 对应 `TraceScope`，不得跨信号持有 scope。
- 异常退出必须走同样清理路径。

## 破坏性评估

首阶段属于迁移式治理，允许出现破坏性更新。破坏性来自删除旧重复实现，而不是默认值变化。

行为变化：

- `fist-boot-web-app` 不再默认注册旧 `HeaderMdcFilter`。
- `HeaderMdcFilter` public 类型首阶段保留并标记 deprecated，不再作为默认能力入口。
- `TraceInfoResolver` public 接口首阶段保留并标记 deprecated，不再作为默认扩展点。
- 需要入口请求 ID 和 MDC 写入的应用，必须引入 `fist-trace-context-spring-boot-starter` 并启用配置。
- `fist.trace-context.enabled=false` 时不创建 `TraceContextRuntime` 和 `TraceContextRegistry`。
- 默认 header / MDC 名仍为 `X-REQ-UID` / `requestId`。
- 缺失 `X-REQ-UID` 时，新 Servlet filter 会生成链路追踪 ID。
- MDC 新增或更新 `requestId`、`systemCode`、`spanId`。
- RestClient 下游请求会通过 `TraceContext` 透传 `X-REQ-UID`。
- Feign 透传在 `fist-cloud-rpc-feign` 内通过 `RelayHandler` 链治理。启用追踪上下文时注册 `TraceRelayHandler`，由默认 `correlation-id` item 补齐原 `X-REQ-UID` 透传能力；认证 header 仍由 `UserRelayHandler` 处理。未启用追踪上下文时不注册 `TraceRelayHandler`。
- 未引入或未启用 starter 的应用，Feign 下游调用不再透传 `X-REQ-UID`。需要跨服务关联 ID 的应用必须引入并启用 starter。
- 默认异常事件中的 `TraceInfo` 从 `TraceContext` 构造，不再依赖默认 `TraceInfoResolver`。
- reactive MDC 传播和 reactive 异常事件 requestId 来源改为基于 `TraceContext` 核心模型。
- `@TraceSpan` 会改变当前 span 和 MDC `spanId`。

迁移要求：

- 原依赖 `HeaderMdcFilter` 的应用需要引入 starter。
- 原自定义 `TraceInfoResolver` 的应用可短期继续编译，但默认异常处理不再消费该扩展点，需要迁移为读取 `TraceContext` 或自定义事件适配逻辑。
- 原依赖 Feign `X-REQ-UID` 透传的应用不需要同时注册新拦截器，但需要引入并启用 starter，确保默认 `correlation-id` item 提供等价 header 透传。
- 迁移说明应写入被迁移能力所在模块的 README 或 changelog。starter README 只说明新模块的使用方式和默认能力。

## 验收标准

### 核心库

- 新增 `fist-trace-context` 模块。
- 提供只读 `TraceContext`。
- 提供 `TraceContexts` / `TraceContextHolder` 工具。
- 提供 `TraceContextSnapshot`。
- 提供 `TraceScope`。
- 提供 `pushSpan`、`withSpan`、`capture`、`restore` 语义。
- 提供 `TraceContextItem`、`TraceContextItemSpec`、`TraceContextItemFactory`、`TraceContextItemFactoryContext`。
- 提供 `TraceContextRegistry` 和 `TraceContextRuntime`。
- 提供 `TraceContextBeanLocator`。
- 提供 `SystemCodeProvider`。
- 提供 `InboundTraceContext`、`OutboundTraceContext`、`TraceMdcContext`。
- 提供 `AbstractTraceContextItem`、`AbstractSingleValueTraceContextItem`、`AbstractPropDrivenTraceContextItem`。
- 支持 Java SPI 注册 item factory。
- 单元测试覆盖上下文读写、快照、restore、scope 恢复、runtime 编排、同序排序、props 校验失败、context / MDC 名称冲突、span 嵌套、异常退出清理。

### starter

- 新增 `fist-trace-context-spring-boot-starter` 模块。
- 提供配置绑定和配置元数据。
- 统一读取和解析 `fist.trace-context` 配置，并创建 `TraceContextRuntime`。
- `fist.trace-context.enabled=false` 时不创建 `TraceContextRuntime` 和 `TraceContextRegistry`。
- 提供默认 item factory：`correlation-id`、`system-code`。
- 提供 Servlet Filter，承接原 `HeaderMdcFilter` 的入口请求 ID 和 MDC 能力。
- 提供 RestClient 下游透传能力。
- 提供 `TaskDecorator` 异步上下文传递。
- 提供 `@TraceSpan` 和 `@TraceSpanGroup` 注解。
- 提供 MDC 同步和清理。
- 测试覆盖自动装配、`enabled=false`、入口解析、缺失生成、MDC 写入、MDC 清理、RestClient 透传、注解 span、异步 restore。

### 既有模块治理

- `fist-boot-web-app` 删除旧 `HeaderMdcFilter` 自动配置。
- `fist-support-web` 保留 `HeaderMdcFilter` public 类并标记 deprecated，不再对外推荐使用。
- `fist-support-web` 保留 `TraceInfoResolver` public 接口并标记 deprecated；默认异常事件从 `TraceContext` 构造 `TraceInfo`。
- `fist-support-web` 基于 `TraceContextRuntime` 重新实现 `MdcContextLifter`，不得读取 `fist.trace-context` 配置。
- `fist-cloud-gateway/fist-gateway-auth-core` 改造 `RequestIdGlobalFilter`，写入与 `MdcContextLifter` 一致的 Reactor Context 载荷。
- `fist-support-web` 改造 `GlobalErrorAttributes`，从 `TraceContext` 或 Reactor Context 载荷读取 requestId。
- `fist-cloud-rpc-feign` 删除 `HeaderRelayHandler`，新增 `TraceRelayHandler`，存在 `TraceContextRuntime` bean 时把 `TraceRelayHandler` 加入 `RelayInterceptor` handler 链。
- 测试覆盖旧 filter 不再注册、异常事件读取 `TraceContext`、Feign 单拦截器注册、启用 starter 后 Feign 通过 `TraceRelayHandler` 透传且保留认证 header、`enabled=false` 时不注册 `TraceRelayHandler`、reactive MDC 通过 runtime 恢复、reactive 信号切换后 MDC 无残留、reactive 异常事件读取 requestId、gateway 转发仍写入 `X-REQ-UID`。

### 文档

在 `fist-trace-context-spring-boot-starter` 模块放置简洁 README。

starter README 必须包含：

- 依赖引入。
- 启用配置。
- 默认使用 `X-REQ-UID` / `requestId`。
- 最小配置。
- 默认能力说明。
- 日志 pattern 示例。
- `@TraceSpan` 示例和 AOP 限制。
- 自定义 item 的最短路径链接或简例。

starter README 不展开旧模块迁移背景，不复制本方案大段内容。

迁移说明按原模块归属维护：

- `fist-web` README 或 changelog 说明：原 `HeaderMdcFilter` 默认能力已迁移到 `fist-trace-context-spring-boot-starter`；默认 `TraceInfoResolver` 不再作为异常事件追踪信息来源；迁移到 starter 的最小配置。
- `fist-cloud-rpc-feign` README 或 changelog 说明：`HeaderRelayHandler` 已由 `TraceRelayHandler` 取代；原 `X-REQ-UID` 透传能力由默认 `correlation-id` item 提供；启用 starter 后 Feign 通过 handler 链透传 trace header。
- `fist-cloud-gateway` 或 reactive Web 文档说明：`RequestIdGlobalFilter` / `MdcContextLifter` / `GlobalErrorAttributes` 的 Reactor Context 协议变更。

### 示例

新增两个用户侧示例，路径为 `examples/fist-trace-context/`。

#### 基本示例

示例名：`example-trace-context-basic`。

目标：

- 演示默认能力。
- 只配置 `correlation-id`、`system-code` 和 span API。
- 提供一个 HTTP 接口。
- 请求缺失 `X-REQ-UID` 时自动生成。
- 日志中可看到 `requestId`、`systemCode`、`spanId`。
- 演示 `@TraceSpan` 或 `TraceContexts.withSpan` 其中一种。

#### 自定义扩展示例

示例名：`example-trace-context-extension`。

目标：

- 演示如何自定义 item。
- 示例 item 可选 `tenant-id` 或 `operator-id`。
- 实现 `TraceContextItemFactory`。
- 继承 `AbstractSingleValueTraceContextItem` 或 `AbstractPropDrivenTraceContextItem`。
- 从私有 `props` 读取 header 名和 MDC 名，或展示硬编码方式。
- 通过配置启用该 item。
- 日志中可看到自定义上下文项。

## 首阶段不做

- 不接入 OpenTelemetry。
- 不兼容 W3C `traceparent`。但默认链路 ID 设计不得阻碍后续增加 `traceparent` item。
- 不做完整可观测性平台能力。
- 不提供 metrics、tracing exporter、采样器。
- 不改造业务项目。
- 不迁移已有日志配置。
- 不把业务流水号、租户号、机构号作为框架默认 item。
- 不强制所有 item 使用统一 header / MDC 配置模型。
- 不支持完整 WebFlux / Gateway 入口追踪上下文能力；首阶段只改造 reactive MDC 恢复、reactive 异常事件 requestId 来源和 `RequestIdGlobalFilter` 的 Reactor Context 写入协议。
- 不支持消息队列上下文传递。
- 不支持 RestTemplate、WebClient 自动透传。

## 推荐实施顺序

1. 创建 `fist-trace-context` 模块。
2. 实现核心上下文、snapshot、restore、scope、span API。
3. 实现 item SPI、factory context、编排器和抽象基类。
4. 为核心库补单元测试。
5. 创建 `fist-trace-context-spring-boot-starter` 模块。
6. 实现配置绑定、配置元数据和默认 item factory。
7. 实现 Servlet Filter，并承接原 `HeaderMdcFilter` 能力。
8. 实现 MDC 同步和清理。
9. 实现 RestClient 透传。
10. 实现 TaskDecorator。
11. 实现 `@TraceSpan` / `@TraceSpanGroup`。
12. 移除旧 `HeaderMdcFilter` 自动配置，保留 public 类并标记 deprecated。
13. 移除旧默认 `TraceInfoResolver` bean，保留 public 接口并标记 deprecated，改造异常事件从 `TraceContext` 构造 `TraceInfo`。
14. 删除 `HeaderRelayHandler`，新增 `TraceRelayHandler`，并接入现有 `RelayInterceptor` handler 链。
15. 提供基于 `ServerHttpRequest` 的 `InboundTraceContext` 适配。
16. 改造 `RequestIdGlobalFilter` 的 Reactor Context 写入协议，并保留 `X-REQ-UID` 转发写回。
17. 基于 `TraceContextRuntime` 改造 `MdcContextLifter`，并验证每个信号回调后 MDC 清理。
18. 改造 `GlobalErrorAttributes` 的 requestId 来源。
19. 补 starter 和既有模块治理测试。
20. 编写 starter README 和原模块 README / changelog 迁移说明。
21. 添加基本示例。
22. 添加自定义扩展示例。

## 格式与构建要求

- 新增 Java 代码遵循现有版权头、包结构和导入顺序。
- Maven module 接入现有父 POM。
- 新增模块必须通过现有 `spring-javaformat-maven-plugin` 格式检查。
- 优先使用现有测试基建和 `ApplicationContextRunner` 风格测试自动装配。
