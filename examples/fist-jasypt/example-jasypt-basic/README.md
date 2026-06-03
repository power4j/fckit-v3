# `example-jasypt-basic`

本示例演示 FIST 国密配置加密的最小接入流程。

## 生成主密钥文件

查看 CLI 命令和参数：

```bash
java -jar fist-jasypt-cli-3.14.0-SNAPSHOT.jar help
```

```bash
java -jar fist-jasypt-cli-3.14.0-SNAPSHOT.jar generate-key --bytes 32
```

将命令输出写入受控文件，例如 `D:/secure/fist-jasypt-master.key`。该文件不写入 Git，不在日志中打印。

## 生成配置密文

默认密文格式为 `GMENC(...)`：

```bash
java -jar fist-jasypt-cli-3.14.0-SNAPSHOT.jar encrypt --master-key-file D:/secure/fist-jasypt-master.key --value hmac-secret
```

如应用配置了自定义密文边界，CLI 需要使用相同的 `--prefix` 和 `--suffix`：

```bash
java -jar fist-jasypt-cli-3.14.0-SNAPSHOT.jar encrypt --master-key-file D:/secure/fist-jasypt-master.key --value hmac-secret --prefix "ENC[" --suffix "]"
```

将输出的 `GMENC(...)` 写入 `application.yml`：

```yaml
demo:
  hmac-key: GMENC(v1:...)
```

## 启动示例

```bash
mvnd -pl examples/fist-jasypt/example-jasypt-basic -am spring-boot:run -Dspring-boot.run.arguments=--fist.jasypt.master-key-file=D:/secure/fist-jasypt-master.key
```

启动日志会输出 `demo.hmac-key.length`，不会输出 HMAC 明文密钥。
