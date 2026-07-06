# fist-trace-context 首阶段实现技术评审（第 4 轮）

- 评审对象：`fist-kit3` 当前分支 `fist-kit3-jasypt` 的 `fist-trace-context` 首阶段实现代码
- 提交范围：`131e84b33d9baa905ed22b6320d4be8bb85b6210..HEAD`（`5cd5f5a5a488ec57e2b30db049bd514193c7892a`）
- 历史评审：`01-technical-review.md`、`02-technical-review.md`、`03-technical-review.md`（均针对方案文档）
- 评审重点：本轮首次从代码基线、通用框架、兼容治理、长期维护视角审视已落地实现，并复核 `ponytail-review` 未直接处理的三项
- 评审日期：2026-07-02

---

## 1. 结论

**有条件进入合入前处理：必须先修复 B1（Blocking），其余 Major 项建议同批处理。**

实现整体把第 1-3 轮方案落到了代码：核心库保持纯 Java（仅 slf4j-api 依赖声明）、item/factory/registry/runtime 分层清晰、Servlet/RestClient/async/span 接入完整、Feign handler 链与 reactive 三件套按统一 snapshot 协议收敛、测试覆盖了核心读写与各接入路径。第 3 轮的四个中级问题（gateway 转发透传、reactive 入口调用链、Feign 破坏性表述、MdcContextLifter scope 关闭）在代码中均有对应实现，已闭环。

但本轮发现一个阻塞性装配缺陷：starter 缺少 `AutoConfiguration.imports`，在项目当前使用的 Spring Boot 3.5.x 下**无法被自动装配发现**，starter 对最终用户完全失效；现有测试用 `AutoConfigurations.of(...)` 显式注册配置类，恰好绕过了自动发现机制，掩盖了该问题。这是合入前必须修复的唯一阻断点。

Major 项集中在三处：普通 WebFlux 应用（非 gateway）没有入站采集、消费方硬编码默认 context-name、span 的 AOP 依赖与全局 `TaskDecorator` 注册策略对不需要该能力的用户不友好。这些不阻断合入，但建议在合入前一并处理，否则会在使用方暴露为隐蔽的功能缺失。

`ponytail-review` 三项复核结论：建议移除 `TraceContextBeanLocator`、保留 registry 冲突校验、合并两份 `Slf4jTraceMdcContext` 到 core（详见第 6 节）。

---

## 2. Blocking

### B1 starter 缺少 `AutoConfiguration.imports`，Spring Boot 3.x 下无法自动装配

- **位置**：`fist-kit-infra/fist-trace/fist-trace-context-spring-boot-starter/src/main/resources/META-INF/`；现有 `spring.factories`（`EnableAutoConfiguration=TraceContextAutoConfiguration`），缺失 `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **现象**：starter 只用旧式 `spring.factories` 注册自动配置。项目内其余所有 starter（`fist-jasypt-spring-boot-starter`、`fist-boot-web-app`、`fist-cloud-rpc-feign`、`fist-redisson` 等）都**同时维护** `spring.factories` 与 `AutoConfiguration.imports` 两个文件，唯独本模块只有前者
- **风险**：Spring Boot 3.0 起不再从 `spring.factories` 读取自动配置（官方文档明确：auto-configurations must be registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，not in `spring.factories`）。项目当前是 Spring Boot 3.5.x。最终用户引入 starter 并设置 `fist.trace-context.enabled=true` 后，`TraceContextAutoConfiguration` 不会被自动发现，`TraceContextRuntime`、Servlet Filter、RestClient interceptor、TaskDecorator、`TraceSpanAspect` 全部不会创建，starter 完全不生效。现有测试 `TraceContextAutoConfigurationTest`、`TraceContextServletFilterTest` 等均用 `ApplicationContextRunner.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class))` 显式注入配置类，**不经过自动发现**，因此测试全绿却掩盖了该缺陷
- **建议**：新增文件 `fist-kit-infra/fist-trace/fist-trace-context-spring-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，内容为单行：
  ```
  com.power4j.fist.trace.boot.autoconfigure.TraceContextAutoConfiguration
  ```
  并补一个通过类路径自动发现的测试（见第 7 节测试缺口 T1），防止回归
