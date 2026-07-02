# fist-trace-context 首阶段第 5 轮技术评审提示词

请对 `fist-kit3` 当前分支 `fist-kit3-jasypt` 的 `fist-trace-context` 首阶段实现进行第 5 轮技术评审，并将评审报告写入：

`docs/review/trace-context-phase1/05-technical-review.md`

## 评审原则

不要假设当前设计是合理的。请从通用代码基线、长期维护、兼容治理、使用方迁移成本和生产可用性角度审查实现。

本轮不是只看代码是否能编译，而是判断首阶段是否可以收口。请明确给出结论：可以收口、需要少量修复后收口、仍有阻断问题，三选一。

## 项目与工作区

- 项目名称：`fist-kit3`
- 当前目录：`fist-kit3-jasypt`，它是 `fist-kit3` 的 git worktree
- 当前分支：`fist-kit3-jasypt`
- 当前 HEAD：`d649900d775118bc7dd426c96d1c8d6ae87fd3c6`

## 评审输入

优先阅读以下文档：

1. `docs/plans/2026-07-01-trace-context-phase1.md`
2. `docs/tasks/2026-07-01-trace-context-phase1-tasks.md`
3. `docs/review/trace-context-phase1/01-technical-review.md`
4. `docs/review/trace-context-phase1/02-technical-review.md`
5. `docs/review/trace-context-phase1/03-technical-review.md`
6. `docs/review/trace-context-phase1/04-technical-review.md`

## Git 范围

完整首阶段实现范围：

```bash
git diff --stat 131e84b33d9baa905ed22b6320d4be8bb85b6210..HEAD
git diff 131e84b33d9baa905ed22b6320d4be8bb85b6210..HEAD
```

第 4 轮评审后的整改范围：

```bash
git diff --stat 46892ca6545ee1ce8f16a8aa333d5a7011d4ffa9..HEAD
git diff 46892ca6545ee1ce8f16a8aa333d5a7011d4ffa9..HEAD
```

第 4 轮评审后的整改提交：

```text
d649900d feat: wire reactive trace context correlation
b0b1ce33 refactor: share slf4j trace mdc context
e44558e7 refactor: remove trace context bean locator
84afc677 feat: allow disabling trace span aspect
6bed3c1f fix: compose trace task decorator
9f6c5c33 chore: remove legacy auto configuration factories
b0003455 fix: register trace context auto configuration
```

## 本轮重点

请重点复核第 4 轮评审发现的问题是否真正闭环：

1. Spring Boot 3 自动配置发现是否正确，`AutoConfiguration.imports` 和相关测试是否足够。
2. 普通 WebFlux 入站能力是否完整，`TraceContextWebFilter`、`ReactiveTraceContext`、`MdcContextLifter`、`GlobalErrorAttributes` 是否形成一致协议。
3. 异常事件和错误响应中的 `requestId` 是否已经摆脱对默认 `context-name=requestId` 的硬编码耦合，且兼容旧行为。
4. `TraceContextTaskDecorator` 是否能与用户自定义 `TaskDecorator` 共存，是否存在执行顺序、重复装饰或上下文泄漏风险。
5. span AOP 开关是否合理，`fist.trace-context.span.enabled=false` 时是否不会创建 aspect，也不会主动启用 AOP。
6. `TraceContextBeanLocator` 删除后，扩展方实现自定义 processor 的能力是否受损。
7. `Slf4jTraceMdcContext` 合并到 core 后，模块依赖边界是否合理，是否引入不必要的耦合。
8. 删除历史 `spring.factories` 是否会影响 Spring Boot 2 兼容；如果这是有意破坏，需要检查文档和 changelog 是否说明清楚。
9. Feign、Servlet、Gateway、Reactive、异常事件是否都只复用 `fist-trace-context` 核心实现，是否还残留第二套默认 requestId/header/MDC 实现。
10. 配置所有权是否统一：除 starter 外，其他模块是否没有重新读取或解释 `fist.trace-context` 配置。

## 架构检查点

请额外检查以下设计点：

- `TraceContextItem` / `processor` / item ID 的概念是否在代码、README、示例中一致。
- 默认能力是否只包含通用基线需要的内容，而不是混入项目专属字段。
- 新增追踪上下文项的实现路径是否清晰：实现 factory、实现 item、注册 processor、配置 item。
- `correlation-id` 默认 processor 是否能承担「链路关联 ID」语义；如果命名、接口或扩展点仍有歧义，请指出。
- `CorrelationTraceContextItem` / `TraceCorrelation` 是否是合适的语义抽象，是否过早抽象或不足以支撑消费方。
- `spanId` 的 push / with / 注解语义是否一致，MDC 同步和清理是否可靠。
- 破坏性更新是否集中在迁移治理目标内，是否存在没有文档说明的破坏。
- starter README 是否足够简洁，旧能力迁移说明是否放在原模块 README 或 changelog，而不是塞进 starter README。
- examples 是否能作为用户侧示例：一个基本示例只演示默认能力，一个扩展示例演示自定义 item。

## 测试与验证

请检查现有测试是否覆盖真实风险，而不是只覆盖 happy path。尤其关注：

- 自动配置是否通过类路径发现，而不是只通过 `AutoConfigurations.of(...)` 显式注入。
- 自定义 `correlation` item 改 `context-name` 后，异常事件和 reactive error attributes 是否仍能输出稳定的 `requestId`。
- WebFlux filter 是否写入 exchange attribute 和 Reactor Context，且请求结束后不残留 ThreadLocal。
- reactive 每个信号是否正确 restore/close scope，MDC 是否无残留。
- `TaskDecorator` 共存测试是否证明用户装饰逻辑和 trace 恢复都生效。
- `enabled=false` 是否覆盖 starter、Feign、Web、reactive、异常事件等协作边界。
- 删除 `spring.factories` 后，受影响 starter 是否有替代的 Spring Boot 3 自动配置发现文件和测试。

已执行过的关键验证命令包括：

```bash
cmd /c mvn -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test
cmd /c mvn -pl fist-kit-app/fist-web/fist-boot-web-app,fist-kit-app/fist-web/fist-support-web -am "-Dtest=FistWebAutoConfigurationTest,TraceContextWebFilterTest,MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest,AbstractExceptionHandlerTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

请不要只复述这些命令通过。需要判断测试集合是否足以证明设计和迁移目标。

## 输出格式

请按以下结构输出：

```markdown
# fist-trace-context 首阶段实现技术评审（第 5 轮）

## 1. 结论

明确说明是否可以收口。

## 2. Blocking

必须修复的问题。没有则写「无」。

## 3. Major

合入或收口前建议修复的问题。

## 4. Minor

可后续处理的问题。

## 5. 第 4 轮问题闭环复核

逐项列出第 4 轮 Blocking / Major / ponytail 项的闭环状态：已闭环、部分闭环、未闭环。

## 6. 架构与兼容性评估

重点评价核心库、starter、既有模块治理、配置所有权、破坏性更新说明。

## 7. 测试与验收评估

说明测试充分性、缺口和建议补充的验证命令。

## 8. 建议后续任务

按优先级给出可执行任务，不要只提出方向。
```

每个问题必须包含：

- 文件和行号。
- 问题描述。
- 为什么重要。
- 建议修复方式。
- 严重级别。

请避免泛泛评价。若认为某项设计可以保留，也请说明保留理由和边界。
