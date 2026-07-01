# fist-trace-context 首阶段方案技术评审（第 2 轮）

- 评审对象：`docs/plans/2026-07-01-trace-context-phase1.md`（修订版）
- 第 1 轮评审：`docs/review/trace-context-phase1/01-technical-review.md`
- 评审重点：第 1 轮问题是否真正解决、迁移方案可落地性、核心库边界、删除旧能力的破坏性
- 评审日期：2026-07-01

---

## 1. 结论

**不通过。**

相比第 1 轮，方案有实质进步：第 1 轮的五个阻断问题（B1-B5）和五个高优先级问题（H1-H5）在方案层面都得到了方向正确的回应——明确删除 `HeaderMdcFilter`、动态装配 `RelayInterceptor`、对齐 `X-REQ-UID` / `requestId`、将 span 从 item 模型剥离、factory 校验 props 并 fail-fast、统一 `TraceContextRuntime` 入口。这些回应不是文字粉饰，是真实的设计调整。

但第 2 轮的重点是「文字回应是否等于可落地」，结论是否定的：方案当前形态存在三个会导致实现失败的契约空白——reactive MDC 读写协议断裂、Feign 透传的组合机制与自动配置顺序缺失、核心库「纯 Java」与 factory 需要Spring Bean 查找的根本矛盾。这三点不补齐，方案无法进入实现。

阻断范围已从第 1 轮的「方向不清」收窄到第 2 轮的「三个具体契约空白」，修复成本可控。

---

## 2. 第 1 轮问题闭环检查

在展开第 2 轮新问题前，先逐项确认第 1 轮问题的真实状态，避免「假装解决」。

| 第 1 轮编号 | 问题 | 方案回应 | 本轮判定 |
| --- | --- | --- | --- |
| B1 | HeaderMdcFilter 双重装配 | 方案改为删除旧 filter 与自动配置，由 starter 提供新 filter（方案 83、106-119 行） | 方向解决，但引出二进制兼容问题（见 H5）与 reactive 漏网（见 B6、M5） |
| B2 | Feign 双 RequestInterceptor | 方案改为动态装配、保证单 bean（方案 85、121-134 行） | 方向正确，但组合机制与装配顺序空白（见 B7） |
| B3 | 生命周期方法签名不可实现 | 定义 `InboundTraceContext` / `OutboundTraceContext` / `TraceMdcContext`（方案 444-480 行） | 基本解决，但 `OutboundTraceContext.getValue` 数据来源未定义（见 H4） |
| B4 | span-id 模型冲突 | 明确 span 不是 item，是内建栈能力（方案 342-359 行） | 解决 |
| B5 | props 不校验 | factory 校验 + fail-fast（方案 278、544 行） | 解决 |
| H1 | factory 无法注入外部依赖 | `TraceContextItemFactoryContext`（方案 530-542 行） | 方向解决，但与「核心库不依赖 Spring」矛盾（见 B8） |
| H2 | SPI 与 Bean 优先级 | 明确 Bean 优先、重复 fail-fast（方案 585-596 行） | 解决，但优先级规则自相矛盾（见 M2） |
| H3 | 默认 header / MDC 名不一致 | 改为 correlation-id，默认 `X-REQ-UID` / `requestId`（方案 286-312 行） | 解决 |
| H4 | 异步传递 API 缺失 | 提供 `capture` / `restore` + `TaskDecorator`（方案 416-436 行） | 解决 |
| H5 | MDC 清理契约 | 明确 try-finally 清理（方案 644-651 行） | 解决 |

第 1 轮 10 个问题中，7 个真正解决，3 个方向正确但落地细节有新缺口。整体回应质量合格。

---

## 3. 重大问题

### B6 reactive MDC 读写协议断裂，写入侧未治理

- 严重级别：阻断
- 位置：方案「MdcContextLifter」136-146 行、「首阶段不做」777 行；现有 `fist-cloud-gateway/fist-gateway-auth-core/.../RequestIdGlobalFilter.java:42-54`、`fist-support-web/.../MdcContextLifter.java:67`、`ContextConstant.java:26`

问题描述：

reactive MDC 传播由两端组成：