- **影响**：不破坏兼容；补一个 `@SpringBootTest` 或 `AutoConfigurationReport` 级别的发现测试即可。这是 starter 能否对外生效的前提，必须修复

---

## 3. Major

### M1 普通 WebFlux 应用（非 gateway）没有入站采集，starter 对 WebFlux 名不副实

- **位置**：starter 模块无 reactive `WebFilter`；`fist-kit-app/fist-web/fist-support-web/.../boot/web/reactive/log/MdcContextLifterConfiguration.java:34-53`（无条件 `@Configuration`，注入 `@Nullable TraceContextRuntime`）；`fist-support-web/.../reactive/log/MdcContextLifter.java:77-99`
- **现象**：入站采集（`runtime.inbound(...)` + snapshot）只有两条路径：servlet 的 `TraceContextServletFilter`（starter，第 27-38 行）和 gateway 的 `RequestIdGlobalFilter`（`fist-cloud-gateway/fist-gateway-auth-core`，第 74-89 行）。普通 WebFlux 应用引入 starter 后：servlet filter 不激活（非 servlet 应用），没有 reactive `WebFilter` 做 inbound + snapshot 写入 Reactor Context。`MdcContextLifterConfiguration` 被 `FistReactiveWebAutoConfiguration` 无条件 `@Import`，lifter 在 runtime bean 存在时会尝试读 `ReactiveTraceContext.getSnapshot(reactorContext)`，但无人写入，回落到旧 `KEY_MDC` 路径——也无生产者，最终 MDC 与 `TraceContext` 全程为空
- **风险**：starter README 第 3 行声明"默认能力覆盖入口请求采集、MDC 写入……"，对 WebFlux 用户不成立。问题极其隐蔽：starter 看似启用、runtime bean 存在、lifter 已挂载，但整条链路产出为空，只有在排查日志时才发现 `requestId` 一直为空。gateway 是 reactive 唯一入口是常见架构假设，但框架级 starter 不应隐式强依赖该假设
- **建议**：二选一：
  - **(a) 首阶段补 reactive 入站**：在 starter 增加一个 `@ConditionalOnWebApplication(type=REACTIVE)` 的 `WebFilter`，调用 `runtime.inbound(new ServerHttpRequestInboundTraceContext(exchange.getRequest()))` → `putSnapshot(exchange, snapshot)` → `contextWrite(KEY_TRACE_CONTEXT, snapshot)`，与 `MdcContextLifter` 闭环。`ServerHttpRequestInboundTraceContext` 已在 `fist-support-web`，需考虑是否上移到 starter 可见范围或由 starter 自带适配
  - **(b) 显式声明边界**：在 starter README 与 CHANGELOG 明确"首阶段 reactive 入站仅通过 gateway 的 `RequestIdGlobalFilter` 提供；普通 WebFlux 应用需自行注册 `WebFilter` 调用 `TraceContextRuntime`，或等待后续阶段"
- **影响**：选 (a) 需补 reactive filter 与端到端测试；选 (b) 仅补文档。不破坏现有兼容。建议首阶段至少选 (b) 把边界写清楚，避免使用方误判

### M2 `AbstractExceptionHandler` 与 `GlobalErrorAttributes` 硬编码 `"requestId"` context-name

