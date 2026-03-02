# fist-logback 重构实施计划

设计文档：`tmp/design-logback-refactor.md`（已完成4轮 Codex 评审）
分支：`logback-p2`
模块：`fist-kit-infra/fist-logback`
基础包：`com.power4j.fist.logback`

## 常用命令

```bash
# 编译+测试
mvn test -pl fist-kit-infra/fist-logback

# 单个测试类
mvn test -pl fist-kit-infra/fist-logback -Dtest=ClassName

# 格式化（提交前必须执行）
mvn spring-javaformat:apply -pl fist-kit-infra/fist-logback
```

---

## Task 1：清理旧代码

### 删除文件

**主代码**
- `core/MaskRule.java`
- `core/MaskStrategy.java`
- `core/MaskingRuleLoader.java`
- `core/RuleBasedMaskingProcessor.java`
- `layout/MessageProcessorLayout.java`
- `layout/package-info.java`

**测试代码**（全部删除，后续重写）
- `converter/SensitiveConverterTest.java`
- `core/ProcessorChainTest.java`
- `layout/MessageProcessorLayoutTest.java`

**保留**
- `util/MaskingUtilTest.java`（MaskingUtil 不变）

### 提交
```
chore(fist-logback): remove obsolete P2 implementation before refactor
```

---

## Task 2：新增核心 API 接口

### 2a. MatchSpan

文件：`api/MatchSpan.java`

```java
package com.power4j.fist.logback.api;

/**
 * 检测结果区间，[start, end)，索引单位为 UTF-16 char。
 */
public record MatchSpan(int start, int end) {
    public String extract(String source) {
        return source.substring(start, end);
    }
}
```

测试：`api/MatchSpanTest.java`

```java
class MatchSpanTest {
    @Test void extract_returnsCorrectSubstring() {
        var span = new MatchSpan(3, 7);
        assertEquals("4567", span.extract("0123456789"));
    }
    @Test void extract_fullString() {
        var span = new MatchSpan(0, 5);
        assertEquals("hello", span.extract("hello"));
    }
}
```

### 2b. Detector

文件：`api/Detector.java`

```java
package com.power4j.fist.logback.api;

import java.util.List;
import java.util.Map;

/** 识别器：从消息中找出需要处理的区间。禁止返回 null，无匹配返回空列表。 */
public interface Detector {
    default void init(Map<String, String> props) {}
    List<MatchSpan> detect(String message, LogMessageContext context);
    default void destroy() {}
}
```

### 2c. Transformer

文件：`api/Transformer.java`

```java
package com.power4j.fist.logback.api;

import java.util.Map;

/** 转换器：对 span 内文本做纯函数变换。禁止返回 null。 */
public interface Transformer {
    default void init(Map<String, String> props) {}
    String transform(String value, LogMessageContext context);
    default void destroy() {}
}
```

### 2d. SPI Provider 接口

文件：`spi/DetectorProvider.java`

```java
package com.power4j.fist.logback.spi;

import com.power4j.fist.logback.api.Detector;

public interface DetectorProvider {
    String name();
    Detector create();
}
```

文件：`spi/TransformerProvider.java`

```java
package com.power4j.fist.logback.spi;

import com.power4j.fist.logback.api.Transformer;

public interface TransformerProvider {
    String name();
    Transformer create();
}
```

### 提交
```
feat(fist-logback): add MatchSpan, Detector, Transformer, SPI provider interfaces
```

---

## Task 3：更新 MessageProcessor

在现有 `api/MessageProcessor.java` 中：
- `process()` 加 javadoc：禁止返回 null
- 新增 `default void destroy() {}`

```java
/** 禁止返回 null；无变化时返回原始 message */
String process(String message, LogMessageContext context);

/** 释放资源，由框架在 SensitiveConverter.stop() 时调用一次 */
default void destroy() {}
```

### 提交
```
feat(fist-logback): add destroy lifecycle and null contract to MessageProcessor
```

---

## Task 4：更新 ProcessorChain

核心变更：
1. 构造器接收 `Map<String, String> options`
2. `start()` 对每个 processor 做命名空间过滤后调用 `init()`，失败则禁用并 addWarn
3. `execute()` 的 catch 改为 `Throwable`
4. 新增 `destroy()` 方法

