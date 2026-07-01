# fist-trace-context 首阶段方案技术评审（第 1 轮）

- 评审对象：`docs/plans/2026-07-01-trace-context-phase1.md`
- 评审范围：模块边界、核心抽象、默认能力、span 设计、配置与注册机制、MDC 与 HTTP 集成、破坏性、测试与验收
- 评审日期：2026-07-01

---

## 1. 结论

**不通过。**

方案在「与现有 trace 体系的关系」这一前置问题上完全空白：仓库内已经存在默认开启的 `HeaderMdcFilter`、已注册的 Feign `RelayInterceptor`、reactive 侧的 `MdcContextLifter`、以及被 `ApiLog` 监控消费的 `TraceInfo` / `TraceInfoResolver`，它们与方案要新建的入口 Filter、Feign 透传、MDC 同步在运行时直接冲突，且采用 `X-REQ-UID` / `requestId` 标准名，与方案示例的 `X-Trace-Id` / `traceId` 不一致。方案声称「不改造 fist-boot-web-app、开箱即用、低破坏」，三者无法同时成立。

其次，核心接口 `TraceContextItem` 的生命周期方法依赖一个未定义类型 `TraceContextLifecycle`，且签名不足以表达「读 header / 写上下文 / 写 MDC / 写出口 header」四种动作；`span-id` 同时作为「默认 item」和「span 栈管理对象」，模型自相矛盾。这两点会导致实现阶段无法落地，必须先改方案再动手。

---

## 2. 重大问题

### B1 与现有 `HeaderMdcFilter` 运行时双重装配冲突，方案未处理

- 严重级别：阻断
- 位置：方案「模块划分」「破坏性评估」「默认能力 chain-trace-id」；现有 `fist-boot-web-app/.../FistWebAutoConfiguration.java:76-87`、`fist-support-web/.../HeaderMdcFilter.java:38-91`

问题描述：

现有 `HeaderMdcFilter` 在 `FistWebAutoConfiguration` 中以 `@ConditionalOnProperty(prefix = "fist.web.filter.mdc", name = "enabled", havingValue = "true", matchIfMissing = true)` 装配，即业务项目只要引入 `fist-boot-web-app` 就默认开启。它的行为是：从 `X-REQ-UID` header 读取值，写入 MDC `requestId`，`finally` 中清理 MDC。

方案中 `chain-trace-id` 的默认行为（方案 119-127 行）与此几乎完全相同：从入口 HTTP header 读取、缺失生成、写入 `TraceContext`、写入 MDC、透传下游。方案同时声明「不改造 fist-boot-web-app」（方案 305 行）。

影响：

业务项目同时引入 `fist-boot-web-app` 与新 starter 时，两个 Filter 都会执行，出现以下不可预测行为：

- 同一请求被两个 Filter 各读一次 header、各写一次 MDC、各清理一次。
- header 名不一致（`X-REQ-UID` vs `X-Trace-Id`）时，日志里同时出现两套 ID。
- Filter 顺序未定义，`finally` 清理顺序未定义，可能互相清掉对方写入的 MDC。
- 这直接破坏方案「低破坏」「开箱即用」的承诺，且不属于方案「潜在行为变化」清单（方案 319-325 行）描述的范围，因为该清单假设只有新 starter 在写 MDC。

建议修改：

方案必须新增「与现有 trace 体系关系」章节，对 `HeaderMdcFilter` 明确采用以下其一并写进 README：

1. 复用：chain-trace-id 不再独立读 header / 写 MDC，而是依赖 `HeaderMdcFilter` 已写入的 MDC。但 `HeaderMdcFilter` 不支持「缺失生成」「span」「多 item」，需增强，违反「不改造」。
2. 替代：新 starter 装配自己的入口 Filter，并在 README 强制要求用户设置 `fist.web.filter.mdc.enabled=false`。这违反「开箱即用」，必须显式声明。
3. 共存：定义两个 Filter 的执行顺序、数据交接协议（谁读 header、谁写 MDC、谁清理），并保证 header / MDC 名一致。

