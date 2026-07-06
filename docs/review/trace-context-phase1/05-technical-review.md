# fist-trace-context 首阶段实现技术评审（第 5 轮）

- 评审对象：`fist-kit3` 当前分支 `fist-kit3-jasypt` 的 `fist-trace-context` 首阶段实现代码
- 提交范围：`131e84b33d9baa905ed22b6320d4be8bb85b6210..HEAD`（`c5d402a5`）
- 第 4 轮后整改范围：`46892ca6545ee1ce8f16a8aa333d5a7011d4ffa9..HEAD`，共 7 个提交
- 历史评审：`01-technical-review.md`、`02-technical-review.md`、`03-technical-review.md`（方案）、`04-technical-review.md`（代码）
- 评审重点：第 4 轮 Blocking / Major / ponytail 项闭环复核、本轮 10 个重点、架构检查点、测试充分性
- 评审日期：2026-07-03

---

## 1. 结论

**可以收口。**

第 4 轮发现的唯一阻断（B1：starter 缺少 `AutoConfiguration.imports`）已闭环，且补了类路径发现测试防回归。四个 Major（M1 WebFlux 入站、M2 硬编码 context-name、M3 span AOP 开关、M4 TaskDecorator 共存）全部闭环，其中 M2 的实现优于第 4 轮建议——通过新增 `CorrelationTraceContextItem` 接口与 `TraceCorrelation` 工具，让 item 自声明语义、消费方按 registry 动态解析，从根本上消除了 context-name 硬耦合，而不是停留在「注释 + 文档」的弱方案。ponytail 两项（P1 移除 `TraceContextBeanLocator`、P3 合并 `Slf4jTraceMdcContext` 到 core）均闭环，P2 冲突校验按建议保留。

本轮 10 个重点逐项复核均成立：Spring Boot 3 自动配置发现正确、reactive 三件套经 `ReactiveTraceContext` 形成一致协议、异常 requestId 摆脱默认 context-name 耦合、TaskDecorator 可共存、span AOP 可关闭、BeanLocator 删除后扩展路径更直接、MDC 实现合并到 core 不破坏边界、各接入模块只复用核心 runtime、配置所有权集中在 starter。

本轮未发现新的 Blocking 或 Major 问题。剩余均为 Minor：reactive 入站协议在 gateway filter 与普通 WebFlux filter 之间代码重复、`TraceContextWebFilter` 缺少显式 `@Order` 与同步段注释、删除历史 `spring.factories` 未在 CHANGELOG 记录、Feign handler 顺序依赖隐式默认、systemCode 启动期冻结语义无 javadoc。这些不阻断首阶段收口，建议作为合入后的紧随任务处理，其中「reactive 入站协议去重」与「CHANGELOG 补录 spring.factories 清理」两项成本低、收益清晰，建议同批做掉。

---

## 2. Blocking

无。

第 4 轮 B1 已闭环，详见第 5 节。

---

## 3. Major

无新 Major。第 4 轮 M1–M4 经复核均已闭环（判定与证据见第 5 节）。

---

## 4. Minor

### 4.1 reactive 入站协议在 gateway filter 与普通 WebFlux filter 之间代码重复

- **位置**：`fist-cloud-gateway/fist-gateway-auth-core/.../filter/RequestIdGlobalFilter.java:74-91`（`filterWithTraceRuntime`）；`fist-kit-app/fist-web/fist-support-web/.../reactive/trace/TraceContextWebFilter.java:29-53`（`filter`）
- **现象**：两处的 reactive 入站编排逻辑近乎逐行相同——`runtime.inbound(new ServerHttpRequestInboundTraceContext(exchange.getRequest()))` → `MapOutboundTraceContext` → `runtime.outbound` → `mutateHeaders` → `traceContext.snapshot()` → `ReactiveTraceContext.putSnapshot` → `chain.filter(...).contextWrite(KEY_TRACE_CONTEXT, snapshot)` → `finally { TraceContexts.clear(); }`。`mutateHeaders` 私有方法也几乎一致。
- **为什么重要**：这是 reactive 入口的核心协议（snapshot 写入 + header 回写 + 线程局部清理）。协议任一处调整（例如 snapshot 包装方式、exchange attribute key、清理时机）都需要同步两处，极易漂移；且两处分别位于 gateway 模块与 web 模块，评审与测试都难以发现不一致。
- **建议修复方式**：把「同步段构建」抽取为共享工具，例如在 `fist-support-web` 的 `reactive/trace` 包提供一个静态方法 `ReactiveTraceContextHolder.prepare(ServerWebExchange, TraceContextRuntime)`，返回处理后的 `exchange` 与 `snapshot`（不含 `chain.filter`，因为两个 filter 的 chain 类型不同）。gateway filter 与 `TraceContextWebFilter` 都调用它，各自只保留 `chain.filter(...).contextWrite(...)` 与 `finally`。gateway-auth-core 已依赖 fist-support-web，无需新增模块依赖。
- **严重级别**：Minor（功能正确，纯维护性债务）。

