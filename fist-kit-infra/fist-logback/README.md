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

### 注册消息处理器（SPI）

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

多个处理器按 `order()` 升序串行执行。

### 兼容层：MessageProcessorLayout

仅用于兼容旧 layout 配置，不推荐新增使用：

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="com.power4j.fist.logback.layout.MessageProcessorLayout">
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </layout>
    </encoder>
</appender>
```