- **位置**：`fist-support-web/.../servlet/error/AbstractExceptionHandler.java:67`（`context.getValue("requestId")`）；`fist-support-web/.../reactive/error/GlobalErrorAttributes.java:35,44`（`ATTRIBUTE_KEY_REQUEST_ID = "requestId"` 与 `snapshot.values().get("requestId")`）
- **现象**：两处消费方都用硬编码字符串 `"requestId"` 去 `TraceContext` / snapshot 取关联 ID。这个 key 来自默认 `correlation` item 的 `context-name`（`TraceContextAutoConfiguration.java:125-126`）
- **风险**：用户自定义 `correlation` item 改了 `context-name`（例如改成 `corrId`），异常报警事件（`HandlerErrorEvent.traceInfo`）和错误响应（`GlobalErrorAttributes`）里的 requestId 就静默丢失。这是默认配置与消费方之间的隐式耦合，没有任何编译期或启动期约束把它们绑在一起。`examples/example-trace-context-extension` 的配置恰好保留了 `context-name: requestId`，所以示例不会暴露该问题
- **建议**：
  - 短期（首阶段）：在这两个消费点的代码注释和 README 明确"此处假设 correlation item 的 `context-name` 为 `requestId`；自定义该 name 时需同步调整"
  - 更稳妥：让消费方从 MDC 取值（`requestId` 也是默认 MDC key，但与 context-name 解耦后可独立配置），或定义一个稳定的"主关联 ID"常量供生产者与消费者共享
- **影响**：短期方案不破坏兼容，仅补注释与文档。补一个自定义 context-name 的测试覆盖该边界

### M3 starter 强制带入 AOP 依赖与无条件 `@EnableAspectJAutoProxy`

- **位置**：`fist-trace-context-spring-boot-starter/pom.xml:54-61`（`spring-aop`、`aspectjweaver` 为 compile scope）；`TraceContextAutoConfiguration.java:40`（类级别 `@EnableAspectJAutoProxy`）；`TraceContextAutoConfiguration.java:102-106`（`TraceSpanAspect` 无条件注册）
- **现象**：span 注解能力（`@TraceSpan` / `@TraceSpanGroup` + `TraceSpanAspect`）依赖 spring-aop 与 aspectjweaver，这两个依赖是 compile scope（非 optional），且 `@EnableAspectJAutoProxy` 标在自动配置类上、`TraceSpanAspect` bean 无条件创建
- **风险**：
  1. 不需要 span 注解的用户也被强制带入 AOP 依赖与代理开销，违背"按需启用"的 starter 惯例
  2. 类级别 `@EnableAspectJAutoProxy` 会注册 `AutoProxyCreator`；若用户应用自身（或 Spring Boot 的 `AopAutoConfiguration`）已注册同类 bean，在 Spring Boot 3.x 默认禁用 bean 覆盖的背景下存在重复注册风险
  3. `TraceSpanAspect` 无条件实例化，即使没有任何 `@TraceSpan` 注解，仍会付出 AOP 代理扫描成本
- **建议**：
  - `spring-aop` / `aspectjweaver` 改为 `optional=true`（或拆出独立 span 子模块）
  - 去掉类级别 `@EnableAspectJAutoProxy`，依赖 Spring Boot 的 `AopAutoConfiguration` 自动开启代理；若确需显式控制，改为 `@ConditionalOnMissingBean(name = "org.springframework.aop.config.internalAutoProxyCreator")` 之类的守卫
  - `TraceSpanAspect` bean 增加 `@ConditionalOnProperty(prefix = "fist.trace-context.span", name = "enabled", havingValue = "true", matchIfMissing = true)`，让用户可关闭
- **影响**：默认行为不变（仍开箱即用），但用户可按需关闭 span、避免不必要的 AOP。需确认 starter 自身测试在去掉 `@EnableAspectJAutoProxy` 后仍能触发代理（Spring Boot 测试切片下可能需要显式开启）

### M4 `TraceContextTaskDecorator` 注册策略会与用户自定义 `TaskDecorator` 互斥并静默丢失 trace

- **位置**：`TraceContextAutoConfiguration.java:96-100`
- **现象**：
  ```java
  @Bean
  @ConditionalOnMissingBean   // 无 value，等价于 @ConditionalOnMissingBean(TaskDecorator.class)
  TaskDecorator traceContextTaskDecorator(TraceContextRuntime runtime) { ... }
  ```