三选一之前，方案不应进入实现。

---

### B2 与现有 Feign `RelayInterceptor` 下游透传双注册冲突

- 严重级别：阻断
- 位置：方案「验收标准 starter」；现有 `fist-cloud-rpc-feign/.../FeignClientAutoConfiguration.java:40-45`、`RelayInterceptor.java:32-41`、`HeaderRelayHandler.java:34-65`

问题描述：

现有 `FeignClientAutoConfiguration` 已经注册了一个 `RelayInterceptor`（实现 `feign.RequestInterceptor`）作为 Bean，内部由 `HeaderRelayHandler`（透传 `X-REQ-UID`）和 `UserRelayHandler`（透传用户 token）组成。它的关键实现是：通过 `HttpServletRequestUtil.getCurrentRequestIfAvailable()` 从当前线程的 `HttpServletRequest` 读取 header 再写入 `RequestTemplate`。

方案要求新 starter「提供 Feign 或 RestClient 至少一种下游透传能力」（方案 349 行），但未说明与现有 `RelayInterceptor` 的关系。

影响：

- 同一 Feign 客户端会同时存在两个 `RequestInterceptor`，Spring 会按顺序全部调用，两个都向下游写 header。
- header 名一致时覆盖顺序未定义；不一致时下游收到两套 trace header。
- 现有 `RelayInterceptor` 依赖当前线程有 `HttpServletRequest`，在异步、`@Scheduled`、消息消费等场景失效，这正是新方案要解决的问题；但只要旧的 `RelayInterceptor` 仍在 classpath 上自动装配，新实现就无法真正接管，缺陷依旧。
- 方案「不改造 fist-cloud-rpc-feign」与「提供下游透传」矛盾。

建议修改：

方案必须明确：

1. 新 starter 的 Feign 透传是替代还是补充 `RelayInterceptor`。
2. 若替代：说明如何在不改造 `fist-cloud-rpc-feign` 的前提下让新拦截器接管（例如 `@ConditionalOnMissingBean`、`@AutoConfigureBefore`、或更高 `@Order`）。
3. 若补充：定义二者 header 名与写入顺序协议，避免重复或互相覆盖。
4. 是否复用现有 `RelayHandler` 扩展点，将其从「读当前 request」改造为「读 TraceContext」，还是另起一套。

---

### B3 `TraceContextItem` 生命周期方法签名不可实现，依赖未定义类型

- 严重级别：阻断
- 位置：方案「核心接口和基类」198-216 行

问题描述：

方案给出的接口：

```java
public interface TraceContextItem {
    String type();
    default void validate(TraceContextItemSpec spec) {}
    default void onInbound(TraceContextLifecycle lifecycle) {}
    default void onOutbound(TraceContextLifecycle lifecycle) {}
    default void onMdc(TraceContextLifecycle lifecycle) {}
}
```

存在三个阻断缺陷：

1. `TraceContextLifecycle` 这个类型在整个方案中没有任何定义。它有哪些方法、item 能在它上面做什么，全部未知。这是实现者最先需要的契约，却是最空的。
2. item 要承担「从入口载体读取」「生成默认值」「覆盖已有值」「写入出口载体」「写入 MDC」「解释校验私有配置」六类动作（方案 49-55 行），但三个生命周期方法都只接收同一个 `lifecycle` 参数，无法表达「读 header」与「写 header」与「读上下文」与「写上下文」的区分。
3. 「覆盖已有值」（方案 51 行）需要 item 能看到前序 item 写入的值，但方案未定义 item 之间的数据可见性与执行顺序语义，`order` 字段（方案 106 行）只说「用于处理依赖、覆盖、兼容迁移、签名」，没有定义可见性规则。

影响：

- `chain-trace-id` 需要在 `onInbound` 中同时「读 header」「生成值」「写 TraceContext」「写 MDC」，单一 `lifecycle` 参数无法支撑，实现者只能猜测 `TraceContextLifecycle` 上有什么方法。
- 后续定制 item 的开发者无从下手，验收标准「item 编排」（方案 341 行）无法验证。
- 这是「抽象不足以支撑后续定制」的典型情况。

