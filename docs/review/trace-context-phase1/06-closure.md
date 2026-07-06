# fist-trace-context 首阶段收口记录

- 项目：`fist-kit3`
- 工作目录：`fist-kit3-jasypt`
- 分支：`fist-kit3-jasypt`
- 收口日期：2026-07-06
- 基准提交：`5b0e08a6 fix: address trace context review nits`

## 1. 收口结论

`fist-trace-context` 首阶段可以收口。

第 5 轮技术评审后的低风险问题已处理并提交，关键模块验证已完成。现有实现满足首阶段目标：`fist-trace-context` 作为唯一核心实现，starter 负责 `fist.trace-context` 配置读取和默认能力装配，Web、Feign、reactive MDC、异常响应和 gateway 相关能力复用核心运行时。

应用项目暂不强制接入新 trace 模块。首阶段只做兼容性扫描，确认应用侧没有直接依赖已迁移的旧实现类型；实际接入应作为独立迁移任务处理。

## 2. 第 5 轮后处理

第 5 轮评审后的修复提交：

- `5b0e08a6 fix: address trace context review nits`

处理内容：

- `TraceContextWebFilter` 增加显式最高优先级，并补充同步段与清理语义注释。
- `UserRelayHandler` 增加显式最低优先级，明确 Feign trace handler 先于用户认证 handler 执行。
- 补充 Feign handler 顺序测试。
- 补充 `system-code` 启动期冻结语义的 JavaDoc。
- `CHANGELOG.md` 补录历史 `spring.factories` 自动配置条目清理。

第 5 轮剩余建议中，reactive 入站协议去重、跨模块端到端测试、应用项目接入验证保留为后续任务。

## 3. 验证结果

### 3.1 代码格式检查

```bash
git diff --check
```

结果：通过，退出码 0。

### 3.2 core 与 starter

```bash
cmd /c mvn -pl fist-kit-infra/fist-trace-context-spring-boot-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" test
```

结果：通过。

- `fist-trace-context`：21 个测试，0 失败，0 错误。
- `fist-trace-context-spring-boot-starter`：17 个测试，0 失败，0 错误。

### 3.3 Web 相关回归

```bash
cmd /c mvn -U -pl fist-kit-app/fist-web/fist-boot-web-app,fist-kit-app/fist-web/fist-support-web -am "-Dtest=FistWebAutoConfigurationTest,TraceContextWebFilterTest,MdcContextLifterTraceContextTest,GlobalErrorAttributesTraceContextTest,AbstractExceptionHandlerTraceContextTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

结果：通过。

- `fist-support-web`：9 个目标测试，0 失败，0 错误。
- `fist-boot-web-app`：2 个目标测试，0 失败，0 错误。

说明：常规沙箱内 Maven 执行曾因 central BOM 解析失败中止，未进入 Java 编译阶段；使用 `-U` 并在联网环境刷新依赖缓存后通过。

### 3.4 Feign 相关回归

```bash
cmd /c mvn -pl fist-kit-cloud/fist-cloud-rpc-feign -am "-Dtest=FeignClientAutoConfigurationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

结果：通过。

- `FeignClientAutoConfigurationTest`：5 个测试，0 失败，0 错误。

### 3.5 Gateway 相关回归

```bash
cmd /c mvn -U -pl fist-kit-cloud/fist-cloud-gateway/fist-gateway-auth-core,fist-kit-cloud/fist-cloud-gateway/fist-cloud-gateway-acl -am "-Dsurefire.failIfNoSpecifiedTests=false" test
```

结果：通过。

- `fist-gateway-auth-core`：16 个测试，0 失败，0 错误。
- `fist-cloud-gateway-acl`：6 个测试，0 失败，0 错误。

说明：`Oauth2IntrospectFilterTest` 覆盖异常分支时会打印预期异常堆栈，Surefire 统计为 0 失败、0 错误。

### 3.6 示例模块

```bash
cmd /c mvn -U -Pexamples -pl examples/fist-trace-context/example-trace-context-basic,examples/fist-trace-context/example-trace-context-extension -am "-Dsurefire.failIfNoSpecifiedTests=false" test
```

结果：通过。

- `example-trace-context-basic`：编译通过，无测试用例。
- `example-trace-context-extension`：编译通过，无测试用例。
- 该命令同时验证 core 与 starter 的示例依赖路径。

## 4. 应用侧兼容性扫描

扫描范围：

- `pac-cloud-crypt-refac`
- `pac-pms-crypt-refac`
- `pac-ssx10a-crypt-refac`

扫描命令：

```bash
rg -n "HeaderMdcFilter|TraceInfoResolver|HeaderRelayHandler|X-REQ-UID|requestId|fist\.trace-context" <app-worktree>
```

结论：

- 未发现应用代码直接引用 `HeaderMdcFilter`、`TraceInfoResolver`、`HeaderRelayHandler`。
- 未发现应用项目已有 `fist.trace-context` 配置。
- 主要命中项是各应用的 `logback-spring.xml` 中仍读取 MDC `requestId`。
- `pac-pms` 文档中存在 `X-REQ-UID` 业务约定，属于对外接口约定，不是旧实现类型依赖。

影响判断：

- 首阶段无需修改应用项目代码。
- 后续应用接入新 trace 模块时，应通过 `fist.trace-context.items.<id>` 配置把默认或自定义关联 ID 的 MDC 名称映射为现有日志格式使用的 `requestId`。
- `X-REQ-UID` 如需继续作为对外请求头约定，应在应用侧配置或自定义 item 中显式声明，而不是依赖旧模块默认行为。

## 5. 未纳入首阶段的事项

以下事项不阻断首阶段收口，建议作为后续独立任务处理：

1. 抽取 reactive 入站协议公共工具，减少 `TraceContextWebFilter` 与 gateway `RequestIdGlobalFilter` 的重复逻辑。
2. 补充跨模块端到端测试，例如 Servlet 入站到 Feign 出站、gateway 入站到下游 Servlet 入站。
3. 为应用项目制定接入迁移说明，覆盖依赖引入、`fist.trace-context` 配置、MDC 名称兼容和请求头约定。
4. 在应用项目中按需启用新 trace 模块，并执行应用级编译与冒烟验证。
5. 评估是否补充 reactive 或 gateway 示例。

## 6. 最终状态

当前首阶段代码与文档可以作为 `fist-kit3` 基线能力进入后续集成流程。应用项目是否启用新 trace 模块不作为本阶段验收条件，应在迁移任务中单独验收。
