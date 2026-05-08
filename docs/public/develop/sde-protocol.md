# fist-sde 协议规范

状态：首阶段协议约定

日期：2026-05-08

本文档定义 `fist-sde` secure envelope 的跨语言调用规则。内容面向需要自行生成、验签、加密、解密 SDE 报文的服务端或客户端实现。

首阶段只覆盖 Request Body 和 Response Body，不覆盖 Query 加密、Query 签名和正式 Feign 集成。

## 协议范围

`fist-sde` 使用 secure envelope 承载加密或签名后的业务 payload。发送端负责生成 envelope、加密 payload、生成签名；接收端负责解析 envelope、验签、重放校验和解密。

首阶段支持两类报文域：

| `scope` | 说明 |
| --- | --- |
| `body` | HTTP Request Body。 |
| `responseBody` | HTTP Response Body。 |

`multipart/form-data` 不进入 SDE Body 处理流程。

## Envelope 字段

默认 JSON 字段如下。接入方可通过 envelope 字段映射配置调整物理字段名，但签名输入始终使用逻辑字段名。

| 逻辑字段名 | 默认 JSON 字段 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `version` | `version` | 是 | Envelope 协议格式版本，只表示字段结构、canonicalization 规则和时间格式等协议层版本。 |
| `scope` | `scope` | 是 | 报文域。首阶段为 `body` 或 `responseBody`。 |
| `payload` | `data` | 是 | 加密模式下为密文字符串；sign-only 模式下为明文 JSON 字符串。 |
| `signature` | `sign` | 签名启用时必填 | 对 canonical text 的签名结果。 |
| `timestamp` | `timestamp` | 是 | ISO-8601 时间字符串。 |
| `nonce` | `nonce` | 是 | 同一 `keyRef + policyId + scope` 下不得重复。 |
| `keyRef` | `keyRef` | 是 | 密钥引用，由接收端 `SecureKeyResolver` 定位密钥材料。 |
| `algorithm` | `alg` | 否 | 算法提示。算法可完全由本地 `policyId` 固定。 |
| `policyId` | `policyId` | 否 | 安全处理策略 ID。出现时必须与接收端当前策略匹配。 |
| `metadata` | `meta` | 否 | 扩展元数据。出现时参与签名。 |

字段语义约束：

- `keyRef` 只表示密钥引用，不承载密钥材料。
- `policyId` 指向本地安全处理策略，不表示接口资源名。
- `version` 不表示算法版本或密钥版本。
- `algorithm`、`policyId`、`metadata` 如果出现在 envelope 中，必须参与签名。

## Payload 编码

发送端先将业务对象序列化为 UTF-8 JSON 字节，再按策略处理：

| 策略组合 | `payload` 内容 |
| --- | --- |
| `cryptoEnabled=true` 且 `signatureEnabled=true` | 加密后的密文字符串。 |
| `cryptoEnabled=false` 且 `signatureEnabled=true` | 原始 JSON 字符串。 |
| `cryptoEnabled=true` 且 `signatureEnabled=false` | 不允许。 |
| `cryptoEnabled=false` 且 `signatureEnabled=false` | 仅在 request / response 模式均为 `DISABLED` 或 `PLAIN` 时允许。 |

签名输入直接包含规范化后的 `payload` 字段值，不使用 `payloadDigest`。

## Canonical Text

签名输入由 `SignatureCanonicalizer` 生成，规则如下：

1. 使用逻辑字段名，不使用映射后的 JSON 字段名。
2. 字段按逻辑字段名的 ASCII 字典序排序。
3. `version`、`scope`、`payload`、`timestamp`、`nonce`、`keyRef` 为基础字段。
4. `algorithm`、`policyId`、`metadata` 存在且非空时加入。
5. `signature` 不参与签名输入。
6. 空字符串和 `null` 视为不存在，不参与签名输入。
7. 字段值不得包含换行符。

Canonical text 使用 UTF-8 编码。每行格式为：

```text
fieldName=fieldValue
```

示例：

