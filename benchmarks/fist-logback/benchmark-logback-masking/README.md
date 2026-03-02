# benchmark-logback-masking

使用 JMH 评估 fist-logback 日志脱敏的性能损耗。

## 基准场景

- **baseline** — 直接消费原始消息（无脱敏）
- **withMasking** — 经 ProcessorChain 脱敏处理

每个场景分别使用无敏感数据和含手机号的消息进行测试。

## 构建与运行

需要两步：先通过 maven-shade-plugin 打包 fat jar，再运行 JMH。

**1. 构建 fat jar**

benchmark 依赖项目内的 `fist-logback` 模块，`-am` 会自动编译其依赖链：

```bash
mvn package -Pbenchmarks -pl benchmarks/fist-logback/benchmark-logback-masking -am
```

产物位于 `benchmarks/fist-logback/benchmark-logback-masking/target/benchmarks.jar`。

**2. 运行基准测试**

```bash
java -jar benchmarks/fist-logback/benchmark-logback-masking/target/benchmarks.jar
```

可追加 JMH 参数，例如只跑 `withMasking` 场景：

```bash
java -jar benchmarks/fist-logback/benchmark-logback-masking/target/benchmarks.jar withMasking
```

## 参考数据

开发机环境，单规则（手机号 regex），JMH Fork=1, Warmup 3x1s, Measurement 5x1s：

| 场景 | 消息类型 | 吞吐量 (ops/ms) |
|------|---------|-----------------|
| baseline | 无敏感数据 | ~3,043,877 |
| baseline | 含手机号 | ~3,005,756 |
| withMasking | 无敏感数据（无匹配） | ~39,639 |
| withMasking | 含手机号（命中规则） | ~5,327 |

结论：

- 脱敏扫描本身（无匹配）使吞吐量降至 baseline 的 ~1.3%，单次处理耗时约 **25μs**
- 命中规则并执行替换时，吞吐量进一步降至 ~0.17%，单次处理耗时约 **188μs**
- 对于典型应用场景，日志输出的 IO 耗时远大于脱敏处理耗时，性能影响可忽略