建议修改：

重写接口前先定义 `TraceContextLifecycle` 的公开契约。推荐拆分上下文对象并暴露明确方法，例如：

```java
interface InboundContext {
    String readHeader(String name);            // 读入口 header
    void putValue(String key, String value);   // 写当前 TraceContext
    String getValue(String key);               // 读前序 item 写入的值（定义可见性）
}
interface OutboundContext {
    void writeHeader(String name, String value);
    String getValue(String key);
}
interface MdcContext {
    void putMdc(String key, String value);
}
```

对应 `onInbound(InboundContext)` / `onOutbound(OutboundContext)` / `onMdc(MdcContext)`。同时明确定义：item 按序执行，后序 item 可读前序 item 写入的 value（即「覆盖」语义的实现基础）。

---

### B4 `span-id` 既是默认 item 又是 span 栈管理对象，模型冲突

- 严重级别：阻断
- 位置：方案「默认能力 span-id」141-151 行、「span API」154-192 行、「核心接口和基类」198-216 行

问题描述：

`span-id` 被列为三个默认 item 之一（方案 141 行），意味着它要参与 `onInbound` / `onOutbound` / `onMdc` 生命周期与 `order` 排序。但 span 同时需要 push/pop 栈、嵌套、`TraceScope` 恢复（方案 154-169 行），这是一套完全独立的执行模型。方案没有定义二者如何交互：

- `onOutbound` 时 `span-id` 输出什么？栈顶？整条栈？是否随 pushSpan 动态变化？
- `pushSpan("remote.pms.query")` 时，是否触发 `span-id` 这个 item 的某个生命周期回调？若触发，走 `onInbound` 还是别的？
- 多层 span 嵌套时，MDC 里的 span-id 写当前栈顶还是入口 span？
- `chain-trace-id` 与 `span-id` 谁先 `onInbound`？`order` 默认值都是 0。

影响：

- item 编排模型（线性、按序、单值生命周期）与 span 栈模型（嵌套、push/pop、作用域恢复）根本不同，强行让 span-id 套用 item 接口，实现时必然产生分歧。
- 方案自己也承认 `span-id`「和普通 item 不同，会频繁出现在业务代码中，因此需要额外 API 和注解」（方案 151 行），这本身就是模型不自洽的信号。
- 验收标准「span 嵌套」（方案 341 行）无法在当前模型下被稳定验证。

建议修改：

将 span 栈管理从 item 模型中剥离：

- span 是 `TraceContext` 内建的栈能力，由 `TraceContexts.pushSpan` / `TraceScope` / `@TraceSpan` 管理，不参与 item 的 `onInbound` / `onOutbound`。
- `span-id` 不是独立 item，而是「当前 span 栈顶帧」在 `TraceContext` 和 MDC 中的视图。入口时由框架创建根 span，pushSpan 时压栈并刷新 MDC，退出时弹栈恢复。
- 这样 item 模型保持线性，span 模型保持栈式，二者职责清晰。

如果一定要保留 span-id 作为 item，则必须定义它与 span 栈的完整交互协议，并给出多 span 嵌套时 MDC 与 outbound 的具体取值规则。

---

### B5 `props` 完全不校验且无配置元数据，配置错误不可发现

- 严重级别：阻断
- 位置：方案「配置模型」107-109 行

问题描述：

方案明确「`props`：item 私有配置。框架不校验、不解释」，且 `inbound-header` / `outbound-header` / `mdc-name` 不提升为通用配置（方案 109 行）。结合验收标准要求测试「入口解析、缺失生成、MDC 写入」（方案 352 行），存在以下缺口：

