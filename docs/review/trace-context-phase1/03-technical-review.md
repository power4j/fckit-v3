# fist-trace-context 首阶段方案技术评审（第 3 轮）

- 评审对象：`docs/plans/2026-07-01-trace-context-phase1.md`（再次修订版）
- 历史评审：`01-technical-review.md`、`02-technical-review.md`
- 评审重点：第 2 轮阻断问题闭环检查、reactive 三件套协议一致性、Feign handler 链、BeanLocator、enabled 契约、deprecated 过渡、默认能力边界、验收覆盖度
- 评审日期：2026-07-01

---

## 1. 结论

**有条件通过。**

第 2 轮的四个阻断问题（B6 reactive 协议、B7 Feign 组合、B8 BeanLocator、B9 enabled 契约）和四个高优先级问题（H1-H4）全部真正解决，不是文字说明。方案在 reactive 读写闭环、Feign handler 链设计、核心库 Bean 查找抽象、配置开关契约、多实例冲突检测、span 默认边界、inbound 冻结语义这些难点上都给出了可落地的契约。三轮评审下来，阻断点从「方向不清」收敛到「无阻断」，设计已经具备进入实现的条件。

剩余问题集中在两类：gateway 转发透传的语义未明确、reactive 入口调用链的实现路径未画清。这两类属于「补齐明确性」而非「设计返工」，修复成本可控。因此本轮不再判不通过，改为有条件通过，条件是补齐下文 M1、M2 两个中级问题。

---

## 2. 第 2 轮阻断问题闭环检查

本轮核心任务是验证第 2 轮阻断是否真正解决，逐项核对如下。

| 第 2 轮编号 | 阻断点 | 方案回应（行号） | 本轮判定 |
| --- | --- | --- | --- |
| B6 | reactive MDC 读写协议断裂 | 治理表纳入 `RequestIdGlobalFilter`（写侧，方案 86、153 行）、`MdcContextLifter`（读侧，方案 87、155 行）、`GlobalErrorAttributes`（方案 88、158 行），统一 Reactor Context 载荷（方案 152 行），「首阶段不做」同步更新（方案 855 行） | 闭环。三方协议统一为 `TraceContextSnapshot` 载荷 |
| B7 | Feign 组合机制与装配顺序 | 保留 `RelayInterceptor` 与 `RelayHandler` 链，新增 `TraceRelayHandler` 取代 `HeaderRelayHandler`（方案 59、130-144 行），handler 顺序固定（方案 141 行），`@AutoConfigureAfter` 保证装配顺序（方案 142 行） | 解决。trace header 与认证 header 在同一 handler 链共存 |
| B8 | 核心库纯 Java 与 Bean 查找矛盾 | 定义 `TraceContextBeanLocator`（方案 570-584 行），核心库定义接口、starter 提供 `ApplicationContext` 实现，`SystemCodeProvider` 经由它查找（方案 586-602 行） | 解决。核心库不出现 Spring 类型 |
| B9 | enabled=false 装配契约 | 方案 108、677、686、730、771 行一致规定 `enabled=false` 不装配 `TraceContextRuntime` / `TraceContextRegistry`，Feign 检测无 bean 时不注册 `TraceRelayHandler`（方案 137 行），验收覆盖（方案 789 行） | 解决 |

第 2 轮四个高级问题同样逐项确认已解决：

| 第 2 轮编号 | 问题 | 回应 | 判定 |
| --- | --- | --- | --- |
| H1 | 多 correlation-id 实例 context-name 冲突 | 方案 298-303 行冲突规则、223-226 行示例显式 name、682-683 行编排检测、764 行测试 | 解决 |
| H2 | span outbound header 与 MDC 组合规则 | 方案 382-383 行默认不写 span header、643 行 syncMdc 语义明确 | 解决 |
| H3 | OutboundTraceContext.getValue 数据源 | 方案 642、703-705 行明确读当前不可变 TraceContext，无 context 时整体跳过 | 解决 |
| H4 | InboundTraceContext 冻结语义 | 方案 693-695 行明确可变构建器→冻结→三者同源 | 解决 |

第 2 轮四个中级问题（M1 `GlobalErrorAttributes` 纳入治理、M2 SPI 覆盖规则、M3 deprecated 过渡、M4 `SystemCodeProvider` 契约）也全部解决，证据见方案 88/158、654-655、76/117/168/808-812、586-602 行。

**第 2 轮全部 12 个问题真正闭环，无残留。**

---

## 3. 重大问题

