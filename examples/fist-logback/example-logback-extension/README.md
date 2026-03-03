# example-logback-extension

演示通过 SPI 机制扩展 fist-logback，实现自定义的身份证号检测器和脱敏转换器。

## 扩展点

- `IdCardDetector` / `IdCardDetectorProvider` — 自定义 Detector，识别 18 位身份证号
- `IdCardTransformer` / `IdCardTransformerProvider` — 自定义 Transformer，保留前后各 3 位

## 运行

```bash
mvn spring-boot:run -Pexamples -pl examples/fist-logback/example-logback-extension
```