### 4.2 `TraceContextWebFilter` 未显式声明执行顺序

- **位置**：`fist-support-web/.../reactive/trace/TraceContextWebFilter.java:20`（类声明无 `@Order`）；`MdcContextLifterConfiguration.java:59-64`（bean 定义无顺序）
- **现象**：`TraceContextWebFilter` 作为裸 `WebFilter` bean 注册，类上无 `@Order`，bean 定义也无顺序。对比 Servlet 侧 `TraceContextServletFilter` 已显式 `@Order(Ordered.HIGHEST_PRECEDENCE)`（`TraceContextServletFilter.java:22`），reactive 侧未做对称处理。
- **为什么重要**：trace filter 需要作为最外层 filter 执行，它的 `putSnapshot`（exchange attribute，同步）与 `contextWrite`（Reactor Context，面向下游）才能覆盖整个请求链。若业务 `WebFilter` 在 trace filter 之外先执行并发起依赖 snapshot 的异步操作，可能读不到 trace 载荷。当前无 `@Order` 时顺序依赖 bean 注册顺序，不够稳定。
- **建议修复方式**：在 `TraceContextWebFilter` 类上加 `@Order(Ordered.HIGHEST_PRECEDENCE)`（与 Servlet filter 对称），或在 `MdcContextLifterConfiguration#traceContextWebFilter` 用 `FilterRegistrationBean`/`Order` 显式化。同时在 README 的 WebFlux 边界说明处补一句顺序约定。
- **严重级别**：Minor。

### 4.3 `TraceContextWebFilter` 缺少同步段 / finally 清理语义注释

- **位置**：`TraceContextWebFilter.java:29-44`
- **现象**：`filter` 方法的 `try-finally` 中，`inbound` 设置了入口线程的 `TraceContextHolder` ThreadLocal（`DefaultTraceContextRuntime.inbound` → `TraceContextHolder.set`），`finally { TraceContexts.clear(); }` 在返回 mono 之前就清理入口线程局部，下游 mono 通过 Reactor Context 里的 snapshot 恢复。逻辑正确，但**无任何注释**。同样的结构在 gateway 的 `RequestIdGlobalFilter.filterWithTraceRuntime` 已有两行注释（`RequestIdGlobalFilter.java:76-77`，正是第 4 轮 m5 的整改成果），reactive 侧未对称补注释。
- **为什么重要**：这段「同步段构建上下文、finally 只清网关/入口线程局部、不影响下游 mono」的语义非常微妙，后续维护者容易误以为 `finally` 在请求结束时执行，或误以为 `clear()` 会清掉下游 mono 的上下文，从而误改结构。gateway 侧已认可该注释价值（m5 已闭环），reactive 侧应保持一致。
- **建议修复方式**：在 `TraceContextWebFilter.filter` 方法体上加与 `RequestIdGlobalFilter` 对称的注释，说明「同步段完成 inbound/outbound/snapshot；snapshot 已写入 Reactor Context 与 exchange attribute；finally 仅清入口线程局部，不影响下游」。
- **严重级别**：Minor。

### 4.4 删除 8 个模块历史 `spring.factories` 未在 CHANGELOG 记录