### M1 `RequestIdGlobalFilter` 改造后 gateway 转发透传语义未明确

- 严重级别：中（接近高）
- 位置：方案「MdcContextLifter」150-159 行、「首阶段不做」855 行；现有 `fist-cloud-gateway/fist-gateway-auth-core/.../RequestIdGlobalFilter.java:42-54`

问题描述：

现有 `RequestIdGlobalFilter` 实际承担两个职责（`RequestIdGlobalFilter.java:44-53`）：

1. gateway 转发透传：读 `X-REQ-UID`，缺失则生成，并通过 `request.mutate().header(headerKey, requestId)` 写回下游 request，使 gateway 转发到下游服务时 header 携带关联 ID。
2. reactive MDC 注入：通过 `contextWrite` 向 Reactor Context 写入 requestId 字符串，供 `MdcContextLifter` 恢复 MDC。

方案 153 行描述改造为「在 reactive 入口构造或获取当前 `TraceContext`，并向 Reactor Context 写入统一载荷」，只覆盖了职责 2 的协议变更。方案 159 行又声明「网关透传策略不纳入首阶段」。这两处合起来，读不出职责 1（向下游 request 写 `X-REQ-UID`）在改造后是保留还是删除。

影响：

- 若实现者把「网关透传策略不纳入首阶段」理解为「删除 mutate header 逻辑」，gateway 转发到下游的请求将丢失 `X-REQ-UID`，整个 gateway 链路的跨服务关联断裂，且只在端到端联调时才暴露。
- 若保留 mutate header，则需明确：写入的值来自 correlation-id item 的入口采集结果，header 名仍为 `X-REQ-UID`。
- 这是 gateway 作为跨服务关键节点的核心能力，语义不能留白。

建议修改：

方案 150-159 行补充一句明确表态：`RequestIdGlobalFilter` 改造后仍保留向下游 request header 写回 `X-REQ-UID` 的行为（值来源于入口采集），只是 Reactor Context 的写入内容由 requestId 字符串变更为 `TraceContextSnapshot` 统一载荷。同时澄清方案 159 行「网关透传策略不纳入首阶段」特指「不做 gateway 的响应写回、采样、动态路由等高级透传策略」，不包括「现有 `X-REQ-UID` 转发透传」。

---

### M2 reactive 入口的 inbound 调用链与 gateway 模块依赖未明确

- 严重级别：中
- 位置：方案「MdcContextLifter」153 行、「依赖关系调整」91-99 行

问题描述：

方案 153 行要求 `RequestIdGlobalFilter`「在 reactive 入口构造或获取当前 `TraceContext`」。要让这句话落地，需要三件事，方案都未明确：

1. `RequestIdGlobalFilter` 所在的 `fist-gateway-auth-core` 如何获得 `TraceContextRuntime` bean。方案 91-99 行的依赖关系调整列了 `fist-support-web`、`fist-boot-web-app`、`fist-cloud-rpc-feign`，唯独没列 gateway 模块对 `fist-trace-context` 核心库的依赖。
2. reactive 入口的 `InboundTraceContext` 实现由谁提供。`InboundTraceContext.readHeader` 要读 reactive 的 `ServerHttpRequest` header，需要一个基于 `ServerHttpRequest` 的适配实现，方案没说这个适配类归属（核心库无法提供，因为它不依赖 Spring Web）。
3. inbound 产生的 `TraceContext` 在 reactive 场景不能进 ThreadLocal（方案 157 行），必须立即 `capture` 成 snapshot 写入 Reactor Context。这条「inbound → capture → contextWrite」的调用链方案没画出来。

影响：

- 三个未明确的点叠加，实现者需要自行设计 reactive 入口的 inbound 路径，容易与 Servlet 入口的实现产生分歧（例如 snapshot 是否包含根 span、capture 时机在 inbound 之内还是之后）。
- 方案 159 行把 reactive MDC 恢复作为首阶段交付项，但入口 inbound 路径不清，这项交付无法稳定实现。

建议修改：

方案 150-159 行补充 reactive 入口的调用链：

- `RequestIdGlobalFilter` 依赖 `fist-trace-context` 核心库，通过 `@Autowired(required = false)` 获取 `TraceContextRuntime`（与 Feign 模块同样的可选依赖模式）。
- `fist-support-web` 提供一个基于 `ServerHttpRequest` 的 `InboundTraceContext` 适配实现（已声明依赖核心库，方案 95 行）。
- `RequestIdGlobalFilter` 调用 `runtime.inbound(...)` 后立即 `runtime.capture()` 得到 snapshot，写入 Reactor Context 统一载荷。
- 无 `TraceContextRuntime` bean 时（未启用 starter），`RequestIdGlobalFilter` 回退到现有「字符串 requestId」协议或最小行为，保证 gateway 不报错。