```java
public class ProcessorChain extends ContextAwareBase {

    private final List<MessageProcessor> input;
    private final Map<String, String> globalOptions;
    private List<MessageProcessor> active;  // init 成功的处理器

    public ProcessorChain(List<MessageProcessor> processors, Map<String, String> options) {
        this.input = processors;
        this.globalOptions = options;
    }

    public void start() {
        List<MessageProcessor> sorted = new ArrayList<>(input);
        sorted.sort(Comparator.comparingInt(MessageProcessor::order));
        List<MessageProcessor> initialized = new ArrayList<>();
        for (MessageProcessor p : sorted) {
            try {
                p.init(buildOptions(p.name()));
                initialized.add(p);
                addInfo("[init] " + p.name() + " ok");
            }
            catch (Throwable t) {
                addWarn("[init] " + p.name() + " failed, disabled: " + t.getMessage());
            }
        }
        this.active = Collections.unmodifiableList(initialized);
        addInfo("ProcessorChain started with " + active.size() + " active processor(s)");
    }

    public String execute(String message, LogMessageContext context) {
        if (message == null || message.isEmpty() || active == null || active.isEmpty()) {
            return message;
        }
        String result = message;
        for (MessageProcessor p : active) {
            try {
                if (!p.supports(context)) continue;
                String out = p.process(result, context);
                result = (out != null) ? out : result;
            }
            catch (Throwable t) {
                addWarn("[process] " + p.name() + " threw exception, keeping original: " + t.getMessage());
            }
        }
        return result;
    }

    public void destroy() {
        if (active == null) return;
        for (MessageProcessor p : active) {
            try { p.destroy(); }
            catch (Throwable t) { addWarn("[destroy] " + p.name() + ": " + t.getMessage()); }
        }
    }

    /** 构建处理器专属 options：全局键（排除 processor.*）+ processor.<name>.* 覆盖 */
    private Map<String, String> buildOptions(String processorName) {
        Map<String, String> result = new LinkedHashMap<>();
        String ns = "processor." + processorName + ".";
        globalOptions.forEach((k, v) -> {
            if (!k.startsWith("processor.")) result.put(k, v);
        });
        globalOptions.forEach((k, v) -> {
            if (k.startsWith(ns)) result.put(k.substring(ns.length()), v);
        });
        return Collections.unmodifiableMap(result);
    }
}
```

测试：`core/ProcessorChainTest.java`

覆盖场景：
- init() 被调用，options 正确过滤（全局键 + 命名空间覆盖）
- init() 失败的 processor 被禁用，不影响其他
- execute() 中 supports()=false 时跳过
- execute() 中 process() 抛 Throwable 时保留原文继续
- process() 返回 null 时保留原文
- destroy() 调用每个 active processor 的 destroy()

### 提交
```
feat(fist-logback): refactor ProcessorChain with init/destroy lifecycle and namespace options
```

---

## Task 5：更新 SensitiveConverter

核心变更：
- `start()` 中 `p.configure(options)` → 删除（由 ProcessorChain.start() 统一调用 init）
- `ProcessorChain` 构造器传入 options
- 新增 `stop()` 触发 `processorChain.destroy()`

```java
@Override
public void start() {
    List<MessageProcessor> processors = loadProcessors();
    Map<String, String> options = parseOptions(getOptionList());
    processorChain = new ProcessorChain(processors, Collections.unmodifiableMap(options));
    processorChain.setContext(getContext());
    processorChain.start();
    super.start();
}

@Override
public void stop() {
    if (processorChain != null) {
        processorChain.destroy();
    }
    super.stop();
}
```

测试：`converter/SensitiveConverterTest.java`

覆盖场景：
- start() 后 convert() 正常工作
- stop() 触发 destroy()
- 无 MessageProcessor 时 convert() 返回原文

### 提交
```
feat(fist-logback): update SensitiveConverter to use init/destroy lifecycle
```

---

## Task 6：ProcessingRule

文件：`core/ProcessingRule.java`

```java
package com.power4j.fist.logback.core;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.api.Transformer;

public final class ProcessingRule {
    private final String name;
    private final Detector detector;
    private final Transformer transformer;

    public ProcessingRule(String name, Detector detector, Transformer transformer) {
        this.name = name;
        this.detector = detector;
        this.transformer = transformer;
    }

    public String getName() { return name; }
    public Detector getDetector() { return detector; }
    public Transformer getTransformer() { return transformer; }
}
```

### 提交
```
feat(fist-logback): add ProcessingRule data class
```

---

## Task 7：内置实现

### 7a. RegexDetector

文件：`builtin/RegexDetector.java`、`builtin/RegexDetectorProvider.java`