- 写入侧 `RequestIdGlobalFilter`（gateway 模块）：`chain.filter(exchange).contextWrite(ctx -> ctx.put(ContextConstant.KEY_MDC, finalRequestId))`，向 Reactor Context 写入字符串 key `mdc_request_id`，值是 requestId 字符串（`RequestIdGlobalFilter.java:53`）。
- 读取侧 `MdcContextLifter`：`coreSubscriber.currentContext().getOrDefault(ContextConstant.KEY_MDC, null)`，从 Reactor Context 读字符串并写入 MDC（`MdcContextLifter.java:67-69`）。

方案治理表（方案 81-87 行）只列了 `MdcContextLifter`（读侧），要求改为从 `TraceContextSnapshot` 恢复（方案 86、142-143 行）。但写入侧 `RequestIdGlobalFilter` 不在治理表内，且方案 777 行明确「不支持完整 WebFlux / Gateway 入口追踪上下文能力」。

影响：

- 改造后 `MdcContextLifter` 期望从 Reactor Context 读 `TraceContextSnapshot` 对象，但 `RequestIdGlobalFilter` 仍写入字符串 `requestId`。读写类型不匹配，reactive MDC 恢复要么报错要么拿不到值，整个 reactive MDC 链路失效。
- 若为兼容而保留 `MdcContextLifter` 读字符串的逻辑，则与方案 77 行「不能同时存在两套」、方案 142 行「不再固定处理 requestId」直接冲突，`fist-trace-context` 无法成为唯一核心实现。
- 方案 582 行声称「`MdcContextLifter` 调用 restore 和 syncMdc」，但没有任何入口把 `TraceContextSnapshot` 注入 Reactor Context，`restore` 无源可读。

建议修改：

二选一，并在方案中明确：

1. 纳入写入侧：把 `RequestIdGlobalFilter` 列入治理表，要求它在 reactive 入口把 `TraceContextSnapshot`（或等价上下文）写入 Reactor Context，与 `MdcContextLifter` 的读侧协议对齐。这会触及 gateway 模块，但属于「fist-kit3 项目内」，不违反范围约束。
2. 暂停 reactive 改造：首阶段保留 `MdcContextLifter` 与 `RequestIdGlobalFilter` 现有字符串协议不变，不从 `TraceContextSnapshot` 恢复，待后续阶段统一治理 reactive 入口。此时需删除方案 86、142-146、582、712、797 行关于「基于 TraceContext 改造 MdcContextLifter」的描述，避免承诺无法兑现。

推荐方案 1，否则 reactive MDC 永远是「两套实现」的例外。

---

### B7 Feign 透传的组合机制与自动配置顺序空白

- 严重级别：阻断
- 位置：方案「RelayInterceptor」121-134 行；现有 `fist-cloud-rpc-feign/.../RelayInterceptor.java:32-41`、`HeaderRelayHandler.java:34-65`、`UserRelayHandler.java:38-65`、`FeignClientAutoConfiguration.java:40-45`

问题描述：

两个相互独立但都会导致实现失败的缺口：

第一，组合机制空白。现有 `RelayInterceptor` 持有 `List<RelayHandler>`，其中 `HeaderRelayHandler` 透传 `X-REQ-UID`，`UserRelayHandler` 透传用户 token（认证信息）。方案 131 行声明「`UserRelayHandler` 这类认证信息透传能力继续保留，不纳入 fist-trace-context」，方案 132 行要求「同一应用内只存在一个 `RequestInterceptor` bean」。那么这个唯一的 bean 必须同时执行两件事：调用 `TraceContextRuntime.outbound` 写 trace header，调用 `RelayHandler` 链写认证 header。方案没有定义这两套逻辑如何组合到同一个 `RequestInterceptor`——是 `TraceContextRuntime.outbound` 内部回调 `RelayHandler`，还是新 interceptor 先跑 runtime 再跑 handler 链，还是 `RelayHandler` 扩展点改造为接收 `OutboundTraceContext`。这是实现者第一个会撞上的契约空白。