依赖关系调整章节（方案 91-99 行）补充 `fist-gateway-auth-core` / `fist-cloud-gateway` 对 `fist-trace-context` 的可选依赖声明。

---

### M3 破坏性评估对 Feign 透传丢失场景的描述不够直白

- 严重级别：中
- 位置：方案「破坏性评估」735 行、「迁移要求」744 行、「RelayInterceptor」134-138 行

问题描述：

按方案逻辑，`HeaderRelayHandler` 被删除（方案 134 行），`TraceRelayHandler` 仅在存在 `TraceContextRuntime` bean 时注册（方案 137 行）。因此未启用 starter 的应用，`RelayInterceptor` 的 handler 链里既无 `HeaderRelayHandler` 也无 `TraceRelayHandler`，Feign 请求不再写 `X-REQ-UID`。

迁移要求（方案 744 行）确实提到了「需要引入并启用 starter」，但破坏性评估的行为变化清单（方案 735 行）只写「未启用追踪上下文时不注册 `TraceRelayHandler`」，没有直说「未启用 starter 的应用 Feign 不再透传 `X-REQ-UID`，跨服务关联断裂」。

影响：

- 行为变化清单是 README 和 changelog 的依据。措辞不直白，下游应用维护者容易误判升级影响，以为只是「少注册一个 handler」，实际是「跨服务关联 ID 丢失」。
- 与 `HeaderMdcFilter` 的破坏性描述（方案 726-729 行明确说「不再默认注册」）相比，Feign 侧的表述偏弱，不一致。

建议修改：

破坏性评估的行为变化清单增加一条：未启用 starter 的应用，Feign 下游调用不再透传 `X-REQ-UID`，需引入并启用 starter 由默认 `correlation-id` item 恢复等价能力。与 `HeaderMdcFilter` 的破坏性描述保持同一表述强度。

---

### M4 MDC 清理契约未覆盖 `MdcContextLifter` 的 scope 关闭

- 严重级别：中
- 位置：方案「MDC 清理契约」711-718 行；现有 `MdcContextLifter.java:66-75`

问题描述：

`MdcContextLifter` 现有用 `MDC.putCloseable` 的 try-with-resources 在每次信号回调后清理 MDC（`MdcContextLifter.java:66-75`）。改造后它调用 `TraceContextRuntime.restore` 和 `syncMdc`（方案 640 行），`restore` 返回 `TraceScope`，需要 `close` 来清理。方案 711 行声明「所有写入 MDC 的路径必须有对应的清理」，但清理契约清单（方案 715-718 行）只列了 Servlet Filter、`TraceScope.close()`、`TaskDecorator`，没有列 `MdcContextLifter`。

影响：

- 实现者不清楚 `MdcContextLifter` 在每次信号回调后是否要 `close` 本次 `restore` 产生的 `TraceScope`。若不 close，reactive 线程池复用时 MDC 残留；若每次信号都 restore + close，需明确这个生命周期。
- 这是 reactive MDC 正确性的关键，与方案 640 行声明的「`MdcContextLifter` 调用 restore 和 syncMdc」直接相关，不能遗漏。

建议修改：

MDC 清理契约清单补充 `MdcContextLifter`：每次信号回调内 restore + syncMdc 写入的 MDC，必须在同一信号回调内 close 对应 `TraceScope` 完成 cleanup，不跨信号持有 scope。验收标准补充「reactive MDC 在信号切换后无残留」的测试。

---

## 4. 次要问题

