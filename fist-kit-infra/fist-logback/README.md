## logback 日志处理

模块定位: 日志消息处理的工具包,应用层使用本工具包能够不写代码或者少量代码实现日志消息的二次处理.

> 性能建议:
> 首选方案是对日志输出代码进行改造,先脱敏再打印.不过这也有代价,就是工作量较大.
> 本工具包提供框架级别的处理,不入侵业务代码,但是需要对所有日志进行`检测+加工`处理,性能影响较大.

### 主路径：SensitiveConverter + `%mask`

注册转换规则，在 pattern 中使用 `%mask` 替代 `%msg`：

```xml
<configuration>
    <conversionRule conversionWord="mask"
        converterClass="com.power4j.fist.logback.converter.SensitiveConverter"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %mask%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

`SensitiveConverter` 仅处理消息体，不影响 `%d/%thread/%level/%logger` 等元数据字段。

### 方案一：配置文件驱动（RuleEngine，推荐）

无需写代码，通过 `.properties` 配置文件声明脱敏规则，`RuleEngine` 已通过 SPI 自动注册。

**步骤 1**：在 `%mask` 中指定配置文件路径：

```xml
<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %mask{configFile=logback-masking.properties}%n</pattern>
```

路径格式：
- 无前缀 / `classpath:` — 从 classpath 加载（默认值：`logback-masking.properties`）
- `file:` — 从文件系统绝对路径加载

**步骤 2**：编写配置文件，每条规则形如 `rule.<名称>.<字段>=<值>`：

```properties
# 手机号：保留前3位和后4位，中间打码
rule.phone.detector=regex
rule.phone.transformer=maskMiddle
rule.phone.pattern=1[3-9]\\d{9}
rule.phone.keepFirst=3
rule.phone.keepLast=4
rule.phone.order=0

# 邮箱：整体遮盖
rule.email.detector=regex
rule.email.transformer=maskAll
rule.email.pattern=[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}
rule.email.order=1
```

> 注意：`.properties` 文件中反斜杠需要转义，正则中的 `\d` 应写为 `\\d`。

**内置 Detector**

| 名称 | 说明 | 必填参数 |
|------|------|---------|
| `regex` | 正则匹配，返回所有匹配区间 | `pattern`（Java 正则） |

**内置 Transformer**

| 名称 | 说明 | 参数 |
|------|------|------|
| `maskMiddle` | 保留首尾，中间打码 | `keepFirst`（默认 0）、`keepLast`（默认 0）、`maskChar`（默认 `*`） |
| `maskAll` | 整体替换为掩码字符 | `maskChar`（默认 `*`） |
| `replace` | 对匹配区间内再做正则替换 | `pattern`（必填）、`replacement`（必填） |

规则按 `order` 升序执行，`order` 相同时按规则名字典序。

### 方案二：自定义消息处理器（SPI）

实现 `MessageProcessor` 接口，并在 `META-INF/services/` 下注册：

```
META-INF/services/com.power4j.fist.logback.api.MessageProcessor
```

文件内容填写实现类的全限定名，每行一个。

示例实现（手机号脱敏）：

```java
public class MobileProcessor implements MessageProcessor {

    private static final Pattern PATTERN = Pattern.compile("1[3-9]\\d{9}");

    @Override
    public int order() {
        return 10;
    }

    @Override
    public String process(String message, LogMessageContext context) {
        return PATTERN.matcher(message)
            .replaceAll(m -> MaskingUtil.maskMiddle(m.group(), 3, 4));
    }

}
```

多个处理器（包括内置 `RuleEngine`）按 `order()` 升序串行执行。