- `mdc-name` 拼错、`inbound-header` 写错，启动静默通过，运行时不生效且无任何日志告警。
- 没有配置元数据（`spring-configuration-metadata.json`），IDE 对 `props` 下的 key 无任何提示。
- 方案没有为 `validate` 失败定义启动失败策略，item 实现者不知道该 fail-fast 还是降级。
- 「测试可验证」无法成立：配置错误既不报错也不生效，验收方无法区分「功能没做」与「配置写错」。

影响：

- 业务接入后排查成本高，违背「测试不可验证」红线。
- 默认 item 的 `props` 最小集合未文档化（方案 109 行只说「可以从 props 读取，也可以硬编码」），README 与示例无从写起。

建议修改：

1. 方案强制要求：每个 item 必须在 `validate` 中校验自己的 `props`，校验失败抛明确异常。
2. 框架定义启动失败策略：`validate` 抛异常即启动失败（fail-fast），禁止静默降级。
3. 默认 item 必须文档化其 `props` 最小集合（key、类型、是否必填、默认值）。
4. 为默认 item 生成配置元数据提示（可通过 `@ConfigurationProperties` 的 `Map` 配合 additional-spring-configuration-metadata 提示已知 key）。

---

## 3. 高严重级问题

### H1 item 创建无法注入外部依赖

- 位置：方案「核心接口和基类」236-246 行

`TraceContextItemProvider.create(TraceContextItemSpec spec)` 只传入 spec。但 `system-code` 需要 `SystemCodeProvider`（方案 134 行），`chain-trace-id` 需要 ID 生成器，`span-id` 需要时钟。通过 Java SPI 注册的 provider 无法拿到 Spring 容器里的这些依赖。结果：`system-code` 作为默认 item 形同虚设，`chain-trace-id` 的 ID 生成只能硬编码。

建议：要么在 `create` 时注入一个 `ProviderContext`（可获取 BeanFactory、时钟、ID 生成器等共享组件），要么规定默认 item 只走 Spring Bean 注册（可 `@Autowired`），SPI 仅用于无外部依赖的纯逻辑 item。方案需要明确选择。

### H2 SPI 与 Spring Bean 双注册的优先级与冲突未定义

- 位置：方案「验收标准核心库」340 行、「默认能力」、对照 `fist-logback/.../RuleEngineLoader.java:91-129`

方案同时要求「支持 Java SPI 注册 item provider」（方案 340 行）和「提供默认 item provider」（方案 347 行，必然走 Spring Bean）。同一 `type` 若通过 SPI 和 Spring Bean 各注册一次，谁生效未定义。

对照现有 `fist-logback` 的 `RuleEngineLoader`，它在 provider name 冲突时直接抛 `IllegalStateException`（`RuleEngineLoader.java:97-100`、`117-120`）。方案应参考这一既有约定。

建议：明确优先级（例如 Spring Bean 优先于 SPI），并规定 `type` 冲突时 fail-fast，给出冲突日志格式。

### H3 `chain-trace-id` 默认 header / MDC 名与现有标准不一致

- 位置：方案 72-73、95-97 行；现有 `fist-support-web/.../HttpConstant.java:28`（`X-REQ-UID`）、`fist-kit-api/.../LogConstant.java:26`（`MDC_REQUEST_ID = "requestId"`）

方案示例用 `X-Trace-Id` / `traceId`，现有体系用 `X-REQ-UID` / `requestId`。即便标注为「示例」，默认值会被 README 和示例工程照抄。业务接入后，下游收到 `X-REQ-UID`（来自现有 `RelayInterceptor`）和 `X-Trace-Id`（来自新 starter）两套 header，日志出现 `requestId` 与 `traceId` 两个 MDC key。

建议：方案应规定 `chain-trace-id` 默认 header 与 MDC 名与现有标准对齐（`X-REQ-UID` / `requestId`），或在方案中明确说明二者并存期与迁移预期。这个问题不能推给「由 item 自己解释 props」。

### H4 异步上下文传递范围与 API 未定义

- 位置：方案「追踪上下文」42 行、「span API」154-169 行、「首阶段不做」399-408 行