- **风险**：`@ConditionalOnMissingBean` 不指定类型时按返回类型匹配，即"容器中已存在任意 `TaskDecorator` bean 则不创建 trace decorator"。Spring Boot 的 `TaskExecutionAutoConfiguration` 会把所有 `TaskDecorator` bean 应用到自动配置的 `ThreadPoolTaskExecutor`。若用户已定义自己的 `TaskDecorator`（很常见：附加 `MDC`、`SecurityContext`、`RequestScope` 等），trace 装饰器完全不创建，trace 上下文在 `@Async` / 线程池里**静默丢失**，无任何告警，排查困难
- **建议**：
  - 不要用 `@ConditionalOnMissingBean(TaskDecorator.class)` 守卫一个"全局唯一"的 `TaskDecorator`
  - 推荐改为：无条件注册 `TraceContextTaskDecorator`（它本身是一个独立 bean），让 `TaskExecutionAutoConfiguration` 同时应用多个 `TaskDecorator`；或提供一个 `CompositeTaskDecorator` 组合用户既有装饰器
  - 至少在 README 提醒"已有自定义 `TaskDecorator` 的用户需手动组合 `TraceContextTaskDecorator`"
- **影响**：调整 bean 注册条件；补一个"用户自定义 TaskDecorator 与 trace 共存"的测试。不破坏默认能力

---

## 4. Minor

### m1 core 模块 pom 声明 `slf4j-api` 但代码未使用

- **位置**：`fist-kit-infra/fist-trace/fist-trace-context/pom.xml:33-36`；`src/main` 下无任何 `org.slf4j` 引用（已 grep 验证）
- **现象**：core 依赖了 `slf4j-api`，但 main 代码零引用
- **风险**：无直接功能风险，但属"声明即承诺"的噪音，且与第 6 节 P3 决策直接相关
- **建议**：与 P3 联动——若把 `Slf4jTraceMdcContext` 移入 core，依赖被使用，保留；否则删除该依赖声明
- **影响**：无

### m2 `TraceContextServletFilter` 未显式声明注册顺序

- **位置**：`TraceContextAutoConfiguration.java:75-80`；`TraceContextServletFilter.java:19-40`
- **现象**：filter 作为裸 `Filter` bean 注册，无 `FilterRegistrationBean`、无 `@Order`。默认顺序为 `OrderedFilter.DEFAULT_FILTER_ORDER`
- **风险**：trace filter 需在业务 filter 之前写入 MDC，依赖隐式默认顺序不够稳定；旧 `HeaderMdcFilter` 明确用 `HIGHEST_PRECEDENCE`（见 `FistWebAutoConfiguration` 旧实现），迁移后顺序语义弱化
- **建议**：用 `FilterRegistrationBean` 显式设置 `HIGHEST_PRECEDENCE`（与旧 `HeaderMdcFilter` 一致），或在 filter 类加 `@Order` 并在 README 说明
- **影响**：行为更可预期，不破坏兼容

### m3 `@TraceSpan(".method")` 前导点绕过 group 的"转义"约定无文档无测试

- **位置**：`TraceSpanAspect.java:46-56`
- **现象**：`resolveSpanId` 中，若 `@TraceSpan` 的 value 以 `.` 开头，则去掉前导点且不拼接 `@TraceSpanGroup` 前缀。这是一个隐藏的"局部覆盖 group"约定
- **风险**：魔法字符语义无文档、无测试，维护者与使用方都难以发现；改 `resolveSpanId` 时容易误伤
- **建议**：在 `@TraceSpan` 的 javadoc 与 README 说明该语义，并补一个测试（带 group + 前导点 value 的组合）；或去掉该魔法，统一用"未标注 `@TraceSpanGroup` 即无前缀"表达
- **影响**：不破坏兼容；补测试 + 文档

### m4 `fist-gateway-auth-core` 直接使用 trace-context API，但依赖经 `fist-support-web` 传递