```java
public class RegexDetector implements Detector {
    private Pattern pattern;

    @Override
    public void init(Map<String, String> props) {
        String p = props.get("pattern");
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("RegexDetector: 'pattern' is required");
        }
        this.pattern = Pattern.compile(p);
    }

    @Override
    public List<MatchSpan> detect(String message, LogMessageContext context) {
        List<MatchSpan> spans = new ArrayList<>();
        Matcher m = pattern.matcher(message);
        while (m.find()) {
            spans.add(new MatchSpan(m.start(), m.end()));
        }
        return spans;
    }
}

public class RegexDetectorProvider implements DetectorProvider {
    @Override public String name() { return "regex"; }
    @Override public Detector create() { return new RegexDetector(); }
}
```

测试：`builtin/RegexDetectorTest.java`

覆盖场景：
- 正常匹配返回正确 span 列表
- 无匹配返回空列表
- pattern 缺失时 init() 抛异常
- 正则编译失败时 init() 抛异常

### 7b. MaskMiddleTransformer

文件：`builtin/MaskMiddleTransformer.java`、`builtin/MaskMiddleTransformerProvider.java`

```java
public class MaskMiddleTransformer implements Transformer {
    private int keepFirst = 0;
    private int keepLast = 0;
    private char maskChar = '*';

    @Override
    public void init(Map<String, String> props) {
        keepFirst = Integer.parseInt(props.getOrDefault("keepFirst", "0"));
        keepLast = Integer.parseInt(props.getOrDefault("keepLast", "0"));
        String mc = props.getOrDefault("maskChar", "*");
        maskChar = mc.isEmpty() ? '*' : mc.charAt(0);
    }

    @Override
    public String transform(String value, LogMessageContext context) {
        return MaskingUtil.maskMiddle(value, keepFirst, keepLast, maskChar);
    }
}
```

### 7c. MaskAllTransformer

```java
public class MaskAllTransformer implements Transformer {
    private char maskChar = '*';

    @Override
    public void init(Map<String, String> props) {
        String mc = props.getOrDefault("maskChar", "*");
        maskChar = mc.isEmpty() ? '*' : mc.charAt(0);
    }

    @Override
    public String transform(String value, LogMessageContext context) {
        return String.valueOf(maskChar).repeat(value.length());
    }
}
```

### 7d. ReplaceTransformer

```java
public class ReplaceTransformer implements Transformer {
    private Pattern pattern;
    private String replacement;

    @Override
    public void init(Map<String, String> props) {
        String p = props.get("pattern");
        replacement = props.get("replacement");
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("ReplaceTransformer: 'pattern' is required");
        }
        if (replacement == null) {
            throw new IllegalArgumentException("ReplaceTransformer: 'replacement' is required");
        }
        this.pattern = Pattern.compile(p);
    }

    @Override
    public String transform(String value, LogMessageContext context) {
        return pattern.matcher(value).replaceAll(replacement);
    }
}
```

测试：`builtin/TransformerTest.java`（三个 transformer 合并一个测试类）

### 提交
```
feat(fist-logback): add builtin Detector and Transformer implementations
```

---

## Task 8：RuleEngineLoader

文件：`core/RuleEngineLoader.java`

职责：SPI 加载 + 配置解析 + 规则组装

### 路径解析逻辑

```java
private InputStream openConfigStream(String configFile) throws IOException {
    String path = configFile.trim();
    if (path.startsWith("classpath:")) {
        String resource = path.substring("classpath:".length()).replaceFirst("^/+", "");
        InputStream is = tryClasspath(resource);
        if (is == null) throw new IOException("classpath resource not found: " + resource);
        return is;
    }
    if (path.startsWith("file:")) {
        String filePath = decodeUri(path.substring("file:".length()).trim());
        return new FileInputStream(filePath);
    }
    // 无前缀：classpath
    InputStream is = tryClasspath(path);
    if (is == null) throw new IOException("classpath resource not found: " + path);
    return is;
}

private InputStream tryClasspath(String resource) {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    InputStream is = (tccl != null) ? tccl.getResourceAsStream(resource) : null;
    return (is != null) ? is : RuleEngineLoader.class.getClassLoader().getResourceAsStream(resource);
}

private String decodeUri(String raw) {
    try { return java.net.URLDecoder.decode(raw, StandardCharsets.UTF_8); }
    catch (Exception e) { return raw; }
}
```

### 规则解析逻辑

```java
// 从 Properties 中提取规则名集合
// key 格式：rule.<name>.<field>
// 规则名约束：[A-Za-z0-9_-]+
private Set<String> extractRuleNames(Properties props) {
    Set<String> names = new LinkedHashSet<>();
    for (String key : props.stringPropertyNames()) {
        if (key.startsWith("rule.")) {
            String[] parts = key.split("\\.", 3);
            if (parts.length >= 3 && parts[1].matches("[A-Za-z0-9_\\-]+")) {
                names.add(parts[1]);
            }
        }
    }
    return names;
}
```

