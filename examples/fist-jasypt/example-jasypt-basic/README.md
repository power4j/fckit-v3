# `example-jasypt-basic`

本示例默认使用 `raw` 主密钥，演示 `GMENC(...)` 配置解密。配置规则及兼容行为见 [Jasypt 主密钥来源](../../../docs/public/develop/jasypt-master-key.md)。

`application.yml` 中的 `example-only-master-key`、配套密文和 [demo-master.key](demo-master.key) 均为公开演示数据，不能用于真实环境。两种来源使用相同主密钥，密文解密结果均为 `hmac-secret`。

## 构建和启动

在仓库根目录构建示例及依赖：

```bash
mvnd -B -ntp -Pexamples -pl examples/fist-jasypt/example-jasypt-basic -am package
```

进入示例目录并启动。显式读取源码配置文件，后续切换注释只需重新启动，无需重新打包：

```bash
cd examples/fist-jasypt/example-jasypt-basic
java -jar target/example-jasypt-basic-3.16.0-SNAPSHOT.jar --spring.config.location=file:./src/main/resources/application.yml
```

上述命令可在 Linux shell 和 Windows PowerShell 中执行。版本升级后，使用 `target` 下对应版本的 jar 文件名。

启动成功后输出 `demo.hmac-key.length=11`，不会输出主密钥或 HMAC 明文。示例没有 Web 服务，输出后正常退出。

## 切换主密钥来源

编辑 [application.yml](src/main/resources/application.yml)。默认启用：

```yaml
master-key-source: raw
master-key-value: example-only-master-key
```

将这两行注释，取消下面两行的注释：

```yaml
master-key-source: file
master-key-file: ./demo-master.key
```

再次执行启动命令，仍应输出 `demo.hmac-key.length=11`。反向切换注释即可恢复 `raw`。同一时间只启用一组配置，避免重复的 `master-key-source` 键；文件路径相对于进程工作目录，因此应从示例目录启动。

## 使用自己的密钥和密文

使用 [fist-jasypt-cli](../../../fist-kit-infra/fist-jasypt/fist-jasypt-cli/README.md) 生成主密钥，并将输出保存到本地受控文件：

```bash
java -jar fist-jasypt-cli.jar generate-key --bytes 32
java -jar fist-jasypt-cli.jar encrypt --master-key-file /path/to/master.key --value hmac-secret
```

将生成的密文写入 `demo.hmac-key`。`raw` 方式将对应文件中的主密钥文本写入 `master-key-value`；文件方式将 `master-key-file` 指向该文件。真实密钥不提交到代码仓库。

如配置了自定义密文边界，CLI 加密时需要使用相同的 `--prefix` 和 `--suffix`。