第二，自动配置顺序未定义。方案 134 行说「是否选择新实现，依据核心类或核心 bean 是否存在」，即用 `@ConditionalOnBean(TraceContextRuntime.class)` 之类判断。但 `TraceContextRuntime` 由 starter 装配，`fist-cloud-rpc-feign` 的 `FeignClientAutoConfiguration` 与 starter 自动配置的相对顺序未定义。若 Feign 配置先于 starter 评估，`@ConditionalOnBean` 检测不到 `TraceContextRuntime`，会误走旧实现，即使 starter 已启用。方案未规定 `@AutoConfigureAfter` / `@AutoConfigureBefore` 或 `BeanFactoryPostProcessor` 等顺序保证手段。

影响：

- 组合机制空白会导致 Feign 透传要么丢认证 header，要么丢 trace header，要么实现者各自发挥产生分歧。
- 装配顺序未定义会导致「启用了 starter 但 Feign 仍走旧实现」的隐蔽 bug，且单元测试用 `ApplicationContextRunner` 时序可能与生产不一致，难以暴露。

建议修改：

1. 明确定义组合机制：推荐将 `RelayHandler` 扩展点的签名从 `handle(HttpServletRequest, RequestTemplate)` 演进为 `handle(OutboundTraceContext)`，让 trace header 和认证 header 走同一套 `TraceContextRuntime.outbound` 编排；或定义一个 `CompositeRelayRequestInterceptor`，内部按固定顺序调用 runtime 与 handler 链。方案需画出这个组合关系。
2. 明确装配顺序：规定 `FeignClientAutoConfiguration` 使用 `@AutoConfigureAfter` 依赖 starter 的自动配置类，或改用 `BeanFactoryPostProcessor` 在所有 bean 定义完成后判断，避免 `@ConditionalOnBean` 的时序陷阱。
3. 验收标准补充：「启用 starter 时 Feign 透传同时包含 trace header 与认证 header」「异步线程发起 Feign 调用时 trace header 来源为 `TraceContext`」两项测试。

---

### B8 核心库「纯 Java」与 factory 需要 Spring Bean 查找的根本矛盾

- 严重级别：阻断
- 位置：方案「TraceContextItemFactory」526-543 行、「模块划分」91 行

问题描述：

方案 91 行声明「`fist-trace-context` 不依赖 Spring、Servlet、Feign、Logback」。方案 542 行同时声明 `TraceContextItemFactoryContext` 提供「Spring 环境下可选的 Bean 查找能力」。

这两条直接冲突。Bean 查找必然需要 `ApplicationContext` / `ObjectProvider` / `BeanFactory` 等 Spring API。如果 `TraceContextItemFactoryContext` 暴露这些类型，核心库就引入了对 Spring 的编译期依赖，违反 91 行声明；如果不暴露，factory 无法获取 Spring 容器中的 bean。

这对默认 item 是实际阻断：`system-code` 的取值顺序包含 `SystemCodeProvider`（方案 328 行），而 `SystemCodeProvider` 在真实业务里几乎一定是 Spring bean（由应用注入）。如果 `SystemCodeProvider` 是 bean，factory 必须能从容器查找它；如果它是普通对象，应用就没法用自己的实现替换。

方案全文没有定义这个 Bean 查找抽象的类型、归属和契约。

影响：

- 实现者要么让核心库偷偷依赖 spring-beans（破坏边界），要么放弃 Bean 查找导致 `SystemCodeProvider` 无法作为 bean 注入（默认 item 失去扩展性）。
- 无论选哪条，方案 65 行「`fist-trace-context` 是唯一核心实现」与方案 91 行「不依赖 Spring」不可能同时满足。

建议修改：

定义一个核心库自有的抽象，由 starter 注入实现：

```java
// 在 fist-trace-context 核心库
public interface TraceContextBeanLocator {
    <T> Optional<T> find(Class<T> type);
    <T> List<T> findAll(Class<T> type);
}
```

核心库只定义接口，不引用任何 Spring 类型。starter 提供基于 `ApplicationContext` 的实现，并通过 `TraceContextItemFactoryContext` 注入。这样既保住核心库的纯 Java 边界，又让 factory 能拿到 Spring bean。方案需明确这个抽象，并写明 `SystemCodeProvider` 既可由 `TraceContextBeanLocator` 查找，也可由 `TraceContextItemFactoryContext` 直接持有。

