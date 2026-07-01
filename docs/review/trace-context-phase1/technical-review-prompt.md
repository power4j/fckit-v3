# fist-trace-context 首阶段方案技术评审提示词

## 使用方式

将以下提示词交给技术评审人员或独立评审代理。评审时不要假设当前方案合理，也不要只做文字润色。评审目标是发现设计缺陷、范围失控、抽象不稳、实现风险和验收缺口。

评审输入文档：

- `fist-kit3-jasypt/docs/plans/2026-07-01-trace-context-phase1.md`
- `fist-kit3-jasypt/docs/review/trace-context-phase1/01-technical-review.md`
- `fist-kit3-jasypt/docs/review/trace-context-phase1/02-technical-review.md`

说明：`fist-kit3` 是项目名称，`fist-kit3-jasypt` 是当前落地目录，即该项目的 git worktree。

评审输出目录：

- `fist-kit3-jasypt/docs/review/trace-context-phase1/`

建议每轮输出文件命名：

- 第 1 轮：`01-technical-review.md`
- 第 2 轮：`02-technical-review.md`
- 第 3 轮：`03-technical-review.md`

每轮评审后，应根据问题修改方案文档，再进入下一轮评审。若同一类重大问题连续 3 轮无法解决，应停止迭代并提交人工决策。

## 评审提示词