方案说 `TraceContext` 支持「创建快照」用于异步任务传递（方案 42 行），但全文没有定义「快照如何 restore 到子线程」的 API，`TaskDecorator` 边界未提，「首阶段不做」清单也没有提及异步。`@Async`、`CompletableFuture`、线程池是 Java 后端的高频场景，这块范围不清会导致实现者自行发挥。

建议：明确首阶段是否支持异步传递。若支持，定义 `TraceContexts.restore(snapshot)` 与 `TaskDecorator` 集成；若不支持，写入「首阶段不做」并说明后续阶段规划。

### H5 MDC 清理与线程复用污染契约不完整

- 位置：方案「span API」169 行；对照现有 `HeaderMdcFilter.java:67-72`

方案只在 span 侧提到用 `TraceScope.close()` 避免线程复用污染（方案 169 行），但入口 Filter 的 MDC 清理契约未在方案体现。现有 `HeaderMdcFilter` 用 `try / finally` 在请求结束时 `MDC.remove`（`HeaderMdcFilter.java:67-72`）。新方案若不在入口 Filter 与 span scope 两侧都规定 `try-finally`，线程池复用时会残留上一次请求的 traceId / spanId。

建议：方案明确入口 Filter 与 `TraceScope` 的 MDC 清理契约，要求所有写入 MDC 的路径必须有对应的 `finally` 清理。

---

## 4. 中严重级问题

### M1 `@TraceSpan` / `@TraceSpanGroup` 的 AOP 依赖与代理限制未评估

- 位置：方案「span API」172-192 行

注解必然依赖 AOP（`spring-aop` + CGLIB 或 JDK 动态代理），但方案未列出该依赖坐标（核心库不能依赖 spring-aop，starter 需要显式引入）。同时：

- 自调用、`final` 方法、非 Spring Bean 上的 `@TraceSpan` 不生效，方案未在 README 要求声明。
- `@TraceSpanGroup("bank")` 类级前缀与 `@TraceSpan("submit")` 的拼接规则未定义（连接符、是否覆盖、是否支持 SpEL）。
- `@Async` 方法上 `@TraceSpan` 的 span 边界与线程边界不一致，span 可能跨线程不闭合。

建议：明确 AOP 依赖来源、注解失效场景写入 README、定义 `@TraceSpanGroup` 拼接语义。

### M2 reactive（WebFlux / Gateway）完全未覆盖

- 位置：方案「模块划分」27 行；现有 `fist-support-web/.../MdcContextLifter.java`、`MdcContextLifterConfiguration.java`

现有体系已有完整的 reactive MDC 支持（`MdcContextLifter` 通过 `Hooks.onEachOperator` 传递 MDC，`MdcContextLifterConfiguration.java:35-44`）。方案只提 Servlet Filter，`fist-cloud-gateway` 是 reactive 栈，业务用 Gateway 时 trace 会完全断裂。「首阶段不做」清单（方案 399-408 行）也没有写明 reactive。

建议：方案明确「首阶段不支持 reactive」，写入「首阶段不做」清单，并说明与现有 `MdcContextLifter` 的关系（不处理、保持现状）。

### M3 现有 `TraceInfo` / `TraceInfoResolver` 与新 `TraceContext` 数据源分裂

- 位置：现有 `fist-support-web/.../mon/info/TraceInfo.java`、`TraceInfoResolver.java`、`FistWebAutoConfiguration.java:64-74`、`ApiLogAspect.java:70`

`TraceInfo`（含 `requestId`、`userId`）和 `TraceInfoResolver` 已存在并被 `ApiLogAspect` 消费用于接口日志监控。新方案另起一套 `TraceContext`，两套数据源并存：`ApiLog` 拿到的是 `TraceInfo`，业务代码拿到的是 `TraceContext`，两边对 `requestId` 的来源（header 名、生成策略）可能不一致。

建议：方案说明是否复用或桥接 `TraceInfoResolver`；若不处理，说明 `ApiLog` 与新 `TraceContext` 数据源分裂的后果。