---

### B9 `enabled=false` 时 starter 的装配策略未定义，影响其他模块判断

- 严重级别：阻断
- 位置：方案「配置所有权」103 行、「RelayInterceptor」128-130 行、「破坏性评估」657-668 行

问题描述：

方案要求 `fist-cloud-rpc-feign` 等模块通过检测 `TraceContextRuntime` / `TraceContextRegistry` bean 判断是否启用新能力（方案 103、128、713 行）。但方案没有定义 `fist.trace-context.enabled=false` 时 starter 的行为：

- 若 `enabled=false` 仍装配 `TraceContextRuntime` bean（内部空实现），Feign 检测到 bean 存在，走新实现，但 runtime 实际不工作，透传静默失效。
- 若 `enabled=false` 完全不装配 `TraceContextRuntime`，Feign 走旧实现，行为正确，但方案需要明确这一装配契约。

方案 252 行有顶层 `enabled: true`，但全文没有说明它在 starter 装配中的作用。

影响：

- `enabled=false` 的语义模糊会导致「关不掉」或「关掉后行为不可预测」。验收标准（方案 692-705 行）没有 `enabled=false` 的测试用例，这个核心开关行为无法验证。
- 其他模块基于 bean 存在性判断的约定（方案 103 行）在 `enabled=false` 时不成立。

建议修改：

明确 `enabled=false` 时 starter 完全不装配 `TraceContextRuntime` / `TraceContextRegistry`，让其他模块的 `@ConditionalOnBean` 自然回退到旧实现。验收标准补充「`enabled=false` 时无 `TraceContextRuntime` bean」「`enabled=false` 时 Feign 走旧实现」两项测试。

---

## 4. 高严重级问题

### H1 多 `correlation-id` 实例的 `context-name` 冲突未检测

- 位置：方案「item ID 与 processor」197-211 行、「correlation-id 默认 props」303-312 行、「编排」482 行

方案 206-211 行示例两个 `correlation-id` 实例（`request` 和 `bankTrace`），两者默认 `context-name` 都是 `requestId`（方案 307 行）。按方案 482 行「后序 item 可以读取前序 item 写入的值」，两个 item 都执行 `InboundTraceContext.putValue("requestId", ...)`，后序覆盖前序，`request` 的值丢失。MDC 同理：两者默认 `mdc-name` 都是 `requestId`，互相覆盖。

方案把多实例作为核心卖点（方案 195、206 行），却没有定义多实例冲突的检测与处理。

影响：

- 用户按示例配置两个 correlation-id 后，只有最后一个生效，且无任何告警，排查困难。
- 多实例语义不明：是「同一 context-name 的多实例必须显式配置不同 name，否则启动失败」，还是「允许多实例覆盖」。

建议修改：

明确多实例规则：同一 `context-name` 或 `mdc-name` 出现多个可写 item 时，启动 fail-fast 并报告冲突；多实例必须显式配置不同的 `context-name`。或者重新评估「同一 processor 多实例」是否真的必要——如果业务需要两个不同的关联 ID（内部 trace + 银行 trace），它们本就是不同的 context 值，用不同 processor 或不同 context-name 才合理，默认值相同是陷阱。

---

### H2 span 的 outbound header 名与 MDC 组合规则缺失

- 位置：方案「span」342-359 行、「编排」635-640 行

span 不是 item，其 outbound 写入由 runtime 内建（方案 640 行「runtime 写入当前 span ID」）。但方案没有定义：

- span 写入的 outbound header 名。`correlation-id` 明确定义了 `X-REQ-UID`，span 的默认 header 名全文缺失。
- span MDC 写入的触发点。方案 358 行说默认 MDC 名 `spanId`，但 `TraceContextRuntime.syncMdc` 是「只跑 item 的 onMdc」还是「item onMdc + 内建 span MDC 写入」？方案 569、631 行只说调用 `syncMdc`，没说它包含 span 的 MDC。

影响：

- 实现者不知道 span ID 用什么 header 透传、由谁写 MDC，实现分歧不可避免。
- 验收标准「span 嵌套」（方案 692 行）无法验证，因为期望的 MDC 与 header 行为未定义。