```text
keyRef=tenant-a.sde.2026-05
nonce=2Hh3u1S7m5
payload=BASE64URL_CIPHERTEXT
policyId=body-strict-v1
scope=body
timestamp=2026-05-08T09:00:00Z
version=1
```

## 签名与验签伪代码

发送端：

```text
plain = jsonSerialize(body)
payload = cryptoEnabled ? encrypt(plain, keyRef, policyId, scope) : utf8String(plain)
envelope = {
  version,
  scope,
  payload,
  timestamp,
  nonce,
  keyRef,
  policyId
}
canonicalText = canonicalize(envelope)
envelope.signature = sign(canonicalText, keyRef, policyId, scope)
```

接收端：

```text
envelope = decodeEnvelope(input)
assert envelope.scope == expectedScope
assert envelope.policyId is empty or envelope.policyId == currentPolicyId
canonicalText = canonicalize(envelope)
verify(canonicalText, envelope.signature, envelope.keyRef, currentPolicyId, expectedScope)
checkReplay(envelope.keyRef, currentPolicyId, expectedScope, envelope.nonce, envelope.timestamp)
plain = cryptoEnabled ? decrypt(envelope.payload, envelope.keyRef, currentPolicyId, expectedScope) : utf8Bytes(envelope.payload)
body = jsonDeserialize(plain)
```

## Request Body 示例

默认字段映射下的 Request Body envelope：

```json
{
  "version": "1",
  "scope": "body",
  "data": "BASE64URL_CIPHERTEXT",
  "sign": "BASE64URL_SIGNATURE",
  "timestamp": "2026-05-08T09:00:00Z",
  "nonce": "2Hh3u1S7m5",
  "keyRef": "tenant-a.sde.2026-05",
  "policyId": "body-strict-v1"
}
```

服务端处理顺序：

1. 使用 `SecureEnvelopeCodec` 解析 envelope。
2. 校验 `scope=body`。
3. 生成 canonical text 并验签。
4. 校验 timestamp 和 nonce。
5. 解密或读取明文 payload。
6. 将明文 JSON 交给 Spring MVC 原有 `HttpMessageConverter`。

## Response Body 示例

默认字段映射下的 Response Body envelope：

```json
{
  "version": "1",
  "scope": "responseBody",
  "data": "BASE64URL_CIPHERTEXT",
  "sign": "BASE64URL_SIGNATURE",
  "timestamp": "2026-05-08T09:00:01Z",
  "nonce": "Dx7b2xM0nQ",
  "keyRef": "tenant-a.sde.2026-05",
  "policyId": "body-strict-v1"
}
```

`null` 响应默认不封装。异常响应默认不封装，由接入应用的统一异常处理器接管。

## 错误排查

| 现象 | 优先检查项 |
| --- | --- |
| 策略不存在 | `policyId` 是否已在接收端配置，注解选择的策略 ID 是否正确。 |
| 密钥解析失败 | `keyRef` 是否存在，`SecureKeyResolver` 是否按 usage 返回正确密钥材料。 |
| 签名失败 | canonical text 字段顺序、字段名映射、payload 原文、字符集和签名密钥是否一致。 |
| 解密失败 | 加密算法、IV / tag 编码、密钥材料和 payload 编码是否一致。 |
| 时间戳过期 | 双方时钟和策略中的 timestamp window 是否符合预期。 |
| 重放请求 | `keyRef + policyId + scope + nonce` 是否重复。 |
| Provider 缺失 | 使用 SM4 或 HMAC-SM3 时，是否显式引入并注册对应 JCA Provider。 |
| Nonce 生成失败 | `NonceGenerator` Bean 是否存在，随机源是否可用。 |

## 首阶段边界

- 不支持 Query 加密和 Query 签名。
- 不交付正式 Feign 生产代码；Feign 当前仅完成技术验证。
- `fist-sde-boot-starter` 不自动引入 `fist-sde-extra` 的算法实现、测试级 `ReplayGuard` 或静态密钥解析器。
- 生产应用必须显式提供 `CryptoHandler`、`SignatureHandler`、`SecureKeyResolver`、`NonceGenerator` 和适合生产环境的 `ReplayGuard`。