### M4 `order` 作用域、依赖表达、同序排序未定义

- 位置：方案「配置模型」106 行；对照 `RuleEngineLoader.java:86`

`order` 号称用于「依赖、覆盖、兼容迁移、签名」（方案 106 行），但 item 间依赖（例如 `span-id` 依赖 `chain-trace-id` 先生成）只能靠手填数字，易错。同 `order` 的排序未定义；`fist-logback` 用 `order` 升序 + 名称字典序兜底（`RuleEngineLoader.java:86`），方案可参考。

建议：明确同 `order` 兜底排序规则；评估是否需要 `dependsOn` 而非纯数字 `order`。

### M5 `validate` 与 `create` 时序倒置

- 位置：方案「编排流程」266-267 行

编排流程先 `create`（方案 266 行）再 `validate`（方案 267 行）。`validate(spec)` 的入参与 `create(spec)` 完全重复，且 validate 失败时实例已创建需要丢弃。

建议：将 spec 合法性校验提前到 `create` 之前由框架统一做（类型存在性、必填项），item 内部校验放在 `create` 实现里抛异常即可，去掉冗余的 `validate` 方法，或将其改为 `create` 前的静态校验钩子。

---

## 5. 低严重级问题

- L1 「追踪上下文项」概念本身合理，比「字段」更合适。但 `TraceContextItem` 与 `TraceContext` 处于同一命名域，初次阅读易混。建议文档明确：item 是「参与 `TraceContext` 生命周期的扩展单元」，`TraceContext` 是「当前执行片段的只读视图」。
- L2 `AbstractSingleValueTraceContextItem` 与 `AbstractPropDrivenTraceContextItem` 职责重合（方案 251-258 行），两者都封装「入口解析、上下文写入、出口写入、MDC 写入」。多数 item 既是单值又是配置驱动，扩展者不知该继承哪个。建议合并为一个基类，或明确边界（例如 single-value 面向硬编码无配置 item，prop-driven 面向配置驱动 item）。
- L3 版本号缺失。`CLAUDE.md` 标注当前版本 `3.11-SNAPSHOT`，`fist-logback` 的 provider 标注 `since 3.12`（`TransformerProvider.java:26`），方案未声明本特性目标版本。
- L4 `inbound-header` 与 `outbound-header` 是否允许不同（如入口 `X-Trace-Id`、出口 `global_trace_id`）方案示例暗示可以（方案 71-78 行），但语义与必要性未说明，容易滥用。

---

## 6. 次要问题

- 方案「编排流程」将「MDC 阶段」单独列出（方案 282-285 行），但 MDC 同步实际紧跟 `onInbound` 之后，时序描述割裂，建议合并描述。
- 示例模块路径未明确。现有示例位于 `examples/fist-logback/`、`examples/fist-sde/`，新增示例应在 `examples/fist-trace-context/` 下，方案应写明。
- 方案未提及 `spring-javaformat-maven-plugin` 格式要求（`CLAUDE.md` 强制），新增模块必须配置。
- starter README 验收（方案 358-367 行）未要求说明与 `HeaderMdcFilter` 的关系、未要求配置元数据。
- 基本示例与扩展示例的验收「日志中可看到」（方案 382、397 行）依赖 logback pattern，但 pattern 由示例工程配置还是由 starter 提供未明确。
- 方案「破坏性评估」清单（方案 319-325 行）遗漏了「Filter 双重装配」「Feign 双 `RequestInterceptor`」「MDC 双重 key」这三项最可能发生的破坏。

---

## 7. 开放问题