建议修改：

补充 span 的默认 outbound header 名（或声明默认不透传 span ID，仅本地 MDC），并明确 `TraceContextRuntime.syncMdc` 的完整语义是「item onMdc 按序执行 + 内建 span 当前栈顶写入 MDC」。

---

### H3 `OutboundTraceContext.getValue` 与异步场景的数据来源未定义

- 位置：方案「生命周期上下文」456-468 行、「编排」635-640 行

`OutboundTraceContext` 暴露 `getValue(name)`，供 item 在 `onOutbound` 时读取自己要透传的值。方案 637 行说出口阶段「获取当前 `TraceContext`」，但没有明确 `OutboundTraceContext.getValue` 读的就是当前 `TraceContext`。

更关键的是异步场景：Feign 或 RestClient 调用可能发生在 `TaskDecorator` 恢复的线程、`@Async` 线程、消息消费线程。这些线程上 `TraceContextHolder` 是否有值，取决于 `TaskDecorator` 是否生效。若当前无 `TraceContext`，`OutboundTraceContext.getValue` 返回什么、`TraceContextRuntime.outbound` 整体行为是什么（跳过、写空、报错），方案未定义。

影响：

- 异步发起的下游调用透传行为不可预测，可能写空 header 覆盖下游默认值。
- 与 `RelayInterceptor` 旧实现「无当前 request 则跳过」的行为不对齐，可能引入回归。

建议修改：

明确 `OutboundTraceContext.getValue` 的数据源是当前 `TraceContextHolder` 持有的 `TraceContext`；明确当前无 `TraceContext` 时 `outbound` 整体跳过（不写任何 header），与旧实现的「无 request 则 skip」语义对齐。

---

### H4 `InboundTraceContext` 与只读 `TraceContext` 的冻结关系未定义

- 位置：方案「生命周期上下文」444-454 行、「编排」625-633 行

`InboundTraceContext.putValue` 是可写的，inbound 阶段多个 item 写入值。方案 630 行说「runtime 建立只读 `TraceContext`」。但 `InboundTraceContext` 写入的值如何冻结为 `TraceContext`、`TraceContext.getValue(name)` 是否读同一份 Map、inbound 完成后 `InboundTraceContext` 是否还能修改，方案都没说。

影响：

- 实现者不确定 `TraceContext` 与 `InboundTraceContext` 是同一个对象的两面还是两个对象，容易出现 inbound 后仍能修改的 bug，破坏只读语义。
- `TraceContextSnapshot`（方案 173 行）从 `TraceContext` 捕获，如果冻结关系不清，快照语义也不清。

建议修改：

明确 `InboundTraceContext` 是 inbound 阶段的可变构建器，inbound 完成后 runtime 调用类似 `freeze()` 产出不可变 `TraceContext`；`TraceContext.getValue` 与 `OutboundTraceContext.getValue` / `TraceMdcContext.getValue` 读同一份冻结数据。

---

## 5. 中严重级问题

### M1 reactive 异常事件 `GlobalErrorAttributes` 未纳入治理

- 位置：方案「TraceInfo / TraceInfoResolver」148-160 行；现有 `fist-support-web/.../web/reactive/error/GlobalErrorAttributes.java:42-44`

方案只治理 servlet 侧 `AbstractExceptionHandler`（确认是 `TraceInfoResolver` 消费方，`AbstractExceptionHandler.java:42-66`）。但 reactive 侧 `GlobalErrorAttributes` 直接读取 `X-REQ-UID` header 构造 `requestId`（`GlobalErrorAttributes.java:42-44`），不走 `TraceInfoResolver`，也不走 `TraceContext`。删除 `HeaderMdcFilter` 不影响它，但它读的是 header 而非 `TraceContext`，与「统一来源」目标不一致。方案治理表未列入。

建议：明确 `GlobalErrorAttributes` 的处理——改为从 `TraceContext` 读取，或声明首阶段不处理并记录为已知缺口。

---

### M2 SPI 覆盖规则自相矛盾

- 位置：方案「注册优先级」591-596 行

