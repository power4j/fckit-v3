# example-logback-basic

演示 fist-logback 最基础的接入方式：通过配置文件定义脱敏规则，自动对日志中的手机号和邮箱进行脱敏。

## 脱敏规则

- 手机号 — 保留前 3 后 4 位，中间用 `*` 替换
- 邮箱 — 全部替换为 `*`

## 运行

```bash
mvn spring-boot:run -Pexamples -pl examples/fist-logback/example-logback-basic
```