```text
你是一个资深 Java 基础设施和 Spring Boot starter 技术评审专家。请对以下方案做严格技术评审：

- 方案文档：fist-kit3-jasypt/docs/plans/2026-07-01-trace-context-phase1.md
- 项目范围：首阶段只涉及 fist-kit3 项目，当前落地目录为 fist-kit3-jasypt；不改造 pac-cloud、pac-pms、pac-ssx10a 等应用项目。
- 预期交付：fist-trace-context 核心库、fist-trace-context-spring-boot-starter、starter README、一个默认能力示例、一个自定义 item 扩展示例。

重要要求：

1. 不要假设当前设计合理。
2. 不要只做文档语句优化。
3. 优先发现会导致实现失败、后续难以扩展、职责边界混乱、破坏性更新、测试不可验证的问题。
4. 对每个问题给出严重级别、影响、证据、建议修改方式。
5. 如果认为某个抽象没有必要，应直接指出并给出更简单替代方案。
6. 如果认为某个抽象不足以支撑后续定制，也应直接指出并给出增强方向。
7. 如果方案在验收标准、示例、README、测试策略上存在缺口，应明确列出。
8. 如果发现已有模块可能重复承担同类职责，应指出如何迁移到 fist-trace-context 核心代码，避免最终实现代码存在多套。

请重点评审以下方面：

一、模块边界

- fist-trace-context 和 fist-trace-context-spring-boot-starter 的边界是否清楚。
- 是否和 fist-logback、fist-sde、fist-support-spring、fist-boot-web-app 等现有模块职责重复。
- 是否存在应该复用现有模块但方案未说明的内容。
- 是否存在应该迁移现有代码但方案未说明的内容。
- 「重复能力治理」是否充分覆盖现有请求 ID、MDC、Feign 透传、TraceInfo 等能力的职责重划、功能整合和新依赖关系。
- 原有模块最终使用 fist-trace-context 核心代码是否可落地，是否仍残留多套请求 ID / MDC / header 处理实现。
- starter 作为接入层、核心库作为复用层的边界是否清楚，是否存在 Feign、Web、reactive 模块反向依赖 starter 的风险。
- `fist.trace-context` 配置是否由 starter 统一读取、解析和校验，其他模块是否只消费 TraceContextRuntime / TraceContextRegistry。
- 是否存在其他模块重复读取配置、重复解释 item props、重复编排 item 的风险。
- 第 2 轮指出的 reactive 写入侧与读取侧协议是否闭环，`RequestIdGlobalFilter`、`MdcContextLifter`、`GlobalErrorAttributes` 是否都纳入治理。
- Feign 治理是否保留现有 RelayInterceptor，仅用 TraceRelayHandler 取代 HeaderRelayHandler；trace header 与认证 header 是否能在同一 handler 链中共存。
- 第 2 轮指出的纯 Java 核心库与 Spring Bean 查找矛盾是否通过核心抽象解决。
- 第 2 轮指出的 `enabled=false` 装配契约是否明确且可测试。
- 新模块的配置能力是否足以支撑向前兼容，而不是形成另一套旁路 trace 能力。
- 首阶段只涉及 fist-kit3 项目是否被严格遵守，是否误改应用项目。

二、核心抽象

- TraceContext、TraceContextItem、TraceContextItemFactory、TraceContextItemSpec、TraceScope 等概念是否足够稳定。
- 「追踪上下文项」这个概念是否比「字段」更合适，是否仍有命名或边界问题。
- item ID 与 processor 的区分是否必要，是否存在过度设计。
- props 私有配置模型是否合理，是否会导致配置不可校验、错误难发现、文档难写。
- 框架不理解 item 业务含义，只做生命周期编排，这个边界是否可实现、可测试、可维护。

三、默认能力

- 默认 item 只包含 correlation-id、system-code，span 作为内建栈能力，是否合理。
- 是否有默认能力过少导致「开箱即用」不足的问题。
- 是否有默认能力过多导致业务规范被固化的问题。
- system-code 是否应作为默认 item，还是应由应用显式启用。
- correlation-id 的语义是否清楚，是否应兼容 W3C traceparent 或暂不处理。

四、span 设计

- pushSpan、withSpan、TraceScope 是否足以支持嵌套和异常场景。
- 是否需要暴露 pop，或禁止裸 pop 是否合理。
- @TraceSpan 和 @TraceSpanGroup 是否应进入首阶段。
- 注解方式是否会引入 AOP 依赖、代理限制、异步边界问题。
- span-id 作为 item 与 span 栈管理之间是否存在模型冲突。

五、配置机制

- 配置只保留 enabled、processor、order、props 是否合理。
- 配置所有权是否清楚：starter 负责配置绑定，核心库提供运行时模型，其他模块只消费运行时组件。
- order 是否应出现在 YAML 中，还是只作为代码注册排序。
- props 全部交给 item 解释是否会影响自动配置元数据、IDE 提示和配置错误排查。
- 默认 item 的 props 是否需要文档化最小集合。
- 是否需要为 item validate 失败定义启动失败策略。

六、注册机制

- Java SPI 与 Spring Bean 注册同时支持是否合理。
- Factory 根据 processor 创建 item 的方式是否能支持多实例。
- item 创建时是否应注入外部依赖，例如 ID 生成器、系统代码提供器、时钟、配置源。
- SPI 和 Spring Bean 同时存在时优先级如何处理。
- 是否需要 factory processor 重复检测和冲突处理。

七、MDC 和日志集成

- 把 MDC 适配放在核心库是否合理，是否应拆出 slf4j adapter。
- 与 fist-logback 的关系是否足够清晰。
- 是否需要默认 logback pattern 或只在 README 中给示例。
- MDC 清理和线程复用污染是否有明确方案。

八、HTTP 和异步传递

- Servlet Filter 是否足够作为首阶段入口能力。
- HeaderMdcFilter 删除并迁移到 starter 是否可行，破坏性是否被充分说明。
- Feign 模块通过 TraceRelayHandler 取代 HeaderRelayHandler 是否可行，是否会造成循环依赖、重复 RequestInterceptor 或 header 重复写入。
- RestClient、Feign、reactive MDC 的边界是否清楚。
- WebFlux / Gateway 只改造 reactive MDC 恢复、不提供完整入口追踪上下文能力，这个范围是否自洽。
- 是否需要支持 RestTemplate、WebClient、消息队列，还是明确不做。
- 异步上下文传递是否应首阶段实现，还是放后续。
- ThreadLocal、快照、TaskDecorator 的边界是否清楚。

九、破坏性和兼容性

- 删除 HeaderMdcFilter、TraceInfoResolver 等旧能力的破坏性是否可接受。
- 引入 starter 后的默认启用策略是否安全。
- 是否可能改变请求头、日志内容、线程上下文、AOP 行为。
- 是否需要默认 disabled，要求应用显式开启。
- 是否有从旧 Web 默认能力迁移到 starter 的明确路径。
- 原自定义 TraceInfoResolver、旧 Feign 透传、reactive MDC 的迁移说明是否完整，是否放在原模块 README / changelog 而不是 starter README。
- 是否需要保留 deprecated 过渡期，而不是直接删除。

十、测试和验收

- 验收标准是否可执行、可验证。
- 单元测试和 starter 集成测试覆盖是否足够。
- README 是否规定得足够简洁。
- 两个示例是否足以证明默认能力和扩展能力。
- 是否缺少错误配置、嵌套 span、异常退出、MDC 清理、并发隔离等关键测试。

输出格式：

1. 结论
   - 给出「通过」「有条件通过」「不通过」之一。
   - 用 2-4 句话说明总体判断。

2. 重大问题
   - 按严重级别排序。
   - 每个问题包含：
     - 编号
     - 严重级别：阻断 / 高 / 中 / 低
     - 位置或相关章节
     - 问题描述
     - 影响
     - 建议修改

3. 次要问题
   - 列出不阻断但应优化的问题。

4. 开放问题
   - 列出需要人工决策的问题。

5. 建议修改清单
   - 给出可直接修改方案文档的条目。

6. 下一轮评审建议
   - 说明下一轮应重点复核哪些内容。

请将评审结果写入本轮对应文件：

- 第 1 轮：`fist-kit3-jasypt/docs/review/trace-context-phase1/01-technical-review.md`
- 第 2 轮：`fist-kit3-jasypt/docs/review/trace-context-phase1/02-technical-review.md`
- 第 3 轮：`fist-kit3-jasypt/docs/review/trace-context-phase1/03-technical-review.md`
```

## 多轮优化流程

1. 使用上述提示词生成第 1 轮评审。
2. 根据「重大问题」和「建议修改清单」更新方案文档。
3. 使用同一提示词进行第 2 轮评审，输出 `02-technical-review.md`。
4. 若仍有阻断或高严重级别问题，继续修改并进入第 3 轮。
5. 第 3 轮后仍无法解决的阻断问题，应明确记录为人工决策项，不继续自动迭代。
