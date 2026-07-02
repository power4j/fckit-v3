# fist-trace-context 首阶段任务跟踪

来源方案：[2026-07-01-trace-context-phase1.md](../plans/2026-07-01-trace-context-phase1.md)

本文件用于跟踪 `fist-kit3` 内 `fist-trace-context` 首阶段实施。当前工作目录为 `fist-kit3-jasypt`，它是 `fist-kit3` 的 git worktree。

## 状态说明

- `todo`：未开始。
- `doing`：正在实施。
- `done`：代码、测试和必要文档已完成并提交。
- `blocked`：存在外部依赖或设计问题，暂不能继续。

## 实施边界

- 只修改 `fist-kit3` 项目内代码。
- 不修改 `pac-cloud`、`pac-pms`、`pac-ssx10a` 等应用项目。
- `fist-trace-context` 是唯一核心实现；既有 Web、Feign、reactive MDC、异常事件能力最终只能复用它。
- `fist.trace-context` 配置只能由 starter 读取、解析和校验。
- 每个任务完成后优先做小提交，避免跨模块大批量提交。

## 任务总览

| 编号 | 状态 | 任务 | 主要范围 | 验收检查 |
| --- | --- | --- | --- | --- |
| T0 | done | 建立任务跟踪 | `docs/tasks/2026-07-01-trace-context-phase1-tasks.md` | 文档提交，工作区干净 |
| T1 | done | 创建 Maven 模块骨架 | `fist-kit-infra/pom.xml`、`fist-kit-dependencies/pom.xml`、`fist-kit-infra/fist-trace-context`、`fist-kit-infra/fist-trace-context-spring-boot-starter` | 新模块可被 Maven 识别 |
| T2 | done | 实现核心上下文 API | `TraceContext`、`TraceContextHolder`、`TraceContexts`、`TraceScope`、`TraceContextSnapshot` | 单元测试覆盖读写、snapshot、restore、scope 清理、span 嵌套 |
| T3 | done | 实现 item 与 runtime 编排 | `TraceContextItem`、`TraceContextItemFactory`、`TraceContextRegistry`、`TraceContextRuntime`、载体接口、抽象基类 | 单元测试覆盖排序、入口冻结、出口透传、MDC 同步、配置冲突 |
| T4 | done | 实现 starter 配置与默认 processor | `TraceContextProperties`、自动配置、`correlation-id`、`system-code`、配置元数据 | `enabled=false` 不创建 runtime；默认配置可生成 `X-REQ-UID` / `requestId` |
| T5 | done | 实现 Servlet / RestClient / async / span 注解 | starter Filter、RestClient interceptor、TaskDecorator、`@TraceSpan`、`@TraceSpanGroup` | 测试覆盖入口采集、缺失生成、MDC 清理、RestClient 透传、注解 span、异步恢复 |
| T6 | done | 治理 Web 旧能力 | `fist-support-web`、`fist-boot-web-app` | 删除旧默认 `HeaderMdcFilter` 和默认 `TraceInfoResolver` 装配；public 类型 deprecated；异常事件读取 `TraceContext` |
| T7 | done | 治理 Feign 透传 | `fist-cloud-rpc-feign` | 删除 `HeaderRelayHandler`；新增 `TraceRelayHandler`；保留单个 `RelayInterceptor`；认证 header 不受影响 |
| T8 | done | 治理 reactive / gateway | `fist-support-web`、`fist-gateway-auth-core`、`fist-cloud-gateway-acl` | `RequestIdGlobalFilter` 写入 snapshot 并保留 `X-REQ-UID`；`MdcContextLifter` 每个信号 close scope；异常响应读取统一上下文 |
| T9 | done | 补充 README / changelog / 示例 | starter README、原模块迁移说明、`examples/fist-trace-context` | README 简洁；两个示例覆盖默认能力和自定义 item |
| T10 | done | 首阶段集成验证 | 相关模块测试和必要聚合构建 | 关键模块测试通过；破坏性更新说明完整 |
| R4-1 | done | 第 4 轮评审阻断项整改 | `fist-trace-context-spring-boot-starter` | Spring Boot 3 自动配置发现文件存在；自动发现测试通过 |
| R4-2 | done | 第 4 轮低风险整改 | starter README、gateway auth core、Servlet filter | 明确 WebFlux 入站边界；Servlet filter 顺序显式化；gateway 显式依赖 trace core |
| R4-3 | done | 第 4 轮 TaskDecorator 共存整改 | `fist-trace-context-spring-boot-starter` | 用户已有 `TaskDecorator` 时仍恢复 trace 上下文，用户装饰逻辑不丢失 |
| R4-4 | done | 第 4 轮 span 文档补充 | starter README、`@TraceSpan` Javadoc | 明确 `@TraceSpan(".xxx")` 不拼接类级 group |
| R4-5 | done | 第 4 轮 span AOP 开关整改 | `fist-trace-context-spring-boot-starter` | `fist.trace-context.span.enabled=false` 时不创建 span aspect，也不主动启用 AOP |
| R4-6 | todo | 第 4 轮剩余设计项 | WebFlux 入站、异常 requestId 消费、ponytail 简化项 | 单独评估后拆分实施 |