- **位置**：提交 `9f6c5c33 chore: remove legacy auto configuration factories`；`CHANGELOG.md`（`[Unreleased]` 段无对应 Removed/Changed 条目）
- **现象**：`9f6c5c33` 删除了 `fist-boot-apidoc`、`fist-boot-crud-mybatis`、`fist-boot-data`、`fist-boot-security`、`fist-boot-web-app`、`fist-cloud-rpc-feign`、`fist-redisson` 共 7 个模块的 `spring.factories` 文件，并从 `fist-jasypt-spring-boot-starter` 的 `spring.factories` 中移除 `EnableAutoConfiguration` 条目（保留 `EnvironmentPostProcessor`）。被删内容**全部是 `org.springframework.boot.autoconfigure.EnableAutoConfiguration` 条目**，没有其他 key。所有被删模块都已具备对应的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（已 grep 验证）。
- **为什么重要**：Spring Boot 3 起不再从 `spring.factories` 读取自动配置，这些条目在 SB3 下本就是 dead config，因此对 SB3 应用**无功能影响**；`jasypt` 的 `EnvironmentPostProcessor` 被正确保留（该 key 在 SB3 仍生效）。判断这是合理的 dead-config 清理，而非「有意破坏」。但删除跨 8 个模块的注册文件是面向下游的可见变更，CHANGELOG 完全未提及，下游维护者无从知晓该版本起这些模块不再提供 SB2 风格自动配置注册。第 4 轮在 CHANGELOG 已为 `AutoConfiguration.imports` 修复记录了 `Fixed` 条目（`CHANGELOG.md:28`），同一轮的 `spring.factories` 清理理应同处说明。
- **建议修复方式**：在 CHANGELOG `[Unreleased]` 的 `Removed` 节补一条：「移除 `fist-boot-*`、`fist-cloud-rpc-feign`、`fist-redisson`、`fist-jasypt-spring-boot-starter` 中冗余的 `spring.factories` 自动配置条目；Spring Boot 3 自动配置改由 `AutoConfiguration.imports` 提供，对 SB3 应用无功能影响。」若项目明确不保留 SB2 兼容，可一句带过。
- **严重级别**：Minor（无功能影响，纯治理透明度）。

### 4.5 Feign handler 执行顺序依赖 `UserRelayHandler` 的隐式默认

- **位置**：`fist-cloud-rpc-feign/.../TraceRelayHandler.java:37`（`@Order(Ordered.HIGHEST_PRECEDENCE)`）；`fist-cloud-rpc-feign/.../UserRelayHandler.java:38`（类声明无 `@Order`）；`RelayInterceptor.java:38-40`（`handlers.forEach` 按列表顺序执行）
- **现象**：`RelayInterceptor` 按 `List<RelayHandler>` 注入顺序逐个执行。`TraceRelayHandler` 显式 `@Order(HIGHEST_PRECEDENCE)`，`UserRelayHandler` 未标注 `@Order`，依赖「无注解即默认 `LOWEST_PRECEDENCE`」才能保证「trace 先、认证后」（方案要求 trace handler 先执行，避免认证 handler 覆盖 trace header）。`FeignClientAutoConfiguration` 把两个 handler 都注册为 bean，Spring 按 `@Order` 排序注入列表。
- **为什么重要**：当前顺序正确，但 `UserRelayHandler` 的顺序是隐式约定。若后续有人给 `UserRelayHandler` 加 `@Order`（或新增其他认证 handler），可能打破「trace 先执行」的协议，且 Feign 侧没有测试断言 handler 顺序。
- **建议修复方式**：给 `UserRelayHandler` 显式 `@Order(Ordered.LOWEST_PRECEDENCE)`（或一个明确低于 trace 的值），把「trace 先、认证后」从隐式默认变为显式契约；可选地在 `FeignClientAutoConfigurationTest` 补一个断言 `RelayInterceptor` 的 handler 列表中 `TraceRelayHandler` 在 `UserRelayHandler` 之前。
- **严重级别**：Minor。

### 4.6 `system-code` 启动期冻结语义仍无 javadoc（第 4 轮 m6 遗留）

- **位置**：`fist-trace-context-spring-boot-starter/.../SystemCodeTraceContextItemFactory.java:43-58`；`fist-trace-context/.../SystemCodeProvider.java`
- **现象**：`SystemCodeTraceContextItemFactory.create` 在 factory 创建期（即 registry 构建、应用启动期）一次性解析 `SystemCodeProvider` / `spring.application.name`，捕获到 effectively final 的 `systemCode`，之后每次 inbound 都返回同一个冻结值。该「启动期冻结、不支持运行时动态」语义在 `SystemCodeTraceContextItemFactory` 与 `SystemCodeProvider` 的 javadoc 中均未说明。第 4 轮 m6 已建议补注释，本轮未处理。
- **为什么重要**：`spring.application.name` 是静态的，冻结无问题；但若用户实现动态 `SystemCodeProvider`（例如按租户切换系统代码），冻结语义会与预期不符，且没有任何文档提示。这与第 4 轮 m6 的判断一致——首阶段实现可保留，但需文档化边界。
- **建议修复方式**：在 `SystemCodeTraceContextItemFactory` 类 javadoc 与 `SystemCodeProvider` 接口 javadoc 注明「systemCode 在启动期解析并冻结，运行时不随 `SystemCodeProvider` 变化」。
- **严重级别**：Minor（首阶段行为可接受，纯文档）。