### 规则排序

```java
// 先按 order 升序，order 相同按规则名字典序
rules.sort(Comparator.comparingInt(ProcessingRule::getOrder)
    .thenComparing(ProcessingRule::getName));
```

注意：`ProcessingRule` 需要增加 `order` 字段。

### SPI 冲突检测

```java
Map<String, DetectorProvider> detectorRegistry = new LinkedHashMap<>();
for (DetectorProvider p : ServiceLoader.load(DetectorProvider.class)) {
    if (detectorRegistry.containsKey(p.name())) {
        throw new IllegalStateException(
            "Duplicate DetectorProvider name: '" + p.name() + "' from " + p.getClass().getName());
    }
    detectorRegistry.put(p.name(), p);
    infoLogger.accept("[SPI] DetectorProvider: " + p.name() + " -> " + p.getClass().getName());
}
```

### ServiceConfigurationError 隔离

```java
Iterator<DetectorProvider> it = ServiceLoader.load(DetectorProvider.class).iterator();
while (it.hasNext()) {
    try {
        DetectorProvider p = it.next();
        // ... 注册
    } catch (ServiceConfigurationError e) {
        warnLogger.accept("[SPI] Failed to load DetectorProvider: " + e.getMessage());
    }
}
```

测试：`core/RuleEngineLoaderTest.java`

覆盖场景：
- classpath 路径加载
- file: 绝对路径加载
- 配置文件不存在时返回空规则列表
- 规则名字典序排序
- rule.order 显式排序
- detector 缺失时跳过规则
- transformer 缺失时跳过规则
- pattern 缺失时跳过规则
- SPI 名称冲突时抛异常

### 提交
```
feat(fist-logback): add RuleEngineLoader with config parsing and SPI loading
```

---

## Task 9：RuleEngine

文件：`core/RuleEngine.java`

```java
public class RuleEngine extends ContextAwareBase implements MessageProcessor {

    private List<ProcessingRule> rules = Collections.emptyList();

    @Override
    public String name() { return "RuleEngine"; }

    @Override
    public void init(Map<String, String> options) {
        String configFile = options.getOrDefault("configFile", "logback-masking.properties");
        RuleEngineLoader loader = new RuleEngineLoader(configFile, this::addInfo, this::addWarn);
        try {
            this.rules = loader.load();
            addInfo("RuleEngine loaded " + rules.size() + " rule(s)");
        }
        catch (IllegalStateException e) {
            addError("RuleEngine init failed: " + e.getMessage(), e);
            throw e;  // 触发 ProcessorChain 禁用此 processor
        }
        catch (Exception e) {
            addWarn("RuleEngine failed to load config, running with 0 rules: " + e.getMessage());
        }
    }

    @Override
    public String process(String message, LogMessageContext context) {
        String result = message;
        for (ProcessingRule rule : rules) {
            result = applyRule(rule, result, context);
        }
        return result;
    }

    @Override
    public void destroy() {
        for (ProcessingRule rule : rules) {
            try { rule.getDetector().destroy(); } catch (Throwable ignored) {}
            try { rule.getTransformer().destroy(); } catch (Throwable ignored) {}
        }
    }

    private String applyRule(ProcessingRule rule, String message, LogMessageContext context) {
        List<MatchSpan> rawSpans;
        try {
            rawSpans = rule.getDetector().detect(message, context);
            if (rawSpans == null) {
                addWarn("[" + rule.getName() + "] detector returned null, skipping");
                return message;
            }
        }
        catch (Throwable t) {
            addWarn("[" + rule.getName() + "] detector threw exception, skipping: " + t.getMessage());
            return message;
        }

        // 过滤非法 span
        int len = message.length();
        List<MatchSpan> valid = new ArrayList<>();
        for (MatchSpan s : rawSpans) {
            if (s.start() < 0 || s.end() > len) {
                addWarn("[" + rule.getName() + "] invalid span [" + s.start() + "," + s.end() + "), discarded");
            }
            else if (s.start() < s.end()) {
                valid.add(s);
            }
        }

        // 排序 + 贪心去重
        valid.sort(Comparator.comparingInt(MatchSpan::start).reversed()
            .thenComparingInt(MatchSpan::end));
        valid.sort(Comparator.comparingInt(MatchSpan::start)
            .thenComparingInt((MatchSpan s) -> s.end()).reversed());
        List<MatchSpan> deduped = greedyDedup(valid);

        // 全部 transform，再倒序替换
        List<String> replacements = new ArrayList<>();
        for (MatchSpan span : deduped) {
            try {
                String rep = rule.getTransformer().transform(span.extract(message), context);
                replacements.add(rep != null ? rep : span.extract(message));
                if (rep == null) addWarn("[" + rule.getName() + "] transformer returned null, keeping original");
            }
            catch (Throwable t) {
                replacements.add(span.extract(message));
                addWarn("[" + rule.getName() + "] transformer threw exception, keeping original: " + t.getMessage());
            }
        }

        // 倒序替换（deduped 已按 start ASC，倒序遍历）
        StringBuilder sb = new StringBuilder(message);
        for (int i = deduped.size() - 1; i >= 0; i--) {
            MatchSpan span = deduped.get(i);
            sb.replace(span.start(), span.end(), replacements.get(i));
        }
        return sb.toString();
    }

    private List<MatchSpan> greedyDedup(List<MatchSpan> sorted) {
        // sorted: start ASC, end DESC
        List<MatchSpan> result = new ArrayList<>();
        int lastEnd = -1;
        for (MatchSpan s : sorted) {
            if (s.start() >= lastEnd) {
                result.add(s);
                lastEnd = s.end();
            }
        }
        return result;
    }
}
```

