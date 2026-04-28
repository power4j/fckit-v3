# AGENTS.md - FIST Kit v3 工作手册

本文件定义在 FIST Kit v3 仓库中的工程约束、文档规则和交付标准。作用范围为当前目录及其子目录；如子目录存在独立 `AGENTS.md`，以更近的文件为准。

## 1. 项目定位

FIST Kit v3 是基于 Spring Boot 3.x / Spring Cloud 的 Java 基础框架库，以 Maven 多模块形式组织，并发布到 Maven Central。

当前仓库事实：

- Maven 坐标前缀：`com.power4j.fist3`
- JDK：`17`
- Spring Boot：`3.5.x`
- Spring Cloud：`2025.0.x`
- 根版本号来源：根 `pom.xml` 的 `<revision>`
- 发布形态：Maven Central 组件、BOM、starter、基础设施工具包、示例与基准测试

本项目不是 TypeScript SDK 项目，不使用 `pnpm`、`tsup`、`Vitest`、ESLint 或 Prettier 作为主构建链路。

## 2. 权威资料

处理需求时按以下顺序确认约束：

1. 当前目录或更近目录的 `AGENTS.md`
2. 根 `pom.xml`、子模块 `pom.xml` 和实际源码
3. `CLAUDE.md`
4. `docs/public/` 下的正式文档
5. `docs/plans/` 与 `docs/local/` 下的计划和过程记录
6. `README.md`、模块 `README.md`、示例和基准测试说明

如文档与代码不一致，先以可编译源码、测试和 `pom.xml` 为准，再更新或标注过时文档。

## 3. 技术栈约束

优先使用项目既有技术栈：

- 构建：Maven Wrapper，优先使用 `./mvnw` 或 `.\mvnw.cmd`
- 语言：Java 17，必要时使用 Groovy 测试
- 框架：Spring Boot 3.5、Spring Cloud 2025、Spring Cloud Alibaba
- 测试：JUnit 5、Mockito、Spock 2.4
- 格式：`spring-javaformat-maven-plugin`
- 覆盖率与质量工具：沿用根 `pom.xml` 已配置插件
- 示例：`examples/`
- 基准测试：`benchmarks/`

禁止为通用能力新增自研构建器、测试框架、格式化器或依赖管理方式。新增依赖前必须确认现有模块、Spring 官方能力或成熟生态库无法满足需求。

## 4. 模块边界

根模块：

```text
fist-kit-build/          构建配置与插件管理
fist-kit-dependencies/   BOM 与依赖版本管理
fist-kit-infra/          基础设施层
fist-kit-app/            应用层能力
fist-kit-cloud/          云集成能力
examples/                示例工程
benchmarks/              基准测试工程
```

分层规则：

- `fist-kit-infra` 不依赖 `fist-kit-app` 或 `fist-kit-cloud`。
- `fist-kit-app` 不依赖 `fist-kit-cloud`。
- `fist-kit-dependencies` 统一管理依赖版本，子模块原则上不重复声明版本号。
- `fist-kit-build` 管理构建插件和公共构建约定，不承载业务代码。
- `examples` 与 `benchmarks` 不作为发布组件维护，除非发布文档另有说明。

## 5. 开发流程

实施前先阅读相关模块的源码、测试和 `pom.xml`，至少确认：

- 变更属于哪个模块或父子模块组合。
- 是否已有相似实现、测试夹具或自动配置模式。
- 变更是否影响公共 API、Maven 坐标、自动配置注册、示例或文档。
- 应执行哪些本地验证命令。

复杂任务应先在 `docs/plans/` 或现有任务文档中记录计划。局部修复可直接实施，但仍需在交付说明中说明验证结果。

实现时保持小步修改：

- 优先修复已确认缺陷，再扩展新能力。
- 优先复用项目内已有抽象和 Spring 生态能力。
- 不为尚未出现的复用需求提前抽象。
- 不混入无关重构、格式化或依赖升级。
- 不回退其他人员或其他任务产生的改动。

## 6. Java 代码规范