---

## 5. 第 4 轮问题闭环复核

### 5.1 Blocking

| 第 4 轮 | 问题 | 整改提交 | 闭环状态 | 证据 |
| --- | --- | --- | --- | --- |
| B1 | starter 缺少 `AutoConfiguration.imports`，SB3 下无法自动装配 | `b0003455` | **已闭环** | 新增 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（单行注册 `TraceContextAutoConfiguration`）；`TraceContextAutoConfiguration` 改用 `@AutoConfiguration`；`TraceContextAutoConfigurationTest#shouldRegisterAutoConfigurationImportCandidate` 用 `ImportCandidates.load(AutoConfiguration.class, ...)` 验证类路径发现，精确覆盖「经 imports 文件被发现」而非「显式 `AutoConfigurations.of`」。CHANGELOG `Fixed` 已记录。 |

### 5.2 Major

| 第 4 轮 | 问题 | 整改提交 | 闭环状态 | 证据与判定 |
| --- | --- | --- | --- | --- |
| M1 | 普通 WebFlux 应用无入站采集 | `d649900d` | **已闭环** | 新增 `TraceContextWebFilter`（`fist-support-web/.../reactive/trace`），在 `MdcContextLifterConfiguration` 中以 `@ConditionalOnBean(TraceContextRuntime.class)` 装配，与 `MdcContextLifter`、`GlobalErrorAttributes` 经 `ReactiveTraceContext`（exchange attribute + Reactor Context 双写）形成闭环；`TraceContextWebFilterTest` 验证 snapshot 同时写入 exchange 与 Reactor Context 且请求结束后 ThreadLocal 无残留。README 第 5 行已说明「普通 WebFlux 应用需引入 `fist-boot-web-app`，存在 runtime 时注册 `TraceContextWebFilter`；gateway 由 `RequestIdGlobalFilter` 写入同一协议」。 |
| M2 | 异常事件 / 错误响应硬编码 `"requestId"` context-name | `d649900d` | **已闭环（优于建议）** | 新增 core 接口 `CorrelationTraceContextItem`（item 自声明 `contextName()` 与 `resolve(context/snapshot)`）+ 工具类 `TraceCorrelation`（从 `runtime.registry().items()` 查找首个 `CorrelationTraceContextItem` 解析）；`CorrelationIdTraceContextItemFactory` 创建的 item 同时实现该接口。`AbstractExceptionHandler.currentRequestId` 与 `GlobalErrorAttributes.requestId` 主路径均走 `TraceCorrelation`，硬编码 `"requestId"` 仅作「无 correlation item」兜底。`GlobalErrorAttributesTraceContextTest#shouldReadRequestIdFromConfiguredCorrelationContextName`、`AbstractExceptionHandlerTraceContextTest#shouldCreateTraceInfoFromConfiguredCorrelationContextName` 用自定义 `correlationId` context-name 验证仍能输出稳定 `requestId`。该方案把硬耦合转为接口契约，优于第 4 轮建议的「注释 + 文档」。 |
| M3 | starter 强制带入 AOP 依赖与无条件 `@EnableAspectJAutoProxy`/aspect | `84afc677` | **已闭环（实现取舍合理）** | `@EnableAspectJAutoProxy` 从主配置类移至独立 `TraceSpanAopConfiguration`（`proxyBeanMethods=false` + `@ConditionalOnProperty(span.enabled, matchIfMissing=true)`）；`TraceSpanAspect` bean 同样加 `@ConditionalOnProperty(span.enabled)`；`TraceSpanAspectTest#shouldDisableSpanAspectByProperty` 验证 `span.enabled=false` 时不创建 aspect。「不主动启用 AOP、可关闭」目标达成。第 4 轮建议的「`spring-aop`/`aspectjweaver` 改 `optional=true`」未做——但 starter 默认开箱提供 `@TraceSpan` 能力，AOP 是该能力的必要依赖，`optional` 化会让默认体验退化为「注解静默失效」，与「starter 默认能力」定位不符。判定：依赖保持 compile scope 是与「默认启用 span」一致的合理取舍，不算缺陷。 |
| M4 | `TraceContextTaskDecorator` 与用户自定义 `TaskDecorator` 互斥 | `6bed3c1f` | **已闭环** | 改为 `BeanPostProcessor`（`TraceContextTaskDecoratorBeanPostProcessor`，static bean、`ObjectProvider<TraceContextRuntime>` 延迟取依赖）方案：对容器中所有非 `TraceContextTaskDecorator` 的 `TaskDecorator` bean 包装为 `CompositeTraceContextTaskDecorator`（先 user 装饰、再 trace 装饰，运行时 trace restore 先生效），用户装饰逻辑与 trace 恢复并存。`TraceContextTaskDecoratorTest#shouldComposeUserTaskDecoratorWithTraceTaskDecorator` 验证用户标记与 trace requestId 同时生效且 MDC 无残留。 |

