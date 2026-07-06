# fist-jasypt-cli

`fist-jasypt-cli` 是 `fist-jasypt` 的命令行工具，用于生成配置密钥、加密和解密 `GMENC(...)` 配置值，以及查看密钥指纹。


## 主密钥来源

`encrypt`、`decrypt`、`fingerprint` 和使用 `--encrypted-value` 的 `hmac-key-fingerprint` 需要主密钥。

主密钥可以通过以下方式提供：

| 方式 | 参数 |
| --- | --- |
| 主密钥文件 | `--master-key-file <path>` |
| 环境变量 | `--master-key-env <name>` |
| 默认环境变量 | `FIST_JASYPT_MASTER_KEY` |

当同时存在主密钥文件和环境变量时，优先读取 `--master-key-file`。

## 命令列表

| 命令 | 用途 |
| --- | --- |
| `generate-key` | 生成 Base64 随机密钥 |
| `encrypt` | 将明文配置值加密为 `GMENC(...)` |
| `decrypt` | 将 `GMENC(...)` 解密为明文配置值 |
| `fingerprint` | 输出 master-key 指纹 |
| `hmac-key-fingerprint` | 输出 HMAC 业务密钥指纹 |
| `help` | 输出帮助信息 |

## generate-key

生成标准 Base64 随机密钥，默认长度为 32 字节。

```powershell
java -jar target/fist-jasypt-cli.jar generate-key
```

指定随机字节数：

```powershell
java -jar target/fist-jasypt-cli.jar generate-key --bytes 32
```

`--bytes` 必须大于或等于 `16`。

## encrypt

使用主密钥将明文配置值加密为 `GMENC(...)`。

```powershell
java -jar target/fist-jasypt-cli.jar encrypt `
  --master-key-file .\master.key `
  --value hmac-secret
```

输出示例：

```text
GMENC(v1:...)
```

自定义密文边界：

```powershell
java -jar target/fist-jasypt-cli.jar encrypt `
  --master-key-file .\master.key `
  --value hmac-secret `
  --prefix "ENC[" `
  --suffix "]"
```

## decrypt

使用主密钥解密 `GMENC(...)` 配置值。

```powershell
java -jar target/fist-jasypt-cli.jar decrypt `
  --master-key-file .\master.key `
  --value "GMENC(v1:...)"
```

如果加密时使用了自定义边界，解密时需要传入相同的 `--prefix` 和 `--suffix`。

## fingerprint

输出 master-key 指纹，用于识别当前主密钥。该命令不用于核对 HMAC 业务密钥。

```powershell
java -jar target/fist-jasypt-cli.jar fingerprint `
  --master-key-file .\master.key
```

输出格式：

```text
SM3:<Base64>
```

## hmac-key-fingerprint

输出 HMAC 业务密钥指纹，用于和应用完整性运维页面中的 `keyFingerprint` 字段核对。

输出格式与应用页面一致：

```text
sm3:<16位小写hex>
```

该命令的计算规则：

1. 获取 HMAC 业务密钥的 Base64 文本。
2. 对 Base64 文本解码，得到密钥字节。
3. 对密钥字节计算 SM3。
4. 截取前 8 字节。
5. 转为 16 位小写 hex，并添加 `sm3:` 前缀。

### 使用明文 Base64 密钥

```powershell
java -jar target/fist-jasypt-cli.jar hmac-key-fingerprint `
  --value "Base64EncodedHmacKey"
```

### 使用 `GMENC(...)` 密文

当配置文件中保存的是加密后的 HMAC 业务密钥时，先用主密钥解密，再计算业务密钥指纹：

```powershell
java -jar target/fist-jasypt-cli.jar hmac-key-fingerprint `
  --master-key-file .\master.key `
  --encrypted-value "GMENC(v1:...)"
```

`--value` 和 `--encrypted-value` 不能同时使用。

非法 Base64 输入会返回明确异常：

```text
HMAC key Base64 is invalid.
```

## help

查看命令帮助：

```powershell
java -jar target/fist-jasypt-cli.jar help
java -jar target/fist-jasypt-cli.jar --help
java -jar target/fist-jasypt-cli.jar -h
```