方案 594 行说「同一来源出现重复 factory `processor`，启动失败」，方案 595 行说「Spring Bean factory 覆盖 SPI factory 时记录 info 日志」。当 Spring Bean factory 与 SPI factory 的 `processor` 相同时，是「重复」还是「覆盖」？两条规则冲突。

影响：实现者无法判断同 `processor` 的 Bean + SPI 共存时应 fail-fast 还是 info 覆盖。

建议：明确「跨来源同 `processor` 视为 Spring Bean 覆盖 SPI，记 info 日志」「同一来源内同 `processor` 才 fail-fast」。

---

### M3 `HeaderMdcFilter` / `TraceInfoResolver` 硬删除的二进制兼容

- 位置：方案「破坏性评估」653-675 行；现有 `HeaderMdcFilter.java`、`TraceInfoResolver.java` 均为 public 类型

方案 112、154 行要求删除 `HeaderMdcFilter` 类与 `TraceInfoResolver` 接口。两者都是 public 类型，删除属二进制不兼容破坏。应用项目（`pac-cloud` 等）若直接引用这些类型（自定义注册 `HeaderMdcFilter`、自定义实现 `TraceInfoResolver`），升级 fist-kit 版本后编译失败。方案声明「不改造应用项目」（方案 74 行），但删除 public 类型会强制应用改代码。

方案的迁移要求（方案 670-675 行）只说「需要引入 starter」「需要迁移为读取 TraceContext」，没有提示「应用代码若直接引用被删类型会编译失败」。

影响：应用项目升级时的破坏面超出方案描述，迁移路径不完整。

建议：评估保留 `@Deprecated` 空壳类的软迁移方案——保留 `HeaderMdcFilter` 类（标记 deprecated，内部空实现或委托 starter），保留 `TraceInfoResolver` 接口（标记 deprecated）。这样应用代码引用不破坏，只是默认行为变化。若团队接受硬删除，需在破坏性评估与 README 明确「此为 major 版本二进制不兼容变更，应用必须改代码」。

---

### M4 `SystemCodeProvider` 契约与注入方式未定义

- 位置：方案「system-code」325-340 行、「TraceContextItemFactoryContext」535-543 行

`SystemCodeProvider` 是 `system-code` 的取值来源之一（方案 328 行）。方案没定义它：是核心库接口还是应用自定义 bean，是单例还是每次调用，是否线程安全，是否允许返回 null（回退到 `spring.application.name`）。它通过 `TraceContextItemFactoryContext` 注入，但 context 如何拿到它——由 starter 自动探测 bean 并放入 context，还是 factory 自己从 `TraceContextBeanLocator` 查找——未说明。这与 B8 直接相关。

建议：定义 `SystemCodeProvider` 为核心库接口，明确线程安全契约与返回 null 的语义；由 starter 通过 `TraceContextBeanLocator` 查找 bean 实现并注入 factory context。

---

## 6. 次要问题

- 方案未定义 `TraceContextConfigurationException`（方案 544 行）的归属模块、是否 checked、错误码格式。
- `order` 默认 0：`correlation-id` 默认 order 0，用户自定义 item 默认也是 0（方案 277 行），同序按 ID 字典序兜底（方案 280 行）。用户未必知道自定义 item 会与 `correlation-id` 同序并按字典序排列，README 应提示「需要明确控制顺序时显式配置 order」。
- README 验收要求「Feign 透传与 `TraceContext` 的关系」（方案 726 行），但 Feign 治理在 `fist-cloud-rpc-feign`，README 在 starter 模块，跨模块描述容易失同步。建议 Feign 透传说明放在 `fist-cloud-rpc-feign` 自身的文档，README 仅链接。
- 方案 406 行声明「首阶段不支持 SpEL」，但 `@TraceSpan` 的值如果是纯字符串，`bank.submit` 这种点分语义靠约定，没有校验，容易写错。建议明确 span name 的合法字符集与长度。
- 方案 9 行「目标版本暂定为 `3.15.0-SNAPSHOT` 后续版本线」表述含糊，建议给出确切版本号或版本范围。

---

## 7. 开放问题

