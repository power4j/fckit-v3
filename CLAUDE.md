# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

FIST Kit v3 是一个基于 Spring Boot 3.x / Spring Cloud 的企业级基础框架库，以 Maven 多模块形式组织，发布到 Maven Central。

- JDK 17，Spring Boot 3.5.x，Spring Cloud 2025.0.x
- 当前版本：3.11-SNAPSHOT

## 模块结构

```
fist-kit-build/          # 构建配置与插件管理
fist-kit-dependencies/   # BOM 依赖管理
fist-kit-infra/          # 基础设施层（logback、jackson、mybatis-plus、redisson 等）
fist-kit-app/            # 应用层（web、data、security、apidoc）
fist-kit-cloud/          # 云集成层（gateway、feign RPC）
```

## 常用命令

```bash
# 编译
mvn clean compile

# 运行所有测试
mvn test

# 运行单个模块测试
mvn test -pl fist-kit-infra/fist-logback

# 运行单个测试类
mvn test -pl fist-kit-infra/fist-logback -Dtest=SensitiveConverterTest

# 完整构建（含测试）
mvn clean package

# 本地安装
./install-local.sh
```

## 代码规范

提交前必须通过 `spring-javaformat-maven-plugin` 格式检查：

```bash
mvn spring-javaformat:apply   # 自动格式化
mvn spring-javaformat:validate # 仅校验
```

## 测试框架

- JUnit 5 + Mockito（单元测试）
- Spock 2.4（Groovy 集成测试，文件名以 `Spec.groovy` 结尾）
- 测试工具模块：`fist-kit-infra/fist-support-test`

## 关键架构说明

- **依赖管理**：所有版本号统一在 `fist-kit-dependencies/pom.xml` 中声明，子模块不写版本号
- **自动配置**：应用层模块（`fist-boot-*`）遵循 Spring Boot starter 模式，通过 `spring.factories` 或 `AutoConfiguration.imports` 注册
- **分层原则**：`fist-kit-infra` 不依赖 `fist-kit-app`，`fist-kit-app` 不依赖 `fist-kit-cloud`