- O1 是否应直接增强现有 `HeaderMdcFilter` + `RelayInterceptor`，而不是新建独立模块？现有代码已覆盖「header→MDC」「Feign 透传」七成场景，缺的只是「缺失生成」「span」「异步」。若走增强路线，破坏面更小，但会违反「不改造现有模块」。需要人工决策：新建模块 vs 增强现有。
- O2 `chain-trace-id` 是否应直接采用 `X-REQ-UID` / `requestId` 作为默认值，与现有标准统一？这决定了 trace ID 在全公司的命名口径。
- O3 `system-code` 作为默认 item 是否合理？它依赖 `SystemCodeProvider`，并非所有项目都有。若作为默认，缺失依赖时降级还是报错？
- O4 Java SPI 是否必要？首阶段是否可以只用 Spring Bean 注册，降低 H1 / H2 的复杂度？SPI 的价值在于「核心库无 Spring 也能用」，但首阶段的核心场景就是 Spring Boot 应用。
- O5 是否首阶段就应明确与 W3C `traceparent` 的兼容立场？方案说首阶段不接入 OpenTelemetry（方案 401 行），但若 `chain-trace-id` 的 header 语义与 `traceparent` 冲突，后续兼容成本高。

---

## 8. 建议修改清单

以下条目可直接用于修订方案文档：

1. 新增「与现有 trace 体系关系」章节，逐项列出 `HeaderMdcFilter`、`RelayInterceptor`、`MdcContextLifter`、`TraceInfo` / `TraceInfoResolver`，每项明确「复用 / 替代 / 共存 / 不处理」并给出依据。
2. 在「默认能力 chain-trace-id」明确默认 header 与 MDC 名（建议对齐 `X-REQ-UID` / `requestId`），并说明与现有 `HeaderMdcFilter` 的共存或替代策略。
3. 重写「核心接口和基类」：先定义 `TraceContextLifecycle`（或拆分 `InboundContext` / `OutboundContext` / `MdcContext`）的公开方法集，再给出 `TraceContextItem` 签名。
4. 将 `span-id` 从 item 模型剥离，改为 `TraceContext` 内建 span 栈能力，`span-id` 作为栈顶视图。
5. 在 `TraceContextItemProvider` 增加 `ProviderContext` 注入参数，或规定默认 item 只走 Spring Bean 注册。
6. 明确 SPI 与 Spring Bean 的优先级与 `type` 冲突 fail-fast 策略，参考 `fist-logback` 的 `RuleEngineLoader` 约定。
7. 增加 `props` 校验要求与启动失败策略（fail-fast），文档化默认 item 的 `props` 最小集合。
8. 明确首阶段异步传递范围：若支持，定义 `TraceContexts.restore(snapshot)` 与 `TaskDecorator`；若不支持，写入「首阶段不做」。
9. 明确入口 Filter 与 `TraceScope` 的 MDC 清理契约（`try-finally`）。
10. 明确 `@TraceSpan` 的 AOP 依赖坐标与失效场景，定义 `@TraceSpanGroup` 拼接语义。
11. 「首阶段不做」清单补充：reactive（WebFlux / Gateway）、RestTemplate、WebClient、消息队列、OpenTelemetry 兼容立场。
12. 调整 `validate` 与 `create` 时序，去除冗余签名。
13. 补充版本号、`spring-javaformat` 配置、示例模块路径（`examples/fist-trace-context/`）。
14. 「破坏性评估」清单补充：Filter 双重装配、Feign 双 `RequestInterceptor`、MDC 双 key。

---

## 9. 下一轮评审建议

方案修订后，下一轮应重点复核：

1. `TraceContextLifecycle`（或拆分后的上下文对象）的最终方法集是否覆盖三个默认 item 的全部需求，尤其是 `chain-trace-id` 的「读 header + 生成 + 写上下文 + 写 MDC + 写 outbound」全流程。
2. span 栈与 item 编排剥离后的交互协议，span 嵌套时 MDC 与 outbound header 的取值规则。
3. 与 `HeaderMdcFilter` / `RelayInterceptor` 共存方案的可运行性，建议附最小 PoC 验证不会双重写 MDC、双重透传 header。
4. 默认 item 的 `props` 校验清单与配置元数据样例。
5. 异步传递 API（若纳入首阶段）与 `TaskDecorator` 的集成方式。
6. 三个默认 item（尤其 `system-code`）在缺失外部依赖时的降级或启动失败行为。
