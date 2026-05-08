# operations-log

## 2026-05-08

- 工具：`rg --files docs\local\plans\20260508-sde`  
  摘要：确认目录包含 `fist-sde-technical-solution-1.1.md` 和 `fist-sde-technical-solution-1.1-review.md`。
- 工具：`Get-Content`  
  摘要：读取 1.1 方案、评审报告和关键段落。
- 工具：`rg --files D:\git-repo\mica\mica`  
  摘要：定位 `mica-api-encrypt` 模块源码。
- 工具：`Get-Content`  
  摘要：读取 `ApiDecryptRequestBodyAdvice`、`ApiEncryptResponseBodyAdvice`、`ApiDecryptParamResolver`、`ApiEncryptConfiguration`、`ApiEncryptParamConfiguration`、`ApiEncryptProperties`、`ApiCryptoUtil`、`build.gradle` 和 `MODULE.md`。
- 决策：1.2 方案删除 Query 能力，保留 Body 和 Response Body；Feign 先做技术验证；Spring MVC 集成借鉴 mica 的 Advice 扩展点，但不采用 `mica-auto` 和 URL 参数解密。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.2.md`。
- 工具：`Get-Content`、`rg`  
  摘要：读取 `fist-sde-technical-solution-1.2-review.md` 和 1.2 方案关键段落，核对评审意见。
- 决策：1.3 删除 `payloadDigest`，改为直接签 `payload`；`keyId` 改为 `keyRef`；`policy` 改为 `policyId`；`SecureMessageCodec` 改为 `SecureEnvelopeCodec`。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.3.md`。
- 工具：`Get-Content`、`rg`  
  摘要：读取 `fist-sde-technical-solution-1.3-review.md`，核对 1.3 评审新增的 5 条建议。
- 决策：1.4 只收敛 1.3 评审新增建议，不改变 1.3 主体架构；清理旧版本方案和评审报告，只保留 1.4 方案。
- 产物：新增 `docs/local/plans/20260508-sde/fist-sde-technical-solution-1.4.md`，删除 1.1、1.2、1.3 旧文档和评审报告。