- **位置**：`fist-cloud-gateway/fist-gateway-auth-core/.../RequestIdGlobalFilter.java:24-28`（直接引用 `TraceContext`、`TraceContextRuntime`、`TraceContextSnapshot`、`TraceContexts`、`MapOutboundTraceContext`）；`fist-gateway-auth-core/pom.xml` 只声明 `fist-support-web`，未直接声明 `fist-trace-context`
- **现象**：该模块直接、大量使用 core API，但依赖是经 `fist-support-web` 传递带入
- **风险**：传递依赖不稳定，若 `fist-support-web` 将来不再传递 `fist-trace-context`，本模块编译断裂
- **建议**：在 `fist-gateway-auth-core/pom.xml` 显式声明 `fist-trace-context` 依赖（与 `fist-cloud-rpc-feign` 一致，后者已显式声明）
- **影响**：无行为变化，依赖更清晰

### m5 gateway `filterWithTraceRuntime` 的"同步段构建 / finally 清线程局部"语义需注释保护

- **位置**：`RequestIdGlobalFilter.java:74-89`
- **现象**：`inbound` → `outbound` → `mutateHeaders` → `snapshot` 全在 `try` 同步段完成，`finally { TraceContexts.clear(); }` 在返回 mono 之前清掉网关线程的 ThreadLocal，下游 mono 通过 Reactor Context 里的 snapshot 恢复
- **风险**：逻辑正确但非常微妙。后续维护者可能误以为"finally 在请求结束时才执行"或"clear 会影响下游 mono"，从而误改结构
- **建议**：在方法上加注释，说明"同步段完成上下文构建与 header 透传；snapshot 已捕获并写入 Reactor Context；finally 仅清网关线程局部，不影响下游"
- **影响**：无；纯注释加固

### m6 `SystemCodeTraceContextItemFactory` 在 create 期一次性解析并冻结 systemCode

- **位置**：`SystemCodeTraceContextItemFactory.java:38-54`
- **现象**：systemCode 在 factory.create（即 registry 构建、应用启动期）一次性解析，之后冻结在 item 实例里，每次 inbound 都返回同一个值
- **风险**：`spring.application.name` 是静态的，无问题；但若用户实现动态 `SystemCodeProvider`（例如按租户切换系统代码），冻结语义会与预期不符
- **建议**：保留当前实现（首阶段足够），但在 javadoc 注明"systemCode 在启动期解析并冻结，不支持运行时动态变化"
- **影响**：无

---

## 5. Suggestion

### s1 `TraceContextRuntime` 无 `pushSpan`，span 推送与 capture/restore 走两套入口

- **位置**：`TraceContextRuntime.java:9-23`（接口含 capture/restore 但无 pushSpan）；`TraceSpanAspect.java:32-37`（用 `TraceContexts.pushSpan` + `runtime.syncMdc`）
- **现象**：capture/restore 经 runtime，而 span 推送经 `TraceContexts` 静态。`TraceSpanAspect` 注入 `TraceContextRuntime` 仅为调 `syncMdc`
- **建议**：统一——要么 runtime 增加 `pushSpan`（与 capture/restore 同入口），要么 aspect 也只依赖 `TraceContexts`（不注入 runtime，MDC 由调用方在外层 filter/decorator 维护）。当前混用不算错误，但接口边界不够一致
- **影响**：可选重构，不阻断

### s2 `correlation-id` processor 名与默认 item ID `correlation` 接近

- **位置**：`CorrelationIdTraceContextItemFactory.java:20`（`PROCESSOR = "correlation-id"`）；`TraceContextAutoConfiguration.java:124`（item ID `correlation`）
- **现象**：processor 名（类型）与默认 item ID（实例）接近，配置时易混写
- **建议**：README 已用表格说清二者关系，可接受，保留现状。确保所有文档始终用全称区分
- **影响**：无

### s3 测试覆盖缺口（详见第 7 节）

需补充：自动装配发现测试（防 B1 回归）、`@TraceSpanGroup` 前缀与 `.` 转义、`SystemCodeProvider` 优先于 `spring.application.name`、`enabled=false` 全局回退、跨模块 servlet→Feign→gateway 链路。

