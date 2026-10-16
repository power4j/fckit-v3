# Jasypt 主密钥来源

`fist-jasypt-spring-boot-starter` 在启用 `fist.jasypt.enabled=true` 后加载主密钥。主密钥用于解密 `GMENC(...)` 配置，不等同于密文中保存的 HMAC 业务密钥。

## 配置契约

| 配置项 | 默认值 | 含义 |
| --- | --- | --- |
| `fist.jasypt.master-key-source` | 未配置 | 可选，显式指定时仅接受 `file` 或 `raw` |
| `fist.jasypt.master-key-value` | 未配置 | `raw` 模式使用的主密钥文本 |
| `fist.jasypt.master-key-file` | 未配置 | 主密钥文件路径 |
| `fist.jasypt.master-key-env` | `FIST_JASYPT_MASTER_KEY` | 旧加载逻辑中读取的 Spring Environment 属性名，可来自环境变量或其他属性源 |

- 未配置 `master-key-source` 时，完整保留旧行为：`master-key-file` 非空则读取文件，否则读取 `master-key-env` 指定的属性。此时不使用 `master-key-value`。
- 显式 `file` 仅读取文件；显式 `raw` 仅读取 `master-key-value`。文件路径与直接值可以共存，未选中的来源不参与密钥解析。
- 来源指定错误或显式设置为空字符串、空白字符串时启动失败。选中来源缺失、为空或文件不可读时启动失败，不回退到其他来源。
- 文件按 UTF-8 读取后执行 `trim()`；`raw` 使用相同的文本和 `trim()` 语义，不要求 Base64，也不增加口令派生规则。相同主密钥可以继续解密已有密文。

## 跨平台共享配置

Linux 和 Windows 实例可以从 Nacos 加载同一配置，无需在各实例提供主密钥文件路径：

```yaml
fist:
  jasypt:
    enabled: true
    master-key-source: raw
    master-key-value: "替换为实际主密钥文本"
```

`master-key-value` 必须在加密器初始化前可直接读取，不能使用依赖该主密钥的 `GMENC(...)` 或自定义密文边界包裹。配置中心需要在应用初始化加密器前完成加载。本功能不改变 Nacos 的接入方式，也不增加密钥热切换能力；修改主密钥来源或内容后应重启实例。

选择 `raw` 表示允许配置读取者获取主密钥。如果密文也保存在同一配置中心，拥有相应读取权限的人能够解密它；是否采用该方式由使用者决定。不要在日志中打印主密钥，也不要将真实密钥提交到代码仓库。

访问同一数据库的实例应使用兼容的主密钥与 HMAC 业务密钥配置。切换来源时保留原主密钥文本，避免误把切换来源当作更换密钥。

## 保留文件方式

现有配置无需修改。需要显式限制为文件来源时，可以增加选择器：

```yaml
fist:
  jasypt:
    enabled: true
    master-key-source: file
    master-key-file: /run/secrets/fist-jasypt-master.key
```

生成密钥、生成密文和运行示例见 [基础示例](../../../examples/fist-jasypt/example-jasypt-basic/README.md)。