- 遵循 Spring Java Format。
- 包名、类名和模块命名保持现有风格。
- 源码注释使用中文，只解释意图、约束或非显然行为。
- 禁止无意义注释、尾注释和装饰性符号。
- 公共 API 需要稳定、清晰，并尽量减少暴露面。
- starter 和自动配置类必须遵循 Spring Boot 3 的自动配置注册方式。
- 同时维护 `spring.factories` 与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 时，不得破坏旧配置的加载预期。
- 涉及日志脱敏、认证、授权、租户、网关、数据访问等能力时，必须保留既有安全语义和兼容约束。

## 7. 测试与验证

新增或修复代码必须补充相应本地测试。优先选择与变更最接近的模块级验证，必要时再执行全量验证。

常用命令：

```bash
./mvnw test
./mvnw -pl fist-kit-infra/fist-logback test
./mvnw -pl fist-kit-infra/fist-logback -Dtest=SensitiveConverterTest test
./mvnw spring-javaformat:validate
./mvnw spring-javaformat:apply
./mvnw clean package
```

Windows 环境使用：

```powershell
.\mvnw.cmd test
.\mvnw.cmd -pl fist-kit-infra/fist-logback test
.\mvnw.cmd spring-javaformat:validate
.\mvnw.cmd clean package
```

验证规则：

- Java 单元测试使用 JUnit 5、Mockito 或项目既有测试工具。
- Groovy 测试使用 Spock，文件命名沿用项目现有约定。
- 修改自动配置、starter、公共 API、错误处理或跨模块依赖时，应覆盖正常路径和失败分支。
- 修改格式检查范围内文件后，提交前必须运行 `spring-javaformat:validate`；如失败，先运行 `spring-javaformat:apply` 或等价格式化命令。
- 如某个验证命令无法执行，交付说明必须写明原因、已完成的替代验证和剩余风险。

## 8. 文档规范

文档优先放入现有目录：

- `docs/public/`：正式文档，面向使用者与协作者。
- `docs/plans/`：入库计划、迁移方案和可追溯任务记录。
- `docs/local/`：本地过程记录，适合临时分析和未定稿内容。
- 模块 `README.md`：模块入口说明和快速使用说明。

写作规则：

- 使用 Markdown。
- 中文技术文档采用克制、准确、可扫读的表达方式。
- 中文与英文、数字之间保留必要空格。
- 代码字面量、JSON 键名、API 路径、Maven 坐标和命令保持原样。
- 跨文档信息优先链接，不复制大段相同内容。
- 新增、移动或归档文档后，同步更新相关索引或入口说明。
- 不在源码、文档、配置、测试、示例、提交信息和变更日志中写入生成工具署名。

## 9. CHANGELOG 规则

本项目使用根目录 `CHANGELOG.md` 记录用户可见变更。该文件采用 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/) 格式，并遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

强制规则：

- 用户可见变更必须先写入根目录 `CHANGELOG.md` 的 `## [Unreleased]` 区段。
- 变更日志写给使用者，不堆砌 Git 提交记录。
- 每个已发布版本使用 `## [x.y.z] - YYYY-MM-DD`，日期使用 ISO 8601 格式。
- 版本号使用 `MAJOR.MINOR.PATCH`。不兼容公共 API 变更递增主版本，向下兼容功能新增递增次版本，向下兼容问题修正递增修订版本。
- 允许的分类为 `Added`、`Changed`、`Deprecated`、`Removed`、`Fixed`、`Security`。没有内容的分类不必保留。
- 发布时将 `Unreleased` 内容移动到新版本区段，并重新创建空的 `Unreleased` 区段。
- 纯内部重构、测试调整、格式化和不影响使用者的构建维护，默认不写入变更日志。

## 10. Git 与发布

- 提交前查看 `git status` 与相关 diff。
- 提交信息使用英文，保持简短、具体。
- 不把无关改动混入同一提交。
- 未经明确确认，不推送远程分支、tag 或发布制品。
- 远程 push、merge、打 tag 归类为发布动作。
- Maven Central 上线归类为部署动作。
- 发布流程以 `docs/public/develop/release-process.md` 和根目录发布脚本为准。

## 11. 交付标准

任务完成时需要说明：

- 修改了哪些文件。
- 完成了哪些目标。
- 执行了哪些验证命令及结果。
- 是否存在未验证项或剩余风险。

只有在本地验证完成，或明确说明无法验证的原因与风险后，才能声明任务完成。