## 建议提交粒度

1. `docs: add trace context task tracking`
2. `feat: add trace context module skeleton`
3. `feat: add trace context core runtime`
4. `feat: add trace context starter defaults`
5. `feat: wire trace context servlet support`
6. `feat: migrate web trace context usage`
7. `feat: migrate feign trace relay`
8. `feat: migrate reactive trace context`
9. `docs: document trace context usage`

## 当前检查命令

```bash
git status --short
git diff --check
```

模块实现后按任务补充更精确的 Maven 验证命令，优先只运行被改模块及其必要依赖。

## 验证记录

- 2026-07-01：T1 模块骨架执行 XML 解析检查通过，`git diff --check` 通过。
- 2026-07-01：T1 Maven 验证命令 `mvnd -pl fist-kit-infra/fist-trace-context,fist-kit-infra/fist-trace-context-spring-boot-starter -am -DskipTests validate` 受限于沙箱网络，无法下载父 BOM；非沙箱执行申请因审批服务 503 未执行。网络可用后需补跑。
- 2026-07-01：T2 测试已先写入，`mvnd -pl fist-kit-infra/fist-trace-context -Dtest=TraceContextsTest test` 仍受限于父 BOM 下载失败，未进入 Java 编译阶段。
- 2026-07-01：T2 已执行 `javac -encoding UTF-8` 编译主代码通过；临时 smoke 程序验证 `restore`、`pushSpan`、scope 清理通过。Maven 单元测试需网络可用后补跑。
- 2026-07-01：T3 已先写入 `DefaultTraceContextRuntimeTest`，本地 `javac -encoding UTF-8` 编译主代码通过；临时 smoke 程序验证入口、出口、MDC 编排通过。Maven 单元测试仍受父 BOM 下载失败限制。
- 2026-07-01：T3 已写入 item 支撑类型测试，本地 `javac -encoding UTF-8` 编译主代码通过；临时 smoke 程序验证单值 item 入口采集、出口写入和 MDC 写入通过。
- 2026-07-01：T3 已写入 registry builder 测试，本地 `javac -encoding UTF-8` 编译主代码通过；临时 smoke 程序验证 item 排序和重复 `context-name` fail-fast 通过。
- 2026-07-01：T4 已写入 starter 自动配置测试和基础实现，`git diff --check` 通过；`mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter "-Dtest=TraceContextAutoConfigurationTest" test` 仍受父 BOM 下载失败限制，未进入 Java 编译阶段。
- 2026-07-01：用户在本地执行 Maven 测试命令无报错。该结果作为 T2、T3、T4 的外部验证记录；当前 Codex 沙箱内 Maven 仍受网络限制，无法复现该结果。
- 2026-07-02：T5 执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dtest=TraceContextServletFilterTest,TraceContextRestClientTest,TraceContextTaskDecoratorTest,TraceSpanAspectTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，9 个测试通过。
- 2026-07-02：执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，core 19 个测试通过，starter 13 个测试通过。
- 2026-07-02：T6 执行 `mvnd -pl fist-kit-app/fist-web/fist-boot-web-app,fist-kit-app/fist-web/fist-support-web -am "-Dtest=FistWebAutoConfigurationTest,AbstractExceptionHandlerTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，4 个目标测试通过。
- 2026-07-02：T7 先执行 `mvnd -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=FeignClientAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 验证 RED，失败原因为缺少 `TraceRelayHandler` 生产类。
- 2026-07-02：T7 执行 `mvnd -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=FeignClientAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，4 个测试通过。
- 2026-07-02：T8 先执行 `mvnd -pl fist-kit-app/fist-web/fist-support-web,fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core -am "-Dtest=MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest,RequestIdGlobalFilterTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 验证 RED，失败原因为缺少 `ReactiveTraceContext` 生产类。
- 2026-07-02：T8 执行 `mvnd -pl fist-kit-app/fist-web/fist-support-web,fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core -am "-Dtest=MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest,RequestIdGlobalFilterTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，4 个目标测试通过。
- 2026-07-02：T8 执行 `mvnd -pl fist-kit-app/fist-web/fist-support-web,fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core,fist-kit-cloud/fist-cloud-gateway/fist-cloud-gateway-acl -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，相关模块测试通过。
- 2026-07-02：T9 直接执行 `mvnd -f examples/fist-trace-context/pom.xml "-Dsurefire.failIfNoSpecifiedTests=false" test` 未通过，原因为独立示例构建无法解析本地 snapshot 版本的 `fist-trace-context-spring-boot-starter`。该方式不作为当前 worktree 内验收命令。
- 2026-07-02：T9 执行 `mvnd -Pexamples -pl examples/fist-trace-context/example-trace-context-basic,examples/fist-trace-context/example-trace-context-extension -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，root reactor 可编译 core、starter 和两个示例模块。
- 2026-07-02：T10 执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，core 19 个测试、starter 13 个测试通过。
- 2026-07-02：T10 执行 `mvnd -pl fist-kit-app/fist-web/fist-boot-web-app,fist-kit-app/fist-web/fist-support-web -am "-Dtest=FistWebAutoConfigurationTest,AbstractExceptionHandlerTraceContextTest,MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，`fist-support-web` 4 个目标测试、`fist-boot-web-app` 2 个目标测试通过。
- 2026-07-02：T10 执行 `mvnd -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=FeignClientAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，4 个测试通过。
- 2026-07-02：T10 执行 `mvnd -pl fist-kit-app/fist-web/fist-support-web,fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core,fist-kit-cloud/fist-cloud-gateway/fist-cloud-gateway-acl -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，相关模块测试通过；其中 `fist-cloud-gateway-acl` 6 个测试通过。
- 2026-07-02：T10 执行 `mvnd -Pexamples -pl examples/fist-trace-context/example-trace-context-basic,examples/fist-trace-context/example-trace-context-extension -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，两个示例模块编译通过。
- 2026-07-02：R4-1 / R4-2 先在沙箱内执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test`，因网络受限失败，未进入 Java 编译阶段。
- 2026-07-02：R4-1 / R4-2 在联网环境执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，core 19 个测试、starter 14 个测试通过。
- 2026-07-02：R4-2 执行 `mvnd -pl fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core -am "-Dsurefire.failIfNoSpecifiedTests=false" test`，首次因 `RequestIdGlobalFilter` 格式校验失败中止；执行 `mvnd -pl fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core spring-javaformat:apply` 后重跑通过，`fist-gateway-auth-core` 16 个测试通过。
- 2026-07-02：清理历史 `spring.factories` 后执行 `mvnd -pl fist-kit-infra/fist-jasypt/fist-jasypt-spring-boot-starter,fist-kit-cloud/fist-cloud-rpc-feign,fist-kit-app/fist-web/fist-boot-web-app -am "-Dtest=FistJasyptAutoConfigurationTest,FeignClientAutoConfigurationTest,FistWebAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，jasypt 4 个测试、Feign 4 个测试、Web auto configuration 2 个测试通过。
- 2026-07-02：清理历史 `spring.factories` 后执行 `mvnd -pl fist-kit-app/fist-boot-apidoc,fist-kit-app/fist-data/fist-boot-data,fist-kit-app/fist-data/fist-boot-crud-mybatis,fist-kit-infra/fist-redisson,fist-kit-app/fist-security/fist-boot-security -am -DskipTests validate` 通过。
- 2026-07-02：R4-3 先执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dtest=TraceContextTaskDecoratorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 验证 RED，失败原因为用户自定义 `TaskDecorator` 存在时 trace 上下文未恢复。
- 2026-07-02：R4-3 / R4-4 执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，core 19 个测试、starter 15 个测试通过。
- 2026-07-02：R4-5 先执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dtest=TraceSpanAspectTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` 验证 RED，失败原因为 `fist.trace-context.span.enabled=false` 时仍创建 `TraceSpanAspect`。
- 2026-07-02：R4-5 执行 `mvnd -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test` 通过，core 19 个测试、starter 16 个测试通过。