---

## 6. `ponytail-review` 三项复核

本轮按要求重点复核上一轮 `ponytail-review` 未直接处理的三项，逐项给出保留/移除结论与替代方案。

### P1 `TraceContextBeanLocator` —— 建议移除

- **位置**：`TraceContextBeanLocator.java`、`EmptyTraceContextBeanLocator.java`、`SpringTraceContextBeanLocator.java`、`TraceContextItemFactoryContext.java:16-29`、`SystemCodeTraceContextItemFactory.java:39-42`
- **现象**：locator 抽象（接口 + 空实现 + Spring 实现，共 3 个类）+ `TraceContextItemFactoryContext.beanLocator` 字段，当前**只有 `SystemCodeTraceContextItemFactory` 一处**用于查 `SystemCodeProvider` bean。而所有 `TraceContextItemFactory` 本身都是 Spring bean
- **风险**：为单一使用点引入了一层间接 + 3 个支撑类。factory 既然是 Spring bean，完全可以直接 `@Autowired`/`ObjectProvider` 注入所需依赖，不需要一个"BeanLocator 抽象"把 Spring 的查找能力再包一层
- **建议**：移除 `TraceContextBeanLocator`、`EmptyTraceContextBeanLocator`、`SpringTraceContextBeanLocator`。`SystemCodeTraceContextItemFactory` 构造改为注入 `ObjectProvider<SystemCodeProvider>`，直接 `ifPresent` 取值。`TraceContextItemFactoryContext` 仅保留 `idGenerator`（若 `CorrelationIdTraceContextItemFactory` 也改为自带默认 UUID 生成，context 可整体移除，factory 各自注入所需）
- **替代方案影响**：factory 仍是 Spring bean，扩展方实现自定义 factory 时直接注入所需依赖即可，路径反而更直接。`examples` 中的 `TenantTraceContextItemFactory` 不使用 locator，不受影响。不破坏现有扩展路径
- **反驳"保留"的理由**：保留的唯一论点是"core 保持纯 Java、不出现 Spring 类型"——但 `TraceContextItemFactoryContext` 本身就在 core，且其 `idGenerator`（`Supplier<String>`）已经是纯 Java 抽象；Spring 侧的查找完全可以在 factory（Spring bean）里直接做，不需要 core 提供 BeanLocator 接口。该抽象是为不存在的需求而设

### P2 registry 冲突校验 —— 建议保留

- **位置**：`TraceContextRegistryBuilder.java:50-70`（`checkSingleValueConflicts`）
- **现象**：构建期对 `AbstractSingleValueTraceContextItem` 的 `context-name`、`mdc-name`、`outbound-header` 做唯一性 fail-fast，重复即抛 `TraceContextConfigurationException`
- **建议保留，理由**：
  1. 这是配置错误的早期诊断。没有它，两个 item 共用 `context-name` 会导致 `putValue` 静默覆盖；共用 `mdc-name` 会导致 MDC 互相覆盖；共用 `outbound-header` 会导致出口 header 互相覆盖——且全部无任何告警，排查极难
  2. 成本极低：一段线性扫描 + 三个 Map，构建期一次性执行，无运行时开销
  3. 第 3 轮评审的次要问题"未检测 `outbound-header` 冲突"已在本实现中补齐（`TraceContextRegistryBuilder.java:58-59`），说明该校验是必要的、被主动强化的
- **局限说明**：当前只校验 `AbstractSingleValueTraceContextItem` 子类；若用户实现完全自定义的 `TraceContextItem`（不继承该基类），绕过校验。建议在 `TraceContextRegistryBuilder` 加注释说明该边界，并在 README 提醒"自定义 item 的 name 唯一性由实现者自负"
- **影响**：无；保留

### P3 `Slf4jTraceMdcContext` 两份实现 —— 建议合并到 core