### 5.3 Minor（第 4 轮 m1–m6）

| 第 4 轮 | 问题 | 闭环状态 | 证据 |
| --- | --- | --- | --- |
| m1 | core 声明 `slf4j-api` 但代码未用 | **已闭环** | `Slf4jTraceMdcContext` 合并到 core 后引用 `org.slf4j.MDC`，依赖被使用（见 P3）。 |
| m2 | `TraceContextServletFilter` 未显式声明注册顺序 | **已闭环** | `TraceContextServletFilter` 类上 `@Order(Ordered.HIGHEST_PRECEDENCE)`（`TraceContextServletFilter.java:22`）。注：reactive 侧 `TraceContextWebFilter` 未做对称处理，见本轮 4.2。 |
| m3 | `@TraceSpan(".method")` 前导点转义无文档无测试 | **已闭环** | `TraceSpan` javadoc（`TraceSpan.java:11、20-23`）、README（第 84 行）、`TraceSpanAspectTest#shouldNotPrefixAbsoluteMethodSpan` 三处覆盖。 |
| m4 | `fist-gateway-auth-core` 依赖经 `fist-support-web` 传递 | **已闭环** | `fist-gateway-auth-core/pom.xml` 显式声明 `fist-trace-context`（diff +4）。 |
| m5 | gateway 同步段 / finally 语义缺注释 | **已闭环** | `RequestIdGlobalFilter.java:76-77` 已补两行注释。注：reactive 侧 `TraceContextWebFilter` 未对称补注释，见本轮 4.3。 |
| m6 | `systemCode` 启动期冻结语义无 javadoc | **未闭环** | `SystemCodeTraceContextItemFactory`、`SystemCodeProvider` 仍无相关 javadoc，见本轮 4.6。 |

### 5.4 ponytail 三项

| 第 4 轮 | 问题 | 整改提交 | 闭环状态 | 证据与判定 |
| --- | --- | --- | --- | --- |
| P1 | 移除 `TraceContextBeanLocator` | `e44558e7` | **已闭环** | `TraceContextBeanLocator`、`EmptyTraceContextBeanLocator`、`SpringTraceContextBeanLocator` 三个类已删除；`TraceContextItemFactoryContext` 仅剩 `idGenerator`；`SystemCodeTraceContextItemFactory` 改为直接注入 `ObjectProvider<SystemCodeProvider>` 与 `Environment`，`CorrelationIdTraceContextItemFactory` 用 `context.idGenerator()`。扩展方自定义 factory 仍是 Spring bean，直接 `@Autowired` 所需依赖，路径比 locator 间接层更直接。 |
| P2 | 保留 registry 冲突校验 | — | **保留（符合建议）** | `TraceContextRegistryBuilder#checkSingleValueConflicts`（`TraceContextRegistryBuilder.java:50-62`）仍对 `context-name`/`mdc-name`/`outbound-header` 做 fail-fast，包含第 3 轮补齐的 `outbound-header` 检测。 |
| P3 | 合并 `Slf4jTraceMdcContext` 到 core | `b0b1ce33` | **已闭环** | core 的 `com.power4j.fist.trace.context.carrier.Slf4jTraceMdcContext`（实现 `TraceMdcContext` + `AutoCloseable`，保存旧值→写新值→close 恢复）被 starter（filter/decorator/aspect）与 `MdcContextLifter` 共用；`fist-support-web/.../reactive/trace/Slf4jTraceMdcContext.java` 已删除。core 依赖 SLF4J 门面不破坏「core 保持通用」原则，与第 4 轮判断一致。 |

