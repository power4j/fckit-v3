package com.power4j.fist.sde.core;

import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * 描述一次 SDE 交换使用的策略配置。
 * <p>
 * 策略负责绑定请求/响应模式、算法处理器名称、密钥解析器、nonce 生成器、重放校验器和 envelope 编码配置。
 */
public class SecurePolicy {

	private String id;

	private SecureInputMode requestBodyMode = SecureInputMode.DISABLED;

	private SecureResponseMode responseBodyMode = SecureResponseMode.DISABLED;

	private boolean cryptoEnabled = true;

	private boolean signatureEnabled = true;

	private String cryptoHandlerName = "aesGcmCryptoHandler";

	private String signatureHandlerName = "hmacSha256SignatureHandler";

	private String keyResolverName = "staticSecureKeyResolver";

	private String nonceGeneratorName = "secureRandomNonceGenerator";

	private String replayGuardName = "replayGuard";

	private String envelopeName = "default";

	private Duration timestampWindow = Duration.ofMinutes(5);

	public @Nullable String getId() {
		return this.id;
	}

	public void setId(@Nullable String id) {
		this.id = id;
	}

	public SecureInputMode getRequestBodyMode() {
		return this.requestBodyMode;
	}

	public void setRequestBodyMode(SecureInputMode requestBodyMode) {
		this.requestBodyMode = requestBodyMode;
	}

	public SecureResponseMode getResponseBodyMode() {
		return this.responseBodyMode;
	}

	public void setResponseBodyMode(SecureResponseMode responseBodyMode) {
		this.responseBodyMode = responseBodyMode;
	}

	public boolean isCryptoEnabled() {
		return this.cryptoEnabled;
	}

	public void setCryptoEnabled(boolean cryptoEnabled) {
		this.cryptoEnabled = cryptoEnabled;
	}

	public boolean isSignatureEnabled() {
		return this.signatureEnabled;
	}

	public void setSignatureEnabled(boolean signatureEnabled) {
		this.signatureEnabled = signatureEnabled;
	}

	public String getCryptoHandlerName() {
		return this.cryptoHandlerName;
	}

	public void setCryptoHandlerName(String cryptoHandlerName) {
		this.cryptoHandlerName = cryptoHandlerName;
	}

	public String getSignatureHandlerName() {
		return this.signatureHandlerName;
	}

	public void setSignatureHandlerName(String signatureHandlerName) {
		this.signatureHandlerName = signatureHandlerName;
	}

	public String getKeyResolverName() {
		return this.keyResolverName;
	}

	public void setKeyResolverName(String keyResolverName) {
		this.keyResolverName = keyResolverName;
	}

	public String getNonceGeneratorName() {
		return this.nonceGeneratorName;
	}

	public void setNonceGeneratorName(String nonceGeneratorName) {
		this.nonceGeneratorName = nonceGeneratorName;
	}

	public String getReplayGuardName() {
		return this.replayGuardName;
	}

	public void setReplayGuardName(String replayGuardName) {
		this.replayGuardName = replayGuardName;
	}

	public String getEnvelopeName() {
		return this.envelopeName;
	}

	public void setEnvelopeName(String envelopeName) {
		this.envelopeName = envelopeName;
	}

	public Duration getTimestampWindow() {
		return this.timestampWindow;
	}

	public void setTimestampWindow(Duration timestampWindow) {
		this.timestampWindow = timestampWindow;
	}

}