- 方案 298-303 行的多实例冲突规则检测了 `context-name` 和 `mdc-name`，未检测 `outbound-header` 冲突。两个 item 写同一 outbound header 会静默覆盖。建议补检，或声明允许覆盖。
- reactive 场景下 `TraceContextHolder`（ThreadLocal 模式）不可用（方案 157 行），方案未明确「reactive 业务代码不提供 `TraceContextHolder` 语义，只提供 MDC 恢复与 Reactor Context 载荷读取」。建议在 span API 或异步章节说明这一点，避免业务误用。
- 配置元数据（方案 768 行）对通用字段（`enabled` / `processor` / `order`）有效，但 `props` 是 `Map<String, String>`，IDE 无法提示 `correlation-id` 的 `inbound-header` 等 key。这是第 1 轮 B5 的固有约束，建议方案注明「`props` 下的 key 不提供 IDE 提示，校验依赖 factory fail-fast」，避免实现者误以为能生成完整元数据。
- `TraceContextConfigurationException`（方案 568 行）归属模块仍未定义（第 2 轮已提）。建议明确为核心库异常、unchecked。
- `RequestIdGlobalFilter` 由 `RouteGuardConfiguration` 装配（`fist-cloud-gateway-acl`），方案治理表只提 filter 类本身，未提装配方。改造 filter 类时装配方通常不用动，但建议在治理表注一句，避免遗漏。
- 两个示例都是 Servlet 场景（方案 818-843 行），reactive 三件套改造无端到端示例。验收靠单元与集成测试可以接受，但若 reactive 是 gateway 项目的主路径，建议补一个最小 reactive 验证示例。

---

## 5. 开放问题

- O1 未启用 starter 时 Feign 是否应保留 `HeaderRelayHandler` 作为回退，还是接受「未启用则不透传」的破坏。前者无回归但与「用 `TraceRelayHandler` 取代」表述冲突；后者一致但影响面大。方案当前选后者，迁移要求已覆盖，但需确认这是有意识取舍而非疏漏。
- O2 gateway 转发透传在首阶段的精确范围（见 M1）。「网关透传策略不纳入首阶段」与「保留现有 `X-REQ-UID` 转发」需要人工确认边界。
- O3 reactive 入口的 `InboundTraceContext` 适配实现，是放在 `fist-support-web`（与 `MdcContextLifter` 同模块）还是 `fist-gateway-auth-core`（与 `RequestIdGlobalFilter` 同模块）。前者复用性更好，后者内聚性更好。

---

## 6. 建议修改清单

以下条目可直接用于修订方案文档：

1. **M1**：「MdcContextLifter」章节明确 `RequestIdGlobalFilter` 改造后仍保留向下游 request 写回 `X-REQ-UID` 的行为，并澄清方案 159 行「网关透传策略不纳入首阶段」的精确范围。
2. **M2**：「MdcContextLifter」章节补充 reactive 入口调用链（依赖 `TraceContextRuntime`、`InboundTraceContext` 适配实现归属、inbound→capture→contextWrite 流程、无 runtime 时的回退），依赖关系调整章节补充 gateway 模块对核心库的可选依赖。
3. **M3**：破坏性评估行为变化清单增加「未启用 starter 的应用 Feign 不再透传 `X-REQ-UID`」一条，表述强度与 `HeaderMdcFilter` 对齐。
4. **M4**：MDC 清理契约清单补充 `MdcContextLifter` 的 scope 关闭要求，验收标准补充 reactive MDC 无残留测试。
5. 次要问题：补 `outbound-header` 冲突检测或声明允许覆盖；注明 reactive 不提供 `TraceContextHolder` 语义；注明 `props` 无 IDE 元数据；明确 `TraceContextConfigurationException` 归属。

---

## 7. 下一轮评审建议

方案修订后，若 M1-M4 补齐，下一轮建议聚焦实现验证而非文档评审：

1. reactive 三件套（`RequestIdGlobalFilter` / `MdcContextLifter` / `GlobalErrorAttributes`）的端到端一致性，最好有最小 PoC 验证 snapshot 能完整通过 Reactor Context 传递，gateway 转发仍携带 `X-REQ-UID`。
2. Feign handler 链顺序验证：启用 starter 后一次 Feign 请求同时携带 trace header 与认证 header，且 `TraceRelayHandler` 先于认证 handler；异步线程发起 Feign 调用时 trace header 来源是 `TraceContext`。
3. deprecated 过渡的实际编译兼容性：在保留 `@Deprecated` 空壳后，验证下游引用 `HeaderMdcFilter` / `TraceInfoResolver` 的旧代码仍能编译，仅得到 deprecation 警告。
4. `enabled=false` 的全局回退：验证 Feign、异常事件、MDC、reactive 全部回退到旧行为或不提供能力，无 `TraceContextRuntime` bean，无半套运行时。
5. 多 correlation-id 实例：验证冲突检测在启动期 fail-fast，正常多实例（显式不同 name）能各自写入与透传。
6. 若进入实现阶段，建议优先做 reactive 入口调用链（M2）和 gateway 转发透传（M1）的 PoC，这两点是本轮唯一可能导致返工的实现路径风险。