### 5.5 第 4 轮测试缺口（T1–T8）

| 缺口 | 状态 | 证据 |
| --- | --- | --- |
| T1 自动装配发现测试 | **已补** | `TraceContextAutoConfigurationTest#shouldRegisterAutoConfigurationImportCandidate`。 |
| T2 `@TraceSpanGroup` 前缀 + 前导 `.` 转义 | **已补** | `TraceSpanAspectTest#shouldPushMethodSpanWithClassGroup`、`#shouldNotPrefixAbsoluteMethodSpan`。 |
| T3 `SystemCodeProvider` 优先于 `spring.application.name` | **已补** | `TraceContextAutoConfigurationTest#defaultSystemCodeShouldPreferSystemCodeProvider`。 |
| T4 `enabled=false` 全局回退 | **部分** | 默认禁用已测（`#shouldStayDisabledByDefault`）；跨模块回退（Feign 不注册 `TraceRelayHandler`、`MdcContextLifter` 走旧路径、异常事件不依赖 runtime）未在一个集成测试里整体断言，见第 7 节。 |
| T5 自定义 `TaskDecorator` 与 trace 共存 | **已补** | `TraceContextTaskDecoratorTest#shouldComposeUserTaskDecoratorWithTraceTaskDecorator`。 |
| T6 自定义 correlation context-name 下异常解析 | **已补** | `GlobalErrorAttributesTraceContextTest#shouldReadRequestIdFromConfiguredCorrelationContextName`、`AbstractExceptionHandlerTraceContextTest#shouldCreateTraceInfoFromConfiguredCorrelationContextName`、`TraceCorrelationTest`。 |
| T7 跨模块端到端链路 | **未补** | servlet 入站→Feign 出站、gateway 入站→下游 servlet 入站的端到端测试仍缺，见第 7 节。 |
| T8 reactive 入站（非 gateway）行为 | **已补** | `TraceContextWebFilterTest`、`MdcContextLifterTraceContextTest`。 |

---

## 6. 架构与兼容性评估

### 6.1 核心库边界

`fist-trace-context` 保持纯 Java，仅声明 SLF4J 门面依赖（合并 `Slf4jTraceMdcContext` 后该依赖被真正使用）。`TraceContext`/`TraceContextHolder`/`TraceContexts`/`TraceScope`/`TraceContextSnapshot` 提供只读视图、span 栈与快照恢复；`TraceContextRuntime` 作为其他模块的唯一运行时入口，`outbound` 在无当前 `TraceContext` 时整体跳过（`DefaultTraceContextRuntime.java:37-44`），`syncMdc` 按 item 序执行后写入内建 `spanId`（`DefaultTraceContextRuntime.java:46-54`）。`TraceContextRegistryBuilder` 对 `AbstractSingleValueTraceContextItem` 子类的 `context-name`/`mdc-name`/`outbound-header` 做 fail-fast（P2 保留）。core 不出现任何 Spring/Servlet/Feign/Logback 类型，边界干净。

`CorrelationTraceContextItem` + `TraceCorrelation` 是本轮新增的核心抽象，把「链路关联 ID」从 item 自声明角度暴露给消费方，是 M2 解耦的关键。该抽象不算过早抽象——异常事件、错误响应有真实的「跨 context-name 读取主关联 ID」需求；接口只定义 `contextName()` + 两个 `resolve` 默认方法，成本极低，消费方（`AbstractExceptionHandler`、`GlobalErrorAttributes`）已实际使用。

### 6.2 starter

`TraceContextAutoConfiguration` 用类级 `@ConditionalOnProperty(enabled=true)`（`TraceContextAutoConfiguration.java:41`）保证 `enabled=false` 时整个配置类不生效，所有 bean（含 `TraceContextTaskDecoratorBeanPostProcessor`）都不创建，`TraceContextRuntime` bean 不存在，其他模块经 `@ConditionalOnBean(TraceContextRuntime.class)` 自然回退。配置所有权集中在 starter：`TraceContextProperties` 只绑定 `enabled` 与 `items`，`span.enabled` 经 `additional-spring-configuration-metadata.json` 提示并由 `@ConditionalOnProperty` 直接读取（无需 Properties 绑定，合理）。默认 item 经 `defaultSpecs()` 提供 `correlation`（`X-REQ-UID`/`requestId`）与 `systemCode`（`spring.application.name`）。