- O1 reactive 治理是否应把 `RequestIdGlobalFilter` 纳入首阶段（见 B6）。纳入则 reactive 闭环，不纳入则 reactive MDC 改造应整体后移。需要人工决策。
- O2 是否保留 `HeaderMdcFilter` / `TraceInfoResolver` 的 `@Deprecated` 空壳以降低应用迁移成本（见 M3），还是接受硬删除。需要评估实际有多少下游直接引用这些类型。
- O3 `fist-cloud-rpc-feign` 判断新能力是否启用，用 `@ConditionalOnBean(TraceContextRuntime.class)` 还是 `@ConditionalOnClass` + 配置标志。前者依赖装配顺序（见 B7），后者需要额外的配置标志，与「配置只有 starter 读取」原则有张力。需要人工决策。
- O4 多 `correlation-id` 实例是否是真实需求（见 H1）。如果业务确实需要多个关联 ID，默认 `context-name` 相同是陷阱；如果不需要，方案 206-211 行的多实例示例应删除或加约束。

---

## 8. 建议修改清单

以下条目可直接用于修订方案文档：

1. **B6**：在治理表新增 `RequestIdGlobalFilter`，要求它在 reactive 入口写入 `TraceContextSnapshot`；或在「首阶段不做」明确 reactive MDC 改造整体后移，并删除方案中所有「基于 TraceContext 改造 MdcContextLifter」的描述。
2. **B7**：补充 Feign 透传组合机制（`RelayHandler` 与 `TraceContextRuntime.outbound` 如何共存于单 `RequestInterceptor`），明确 `FeignClientAutoConfiguration` 的自动配置顺序保证。
3. **B8**：定义核心库自有的 `TraceContextBeanLocator` 抽象，由 starter 注入实现，化解「纯 Java」与「Bean 查找」矛盾。
4. **B9**：明确 `enabled=false` 时完全不装配 `TraceContextRuntime` / `TraceContextRegistry`，补充对应验收测试。
5. **H1**：定义多实例 `context-name` / `mdc-name` 冲突检测规则（fail-fast），或给多实例示例加显式 name 约束。
6. **H2**：补充 span 默认 outbound header 名（或声明不透传），明确 `syncMdc` 包含「item onMdc + 内建 span MDC」。
7. **H3**：明确 `OutboundTraceContext.getValue` 数据源为当前 `TraceContext`，当前无 context 时 `outbound` 整体跳过。
8. **H4**：明确 `InboundTraceContext` 到只读 `TraceContext` 的冻结机制。
9. **M1**：治理表补充 `GlobalErrorAttributes` 的处理方式。
10. **M2**：澄清 SPI 覆盖规则——跨来源覆盖记 info，同来源重复 fail-fast。
11. **M3**：决定 `HeaderMdcFilter` / `TraceInfoResolver` 是硬删除还是保留 deprecated 空壳，更新破坏性评估与迁移要求。
12. **M4**：定义 `SystemCodeProvider` 接口契约与注入路径。

---

## 9. 下一轮评审建议

方案修订后，下一轮应重点复核：

1. B6 的 reactive 闭环：`RequestIdGlobalFilter` 写入侧与 `MdcContextLifter` 读取侧的协议是否对齐，最好有最小 PoC 验证 `TraceContextSnapshot` 能完整通过 Reactor Context 传递。
2. B7 的 Feign 组合：用一个端到端测试验证「启用 starter 后，Feign 请求同时携带 trace header 与认证 header，且在异步线程发起时 trace header 来源是 `TraceContext`」。
3. B8 的 `TraceContextBeanLocator`：验证核心库确实不出现任何 Spring 导入，`system-code` 能拿到应用自定义的 `SystemCodeProvider` bean。
4. B9 的 `enabled=false`：验证关闭后 Feign、异常事件、MDC 全部回退到旧行为，无 `TraceContextRuntime` bean。
5. 迁移破坏面：在一个真实下游（若 `pac-*` 项目可作参照）上验证升级路径，确认 `HeaderMdcFilter` / `TraceInfoResolver` 删除后的编译与运行时影响与方案描述一致。
6. reactive 异常事件：验证 `GlobalErrorAttributes` 在新体系下仍能拿到 `requestId`，不出现 reactive 异常事件 trace 丢失。