- **位置**：`fist-trace-context-spring-boot-starter/.../boot/autoconfigure/Slf4jTraceMdcContext.java`（package-private）与 `fist-support-web/.../boot/web/reactive/trace/Slf4jTraceMdcContext.java`（public），两份实现**逐字节相同**
- **现象**：同一套"保存旧 MDC → 写新值 → close 恢复"逻辑在两个模块各一份。starter 侧被 `TraceContextServletFilter`、`TraceContextTaskDecorator`、`TraceSpanAspect` 使用；reactive 侧被 `MdcContextLifter` 使用
- **风险**：重复实现是明显的维护债——修一处 MDC restore bug 需同步两处，极易漂移
- **建议**：合并到 core（`fist-trace-context`）。理由：
  1. core 的 pom **已经声明** `slf4j-api`（见 m1，当前未使用），把类移入 core 让该依赖名正言顺、消除"声明未用"的噪音（一举解决 m1）
  2. "trace context" 天然服务于日志 MDC，SLF4J 是 Java 事实标准日志门面，core 依赖 SLF4J 门面（不是具体实现 Logback/Log4j）不破坏"core 保持通用"的原则——这与 core 依赖 `java.util` 没有本质区别
  3. starter（filter/decorator/aspect）与 reactive lifter 都能直接引用 core 的版本，消除重复
- **替代方案考虑**：另一条路是让 `fist-support-web` 依赖 starter、复用 starter 的版本。但这会强制 `fist-support-web`（app 层基础库）带入 starter 的 spring-aop/aspectjweaver 重依赖，不可取。合并到 core 是最干净的选择
- **反驳"保持 core 不碰 SLF4J"的理由**：core 已经定义了 `TraceMdcContext` 接口（`putMdc`/`getValue`），MDC 概念已在 core 内。"core 不碰 SLF4J"在该前提下并非真正纯洁——只是把 SLF4J 绑定推到下游并重复实现。统一到 core 更诚实
- **影响**：删 1 个重复类，core 依赖不变（pom 已声明）。两处使用方改 import。需更新 2-3 个测试的 import。不破坏对外 API

### ponytail 三项小结

| 项 | 结论 | 关键理由 |
| --- | --- | --- |
| `TraceContextBeanLocator` | 移除 | 单一使用点、factory 本就是 Spring bean、抽象无实际价值 |
| registry 冲突校验 | 保留 | 配置错误 fail-fast、成本极低、已被第 3 轮主动强化 |
| `Slf4jTraceMdcContext` 合并 | 合并到 core | 两份逐字节相同、core pom 已声明 slf4j-api、一并解决 m1 |

---

## 7. 测试与验收评估

### 已覆盖（质量良好）

- core：`TraceContextsTest`（读写、snapshot、restore、scope 清理、span 三层嵌套、`withSpan`）、`DefaultTraceContextRuntimeTest`（inbound 冻结、outbound 跳过/读当前值、syncMdc 写 spanId）、`TraceContextItemSupportTest`（单值 item 入口/出口/MDC、required context-name 校验）、`TraceContextRegistryBuilderTest`（排序、缺 factory、重复 processor、重复 context-name/mdc-name/outbound-header）
- starter：`TraceContextAutoConfigurationTest`（默认禁用、启用生成、默认 systemCode 取 application.name）、`TraceContextServletFilterTest`（读 header、缺失生成、MDC 清理）、`TraceContextRestClientTest`、`TraceContextTaskDecoratorTest`、`TraceSpanAspectTest`
- web：`MdcContextLifterTraceContextTest`（信号级 restore + MDC 清理）、`GlobalErrorAttributesTraceContextTest`、`AbstractExceptionHandlerTraceContextTest`
- gateway：`RequestIdGlobalFilterTraceContextTest`（snapshot 写入、header 保留、缺失生成）
- feign：`FeignClientAutoConfigurationTest`

### 测试缺口（建议补齐）