`TraceSpanAopConfiguration`（`@EnableAspectJAutoProxy`）与 `TraceSpanAspect` 都受 `span.enabled` 控制，关闭时不启用 AOP、不创建 aspect。`spring-aop`/`aspectjweaver` 保持 compile scope 以支撑默认 span 能力，属合理取舍（见 5.2 M3 判定）。

### 6.3 既有模块治理

- `FistWebAutoConfiguration`（`fist-boot-web-app`）现仅保留 formatter 配置（`FistWebAutoConfiguration.java:43-62`），旧 `headerMdcFilter()` 与 `requestTraceInfoResolver()` 已彻底删除，无第二套默认 requestId/header/MDC 实现——本轮重点 9 成立。
- `FistReactiveWebAutoConfiguration` 经 `@Import` 装配 `GlobalErrorConfigure` 与 `MdcContextLifterConfiguration`，后者在 `@ConditionalOnBean(TraceContextRuntime.class)` 下注册 `TraceContextWebFilter`。
- `FeignClientAutoConfiguration` 用 `@AutoConfigureAfter(TraceContextAutoConfiguration)` + `@ConditionalOnBean(TraceContextRuntime.class)` 注册 `TraceRelayHandler`，单 `RequestInterceptor` bean，`UserRelayHandler` 无条件保留，handler 链顺序「trace 先、认证后」成立（顺序依赖 `UserRelayHandler` 隐式默认，见 4.5）。
- gateway `RequestIdGlobalFilter` 与普通 WebFlux `TraceContextWebFilter` 经 `ReactiveTraceContext` 同一协议写入 snapshot（重点 2 成立），未启用 runtime 时 `RequestIdGlobalFilter` 回退旧字符串协议、`MdcContextLifter` 回退 `KEY_MDC` 路径。

### 6.4 配置所有权

`fist.trace-context` 只由 starter 读取、解析、校验（`TraceContextAutoConfiguration` + `TraceContextRegistryBuilder`）。Feign、Web、reactive、异常事件均只消费 `TraceContextRuntime` / `TraceContextRegistry`，未各自重新读取 `fist.trace-context.items` 或解释 `props`——本轮重点 10 成立。

### 6.5 破坏性更新说明

CHANGELOG `[Unreleased]` 的 `Changed`/`Deprecated`/`Fixed` 已覆盖：`HeaderMdcFilter` 不再默认注册、`TraceInfoResolver` deprecated、Feign `TraceRelayHandler` 取代 `HeaderRelayHandler`、gateway 三件套协议变更、starter `AutoConfiguration.imports` 修复。唯一缺口是 `9f6c5c33` 删除 8 个模块 `spring.factories` 未记录（见 4.4）。除该项外，破坏性更新说明完整。

### 6.6 examples

`examples/fist-trace-context` 含 `example-trace-context-basic`（默认能力 + `@TraceSpan`）与 `example-trace-context-extension`（自定义 `TenantTraceContextItemFactory`），分别演示默认能力与自定义 item，符合方案验收。两者均为 Servlet 场景，无 reactive 端到端示例——首阶段可接受，但与 4.1（reactive 协议重复）叠加，后续若做 reactive 协议抽取，可顺带补一个 gateway/普通 WebFlux 最小验证示例。

---

## 7. 测试与验收评估

### 7.1 已覆盖（质量良好）

- core：`TraceContextsTest`、`DefaultTraceContextRuntimeTest`（inbound 冻结、outbound 跳过、syncMdc 含 spanId）、`TraceContextItemSupportTest`、`TraceContextRegistryBuilderTest`（排序、缺 factory、重复 processor、name/header 冲突）、`TraceCorrelationTest`（按 item contextName 解析、无 item 返回 empty）。
- starter：`TraceContextAutoConfigurationTest`（类路径发现、默认禁用、启用生成、`SystemCodeProvider` 优先）、`TraceContextServletFilterTest`、`TraceContextRestClientTest`、`TraceContextTaskDecoratorTest`（共存与清理）、`TraceSpanAspectTest`（group 拼接、前导点转义、`span.enabled=false`）。
- web：`MdcContextLifterTraceContextTest`（每信号 restore + close scope、MDC/ThreadLocal 无残留）、`TraceContextWebFilterTest`（snapshot 双写 + 清理）、`GlobalErrorAttributesTraceContextTest`（默认/自定义 context-name/无 item 回退）、`AbstractExceptionHandlerTraceContextTest`（默认/自定义 context-name/无 context）。
- gateway：`RequestIdGlobalFilterTraceContextTest`（snapshot 写入、header 保留、缺失生成）。
- feign：`FeignClientAutoConfigurationTest`。