测试：`core/RuleEngineTest.java`

覆盖场景：
- 基本脱敏（单规则）
- 多规则串行（后规则看到前规则处理后的文本）
- 重叠 span 贪心去重
- 紧邻 span 不视为重叠
- detector 抛异常时跳过本规则，返回原文
- transformer 返回 null 时保留原文
- 越界 span 被丢弃
- 零规则时原文透传

### 提交
```
feat(fist-logback): add RuleEngine core execution engine
```

---

## Task 10：SPI 注册文件

创建以下文件：

`src/main/resources/META-INF/services/com.power4j.fist.logback.api.MessageProcessor`
```
com.power4j.fist.logback.core.RuleEngine
```

`src/main/resources/META-INF/services/com.power4j.fist.logback.spi.DetectorProvider`
```
com.power4j.fist.logback.builtin.RegexDetectorProvider
```

`src/main/resources/META-INF/services/com.power4j.fist.logback.spi.TransformerProvider`
```
com.power4j.fist.logback.builtin.MaskMiddleTransformerProvider
com.power4j.fist.logback.builtin.MaskAllTransformerProvider
com.power4j.fist.logback.builtin.ReplaceTransformerProvider
```

### 提交
```
feat(fist-logback): register SPI services for RuleEngine and builtin providers
```

---

## Task 11：集成测试

文件：`converter/SensitiveConverterIntegrationTest.java`

覆盖场景：
- 完整链路：logback.xml 配置 `%mask{configFile=...}` → 手机号脱敏
- 多规则：手机号 + 邮箱同时脱敏
- 无配置文件时原文透传
- 自定义 MessageProcessor 与 RuleEngine 共存（order 控制顺序）

### 提交
```
test(fist-logback): add integration tests for full masking pipeline
```

---

## 完成检查

- [ ] `mvn test -pl fist-kit-infra/fist-logback` 全部通过
- [ ] `mvn spring-javaformat:validate -pl fist-kit-infra/fist-logback` 通过
- [ ] 无旧 API（`configure()`）残留
- [ ] SPI 文件路径正确

## 当前停点/下一步

**完成（2026-03-02）：所有 Task 1-11 已提交，59 个测试全部通过**

### 已完成（已提交）
- Task 1：清理旧代码
- Task 2-3：新增 API 接口 + 更新 MessageProcessor
- Task 4-5：更新 ProcessorChain + SensitiveConverter
- Task 6：ProcessingRule
- Task 7：builtin 实现（RegexDetector/MaskMiddle/MaskAll/Replace + Providers）
- Task 8：RuleEngineLoader
- Task 9：RuleEngine + ProcessorChain context 传播修复
- Task 10：SPI 注册文件
- Task 11：集成测试

### 根因说明
集成测试失败的根本原因：`.properties` 文件中 `\d` 被 Java `Properties.load()` 解析为 `d`，
导致手机号正则 `1[3-9]\d{9}` 变为 `1[3-9]d{9}`，无法匹配手机号。
修复：将 `.properties` 文件中的 `\d` 改为 `\\d`。

另修复：在 `ProcessorChain.start()` 中对 `ContextAware` 的 processor 传播 context。

### 下一步
重构完成，分支 `logback-p2` 可以合并到主分支。
