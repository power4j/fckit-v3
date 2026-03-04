# JDK 8 降级与 JSpecify Null-Safety 迁移实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 fist-kit-api、fist-jackson、fist-mybatis-plus、fist-logback 四个模块降级为 JDK 8 编译目标，用 JSpecify 注解替换 Spring null-safety 注解，断开与 spring-core 的依赖。

**Architecture:** 在每个模块的 pom.xml 中覆盖编译器配置（main 代码 `--release 8`，test 代码保持 JDK 17），移除 spring-core 依赖并添加 jspecify。Spring 的 `@Nullable` 替换为 `@org.jspecify.annotations.Nullable`，`@NonNullApi` + `@NonNullFields` 替换为 `@NullMarked`。fist-logback 额外需要回退 JDK 9+ 语言特性和 API。

**Tech Stack:** Maven, JSpecify 1.0.0, JDK 17（编译环境），JDK 8（目标字节码）

---

## 编译器配置策略

四个模块统一在 `<properties>` 中覆盖编译器设置：

```xml
<properties>
    <maven.compiler.release>8</maven.compiler.release>
    <maven.compiler.testRelease>17</maven.compiler.testRelease>
</properties>
```

- `maven.compiler.release=8`：main 代码用 `--release 8`，编译器会校验不使用 JDK 9+ 标准库 API
- `maven.compiler.testRelease=17`：测试代码保持 JDK 17，不受限制

使用属性方式而非 execution-level 覆盖，避免与父 POM 的 `source/target` 设置产生冲突（Codex review 建议）。

已知编译警告：`warning: unknown enum constant ElementType.MODULE`（JSpecify 的 `@NullMarked` 支持 Java 9 module 声明，JDK 8 目标时会出现此警告，无害）。

## JSpecify 注解映射

| Spring 注解 | JSpecify 替代 | 放置位置变化 |
|---|---|---|
| `@org.springframework.lang.Nullable` | `@org.jspecify.annotations.Nullable` | TYPE_USE：放在类型前面，如 `private @Nullable String field` |
| `@NonNullApi` + `@NonNullFields` | `@org.jspecify.annotations.NullMarked` | 一个注解替代两个 |

---

### Task 1: 添加 JSpecify 依赖管理

**Files:**
- Modify: `pom.xml` (根 POM，添加版本属性)
- Modify: `fist-kit-build/pom.xml` (添加 dependencyManagement 条目)

**Step 1: 在根 pom.xml 添加版本属性**

在 `<properties>` 的 `<!-- libs -->` 区域添加：

```xml
<jspecify.version>1.0.0</jspecify.version>
```

**Step 2: 在 fist-kit-build/pom.xml 添加依赖管理**

在 `<dependencyManagement><dependencies>` 中（非 BOM import 区域，与其他 jar 依赖并列）添加：

```xml
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>${jspecify.version}</version>
</dependency>
```

**Step 3: 验证依赖解析**

Run: `mvn help:evaluate -pl fist-kit-infra/fist-kit-api -Dexpression=project.dependencyManagement -q -DforceStdout | grep -A2 jspecify`
Expected: 能看到 jspecify 1.0.0

**Step 4: Commit**

```bash
git add pom.xml fist-kit-build/pom.xml
git commit -m "build: add JSpecify 1.0.0 dependency management"
```

---

### Task 2: 迁移 fist-kit-api

**Files:**
- Modify: `fist-kit-infra/fist-kit-api/pom.xml`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/common/utils/package-info.java`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/security/core/package-info.java`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/common/utils/MapKit.java`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/common/utils/TypeValidator.java`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/security/core/UserInfo.java`
- Modify: `fist-kit-infra/fist-kit-api/src/main/java/com/power4j/fist/boot/security/core/UserInfoExtractor.java`

**Step 1: 修改 pom.xml**

移除 spring-core 依赖，添加 jspecify 依赖，添加编译器属性：

```xml
<properties>
    <maven.compiler.release>8</maven.compiler.release>
    <maven.compiler.testRelease>17</maven.compiler.testRelease>
</properties>
```

```xml
<!-- 移除 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
</dependency>

<!-- 添加 -->
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
</dependency>
```

**Step 2: 迁移 package-info.java（2 个文件）**

`com/power4j/fist/boot/common/utils/package-info.java`：

```java
@NullMarked
package com.power4j.fist.boot.common.utils;