| 编号 | 缺口 | 关联问题 |
| --- | --- | --- |
| T1 | 自动装配发现测试：用 `@SpringBootTest` 或 `AutoConfigurationReport` 验证 starter 经类路径被自动发现，而非 `AutoConfigurations.of(...)` 显式注册 | B1 |
| T2 | `@TraceSpanGroup` 前缀拼接与前导 `.` 转义 | m3 |
| T3 | `SystemCodeProvider` bean 优先于 `spring.application.name`（当前只测了 fallback 路径） | s3 |
| T4 | `fist.trace-context.enabled=false` 时 runtime/filter/interceptor/aspect 全部不创建，且 Feign、异常事件、MdcContextLifter 全部回退 | 破坏性边界 |
| T5 | 自定义 `TaskDecorator` 与 trace decorator 共存（trace 上下文不丢失） | M4 |
| T6 | 自定义 `correlation` item 改 `context-name` 后，`AbstractExceptionHandler` / `GlobalErrorAttributes` 的行为 | M2 |
| T7 | 跨模块链路：servlet 入站 → Feign 出站透传、gateway 入站 → 下游 servlet 入站 | 端到端 |
| T8 | reactive 入站（非 gateway）当前行为佐证 M1 边界声明 | M1 |

### examples 评估

- `example-trace-context-basic` / `example-trace-context-extension` 作为接入演示足够精简，分别覆盖默认能力与自定义 item，无多余内容
- 缺口：两个示例都是 servlet 场景，无 reactive 端到端示例（第 3 轮已提，首阶段可接受，但与 M1 叠加建议补一个 gateway 最小验证或至少文档说明）

### 文档评估

- `CHANGELOG.md` 的迁移说明条目齐全（`HeaderMdcFilter` 不再默认注册、`TraceInfoResolver` deprecated、Feign handler 替换、gateway snapshot 协议），位置正确
- starter README 只讲 starter 自身用法，符合要求；但有两处需补：reactive 入站边界（M1）、span 的 AOP 依赖与可关闭（M3）
- 建议在 CHANGELOG 的 `Changed` 条目里把 B1 修复（补 `AutoConfiguration.imports`）作为装配正确性修复一并记录

---

## 8. 建议的后续任务清单（按优先级）

1. **[Blocking] 补 `AutoConfiguration.imports` 并加自动发现测试**（B1 + T1）—— 合入前必须完成
2. **[Major] 明确 reactive 入站边界或补 reactive `WebFilter`**（M1）—— 至少补 README/CHANGELOG 边界声明；若选补 filter 则含 T8
3. **[Major] 调整 `TraceContextTaskDecorator` 注册策略**（M4 + T5）—— 改为不与用户 decorator 互斥，补共存测试
4. **[Major] span AOP 依赖与 `@EnableAspectJAutoProxy` 可选化**（M3）—— 依赖改 optional、去掉类级 `@EnableAspectJAutoProxy`、aspect bean 加条件
5. **[Major] 消费方硬编码 context-name 加注释/文档**（M2 + T6）—— 短期补注释与边界测试
6. **[ponytail] 移除 `TraceContextBeanLocator`**（P1）—— 删 3 个类，factory 改直接注入
7. **[ponytail] 合并 `Slf4jTraceMdcContext` 到 core**（P3 + m1）—— 消除重复、解决 m1
8. **[Minor] 显式声明 servlet filter 顺序**（m2）
9. **[Minor] `fist-gateway-auth-core` 显式依赖 `fist-trace-context`**（m4）
10. **[Minor] `@TraceSpan` 转义约定文档与测试**（m3 + T2）
11. **[Minor] gateway 同步段语义注释**（m5）、systemCode 冻结语义注释（m6）
12. **[Suggestion] runtime span 入口统一**（s1）、补 `SystemCodeProvider` 优先级测试（T3）、`enabled=false` 全局回退测试（T4）

建议 1-5 在合入前处理（1 是硬性阻断，2-5 是使用方会直接感知的功能/兼容问题），6-7 作为简化项同批做掉（成本低、收益清晰），8-12 可作为合入后的紧随任务。