第 4 轮 T1/T2/T3/T5/T6/T8 缺口均已补齐，且 T6 用「自定义 correlationId context-name 仍输出 requestId」精准覆盖 M2 的解耦目标，是这一轮整改质量最高的测试。

### 7.2 测试缺口（建议补齐，均不阻断收口）

| 编号 | 缺口 | 关联 |
| --- | --- | --- |
| N1 | `enabled=false` 跨模块整体回退：用一个集成场景断言「无 `TraceContextRuntime` bean 时，Feign 不注册 `TraceRelayHandler`（仍注册 `UserRelayHandler`）、`TraceContextWebFilter` 不注册、`MdcContextLifter` 走 `KEY_MDC`、异常事件不依赖 runtime」 | T4 |
| N2 | 跨模块端到端：servlet 入站→Feign 出站透传 trace header、gateway 入站→下游 servlet 入站，验证 `X-REQ-UID` 端到端一致 | T7 |
| N3 | Feign handler 顺序：断言 `RelayInterceptor` 的 handler 列表中 `TraceRelayHandler` 在 `UserRelayHandler` 之前 | 4.5 |
| N4 | reactive `TraceContextWebFilter` 顺序：补一个「业务 WebFilter 在 trace filter 内层仍能读到 snapshot」的用例，配合 4.2 加 `@Order` | 4.2 |

### 7.3 验证命令建议

tasks 文档已记录 starter、web、gateway、feign、examples 各模块的验证命令均通过。本轮无需新增验证命令，建议在补 N1–N4 测试后重跑：

```bash
cmd /c mvn -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test
cmd /c mvn -pl fist-kit-app/fist-web/fist-boot-web-app,fist-kit-app/fist-web/fist-support-web -am "-Dtest=FistWebAutoConfigurationTest,TraceContextWebFilterTest,MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest,AbstractExceptionHandlerTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
cmd /c mvn -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=FeignClientAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

---

## 8. 建议后续任务

按优先级给出可执行任务；首阶段可先收口，以下作为合入后的紧随工作。

1. **[Minor] reactive 入站协议去重**（4.1）—— 在 `fist-support-web/.../reactive/trace` 抽取共享工具（`prepare(exchange, runtime)` 返回 exchange + snapshot），`RequestIdGlobalFilter` 与 `TraceContextWebFilter` 复用。成本低，消除跨模块协议漂移风险。
2. **[Minor] CHANGELOG 补录 `spring.factories` 清理**（4.4）—— 在 `[Unreleased]` 的 `Removed` 增加一条，说明 8 个模块移除冗余 `EnableAutoConfiguration` 条目、SB3 由 `AutoConfiguration.imports` 提供、无功能影响。
3. **[Minor] `TraceContextWebFilter` 加 `@Order` + 同步段注释**（4.2 + 4.3）—— 与 Servlet filter、gateway filter 对称，稳定 reactive filter 顺序、保护微妙语义。
4. **[Minor] Feign handler 顺序显式化**（4.5）—— `UserRelayHandler` 加 `@Order(LOWEST_PRECEDENCE)`，补 handler 顺序断言测试（N3）。
5. **[Minor] `system-code` 冻结语义 javadoc**（4.6 / 第 4 轮 m6）—— 在 `SystemCodeTraceContextItemFactory` 与 `SystemCodeProvider` 注明启动期冻结。
6. **[测试] 补 N1（`enabled=false` 跨模块回退）与 N2（端到端链路）**—— 把第 4 轮 T4、T7 缺口收齐，验证迁移治理的破坏性边界。
7. **[可选] reactive 端到端示例**（6.6）—— 配合任务 1 的协议抽取，补一个普通 WebFlux 或 gateway 最小示例，演示 reactive 入站→MDC→异常事件全链路。

任务 1–2 建议在首阶段合入同批做掉（成本低、收益清晰，且 4.4 属本轮重点 8 的检查项）；3–7 可作为合入后的紧随任务。