import org.jspecify.annotations.NullMarked;
```

`com/power4j/fist/boot/security/core/package-info.java`：

```java
@NullMarked
package com.power4j.fist.boot.security.core;

import org.jspecify.annotations.NullMarked;
```

保留原有的 license header 不变。

**Step 3: 迁移 MapKit.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

调整注解位置（TYPE_USE 放置）：
- 方法参数 `@Nullable Map<? extends K, ?> map` → `@Nullable Map<? extends K, ?> map`（位置不变，已在类型前）
- 方法参数 `@Nullable Map<K, V> map` → `@Nullable Map<K, V> map`（同上）

**Step 4: 迁移 TypeValidator.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

调整注解位置：
- `public boolean castCheck(@Nullable Object obj)` → 位置不变
- `public static boolean castCheck(@Nullable Object obj, Class<?> cls)` → 位置不变

**Step 5: 迁移 UserInfo.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

移除 `@Serial` 注解及其导入：
- 删除 `import java.io.Serial;`
- 删除 `@Serial` 注解行（`private static final long serialVersionUID = 1L;` 保留）

调整 `@Nullable` 位置（TYPE_USE）：
- `@Nullable private String nickName;` → `private @Nullable String nickName;`
- `@Nullable private String avatarUrl;` → `private @Nullable String avatarUrl;`
- `@Nullable private Map<String, Object> meta;` → `private @Nullable Map<String, Object> meta;`
- `@Nullable public Object putMetaProp(...)` → `public @Nullable Object putMetaProp(...)`
- `@Nullable public Object putMetaPropIfAbsent(...)` → `public @Nullable Object putMetaPropIfAbsent(...)`

**Step 6: 迁移 UserInfoExtractor.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

注解位置：
- `Optional<UserInfo> extractAuthUser(@Nullable Map<String, ?> map)` → 位置不变

**Step 7: 验证编译和测试**

Run: `mvn clean test -pl fist-kit-infra/fist-kit-api -am`
Expected: BUILD SUCCESS

**Step 8: Commit**

```bash
git add fist-kit-infra/fist-kit-api/
git commit -m "refactor(fist-kit-api): migrate to JSpecify and target JDK 8

- Replace Spring null-safety annotations with JSpecify (@Nullable, @NullMarked)
- Remove spring-core dependency
- Remove @Serial annotation (JDK 14+)
- Set compiler release to 8 for main sources"
```

---

### Task 3: 迁移 fist-jackson

**Files:**
- Modify: `fist-kit-infra/fist-jackson/pom.xml`
- Modify: `fist-kit-infra/fist-jackson/src/main/java/com/power4j/fist/jackson/support/obfuscation/package-info.java`
- Modify: `fist-kit-infra/fist-jackson/src/main/java/com/power4j/fist/jackson/support/obfuscation/ObfuscatedStringSerializer.java`
- Modify: `fist-kit-infra/fist-jackson/src/main/java/com/power4j/fist/jackson/support/obfuscation/ObfuscatedStringDeserializer.java`

**Step 1: 修改 pom.xml**

移除 spring-core，添加 jspecify，添加编译器属性（同 Task 2 的 properties 模式）。

**Step 2: 迁移 package-info.java**

`com/power4j/fist/jackson/support/obfuscation/package-info.java`：

```java
@NullMarked
package com.power4j.fist.jackson.support.obfuscation;

