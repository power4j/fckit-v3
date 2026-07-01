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
| T2 | doing | 实现核心上下文 API | `TraceContext`、`TraceContextHolder`、`TraceContexts`、`TraceScope`、`TraceContextSnapshot` | 单元测试覆盖读写、snapshot、restore、scope 清理、span 嵌套 |
| T3 | doing | 实现 item 与 runtime 编排 | `TraceContextItem`、`TraceContextItemFactory`、`TraceContextRegistry`、`TraceContextRuntime`、载体接口、抽象基类 | 单元测试覆盖排序、入口冻结、出口透传、MDC 同步、配置冲突 |
| T4 | todo | 实现 starter 配置与默认 processor | `TraceContextProperties`、自动配置、`correlation-id`、`system-code`、配置元数据 | `enabled=false` 不创建 runtime；默认配置可生成 `X-REQ-UID` / `requestId` |
| T5 | todo | 实现 Servlet / RestClient / async / span 注解 | starter Filter、RestClient interceptor、TaskDecorator、`@TraceSpan`、`@TraceSpanGroup` | 测试覆盖入口采集、缺失生成、MDC 清理、RestClient 透传、注解 span、异步恢复 |
| T6 | todo | 治理 Web 旧能力 | `fist-support-web`、`fist-boot-web-app` | 删除旧默认 `HeaderMdcFilter` 和默认 `TraceInfoResolver` 装配；public 类型 deprecated；异常事件读取 `TraceContext` |
| T7 | todo | 治理 Feign 透传 | `fist-cloud-rpc-feign` | 删除 `HeaderRelayHandler`；新增 `TraceRelayHandler`；保留单个 `RelayInterceptor`；认证 header 不受影响 |
| T8 | todo | 治理 reactive / gateway | `fist-support-web`、`fist-gateway-auth-core`、`fist-cloud-gateway-acl` | `RequestIdGlobalFilter` 写入 snapshot 并保留 `X-REQ-UID`；`MdcContextLifter` 每个信号 close scope；异常响应读取统一上下文 |
| T9 | todo | 补充 README / changelog / 示例 | starter README、原模块迁移说明、`examples/fist-trace-context` | README 简洁；两个示例覆盖默认能力和自定义 item |
| T10 | todo | 首阶段集成验证 | 相关模块测试和必要聚合构建 | 关键模块测试通过；破坏性更新说明完整 |

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