import org.jspecify.annotations.NullMarked;
```

**Step 3: 修复 ObfuscatedStringSerializer.java 的 Optional.isEmpty()**

第 89 行：
- `if (processor.isEmpty()) {` → `if (!processor.isPresent()) {`

（该文件无 Spring 注解导入，无需替换导入）

**Step 4: 修复 ObfuscatedStringDeserializer.java 的 Optional.isEmpty()**

第 87 行：
- `if (processor.isEmpty()) {` → `if (!processor.isPresent()) {`

（该文件无 Spring 注解导入，无需替换导入）

**Step 5: 验证编译和测试**

Run: `mvn clean test -pl fist-kit-infra/fist-jackson -am`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add fist-kit-infra/fist-jackson/
git commit -m "refactor(fist-jackson): migrate to JSpecify and target JDK 8

- Replace Spring null-safety annotations with JSpecify @NullMarked
- Remove spring-core dependency
- Replace Optional.isEmpty() with !isPresent() for JDK 8 compatibility
- Set compiler release to 8 for main sources"
```

---

### Task 4: 迁移 fist-mybatis-plus

**Files:**
- Modify: `fist-kit-infra/fist-mybatis-plus/pom.xml`
- Modify: `fist-kit-infra/fist-mybatis-plus/src/main/java/com/power4j/fist/mybatis/extension/meta/package-info.java`
- Modify: `fist-kit-infra/fist-mybatis-plus/src/main/java/com/power4j/fist/mybatis/extension/meta/MetaHandlerCompose.java`
- Modify: `fist-kit-infra/fist-mybatis-plus/src/main/java/com/power4j/fist/mybatis/extension/meta/MetaInfo.java`

**Step 1: 修改 pom.xml**

移除 spring-core，添加 jspecify，添加编译器属性（同 Task 2 的 properties 模式）。

**Step 2: 迁移 package-info.java**

```java
@NullMarked
package com.power4j.fist.mybatis.extension.meta;

import org.jspecify.annotations.NullMarked;
```

**Step 3: 迁移 MetaHandlerCompose.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

调整 `@Nullable` 位置（5 处）：
- 字段 `@Nullable private final ValueSupplier globalHandler;` → `private final @Nullable ValueSupplier globalHandler;`
- 构造器参数 `@Nullable ValueSupplier globalHandler` → 位置不变（已在类型前）
- 内部类字段 `@Nullable private final FillWith fillWith;` → `private final @Nullable FillWith fillWith;`
- 内部类构造器参数 `@Nullable FillWith fillWith` → 位置不变
- 静态方法参数 `@Nullable FillWith annotation` → 位置不变

**Step 4: 迁移 MetaInfo.java**

替换导入：
- `import org.springframework.lang.Nullable;` → `import org.jspecify.annotations.Nullable;`

调整 `@Nullable` 位置：
- 方法参数 `@Nullable String raw` → 位置不变

**Step 5: 验证编译和测试**

Run: `mvn clean test -pl fist-kit-infra/fist-mybatis-plus -am`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add fist-kit-infra/fist-mybatis-plus/
git commit -m "refactor(fist-mybatis-plus): migrate to JSpecify and target JDK 8

- Replace Spring null-safety annotations with JSpecify (@Nullable, @NullMarked)
- Remove spring-core dependency
- Set compiler release to 8 for main sources"
```

---

### Task 5: 迁移 fist-logback

**Files:**
- Modify: `fist-kit-infra/fist-logback/pom.xml`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/api/package-info.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/converter/package-info.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/core/package-info.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/util/package-info.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/api/MatchSpan.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/builtin/MaskAllTransformer.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/builtin/RegexDetector.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/builtin/ReplaceTransformer.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/core/ProcessorChain.java`
- Modify: `fist-kit-infra/fist-logback/src/main/java/com/power4j/fist/logback/core/RuleEngineLoader.java`

**Step 1: 修改 pom.xml**

移除 spring-core（代码中实际未使用），添加 jspecify，添加编译器属性（同 Task 2 的 properties 模式）。

将 logback-classic 改为 `provided` scope，使用方需自行引入 logback 依赖：

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <scope>provided</scope>
</dependency>
```

**Step 2: 迁移 4 个 package-info.java**

每个文件统一改为（保留各自的 license header 和 package 声明）：

```java
@NullMarked
package com.power4j.fist.logback.xxx;

import org.jspecify.annotations.NullMarked;
```

替换 `xxx` 为对应包名：`api`、`converter`、`core`、`util`。

**Step 3: 将 MatchSpan.java 从 record 转为普通类**

将 `MatchSpan.java` 的 record 定义改为等效的普通类：

```java
package com.power4j.fist.logback.api;

import java.util.Objects;

/**
 * (保留原有 Javadoc)
 */
public final class MatchSpan {

	private final int start;

	private final int end;

	public MatchSpan(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}

	public String extract(String source) {
		return source.substring(start, end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MatchSpan)) {
			return false;
		}
		MatchSpan that = (MatchSpan) o;
		return start == that.start && end == that.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return "MatchSpan[start=" + start + ", end=" + end + "]";
	}

}
```

**Step 4: 修复 MaskAllTransformer.java 的 String.repeat()**

第 44 行，替换 `String.valueOf(maskChar).repeat(value.length())`：

```java
@Override
public String transform(String value, LogMessageContext context) {
    char[] chars = new char[value.length()];
    java.util.Arrays.fill(chars, maskChar);
    return new String(chars);
}
```

**Step 5: 修复 RegexDetector.java 的 String.isBlank()**

第 44 行：
- `if (p == null || p.isBlank()) {` → `if (p == null || p.trim().isEmpty()) {`

**Step 6: 修复 ReplaceTransformer.java 的 String.isBlank()**

第 43 行：
- `if (p == null || p.isBlank()) {` → `if (p == null || p.trim().isEmpty()) {`

**Step 7: 修复 RuleEngineLoader.java 的 String.isBlank()**

第 210 行：
- `if (detectorType == null || detectorType.isBlank()) {` → `if (detectorType == null || detectorType.trim().isEmpty()) {`

第 214 行：
- `if (transformerType == null || transformerType.isBlank()) {` → `if (transformerType == null || transformerType.trim().isEmpty()) {`

**Step 8: 修复 ProcessorChain.java 的 instanceof 模式匹配**

第 69 行，替换 `if (p instanceof ContextAware ca) {`：

```java
if (p instanceof ContextAware) {
    ((ContextAware) p).setContext(getContext());
}
```

**Step 9: 验证编译和测试**

Run: `mvn clean test -pl fist-kit-infra/fist-logback -am`
Expected: BUILD SUCCESS

**Step 10: Commit**

```bash
git add fist-kit-infra/fist-logback/
git commit -m "refactor(fist-logback): migrate to JSpecify and target JDK 8

- Replace Spring null-safety annotations with JSpecify @NullMarked
- Remove spring-core dependency (was declared but unused in code)
- Convert MatchSpan from record to regular class
- Replace String.repeat() with Arrays.fill()
- Replace String.isBlank() with trim().isEmpty()
- Replace instanceof pattern matching with traditional cast
- Set compiler release to 8 for main sources"
```

---

### Task 6: 全量构建验证

**Step 1: 清理并构建整个项目**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS（确保降级模块不影响依赖它们的上游模块编译）

**Step 2: 运行全量测试**

Run: `mvn clean test`
Expected: BUILD SUCCESS

**Step 3: 格式校验**

Run: `mvn spring-javaformat:validate`
Expected: BUILD SUCCESS

**Step 4: 确认字节码版本**

Run: `javap -verbose -cp fist-kit-infra/fist-kit-api/target/classes com.power4j.fist.boot.common.utils.MapKit | grep "major version"`
Expected: `major version: 52`（对应 JDK 8）

对其他三个模块也做同样检查，确认 main class 的 major version 为 52。

---

## 影响范围

以下模块依赖被迁移的模块，需确认编译和测试不受影响：

| 被迁移模块 | 依赖它的模块 |
|---|---|
| fist-kit-api | fist-redisson, fist-support-web, fist-boot-web-app, fist-boot-security, fist-support-security, fist-boot-data, fist-boot-apidoc |
| fist-jackson | fist-support-web |
| fist-mybatis-plus | fist-boot-crud-mybatis |
| fist-logback | 无直接依赖 |

JDK 8 字节码在 JDK 17 上完全兼容，上游模块无需任何改动。Task 6 的全量构建验证会覆盖这些模块。

## 注意事项

1. **logback-classic 为 provided scope**：fist-logback 不传递 logback-classic 依赖，使用方需自行引入。JDK 8 用户应使用 logback 1.2.x 或 1.3.x，JDK 11+ 用户可使用 1.4.x/1.5.x
2. **JSpecify 编译警告**：`warning: unknown enum constant ElementType.MODULE` 是预期行为，不影响功能
3. **Mockito 反射限制**：在 JDK 8 运行时对 `@NullMarked` 类执行 mock 时，可能需要 `MockSettings.withoutAnnotations()`
4. **上游隐式依赖风险**：移除 spring-core 后，依赖这些模块的上游模块若隐式使用了传递的 spring-core，会在全量编译时暴露。Task 6 覆盖此验证
5. **MatchSpan API 变更**：record → final class 是源码兼容的（accessor 方法名保持 `start()/end()`），但属于二进制级变更，已在 SNAPSHOT 阶段，影响可控
