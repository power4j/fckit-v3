package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.codec.SecureEnvelopeFieldMapping;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SDE 自动配置属性。
 * <p>
 * 该属性对象只描述 starter 装配所需的开关、策略与 envelope 字段映射，不隐式提供生产密钥或测试级组件。
 */
@ConfigurationProperties(prefix = "fist.sde")
public class SdeProperties {

	private boolean enabled;

	private final Web web = new Web();

	private final Client client = new Client();

	private final Map<String, Policy> policies = new LinkedHashMap<>();

	private final Map<String, SecureEnvelopeFieldMapping> envelopes = new LinkedHashMap<>();

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Web getWeb() {
		return this.web;
	}

	public Client getClient() {
		return this.client;
	}

	public Map<String, Policy> getPolicies() {
		return this.policies;
	}

	public Map<String, SecureEnvelopeFieldMapping> getEnvelopes() {
		return this.envelopes;
	}

	/**
	 * 服务端 Spring MVC 接入配置。
	 */
	public static class Web {

		private boolean enabled;

		private String defaultPolicyId = "default";

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getDefaultPolicyId() {
			return this.defaultPolicyId;
		}

		public void setDefaultPolicyId(String defaultPolicyId) {
			this.defaultPolicyId = defaultPolicyId;
		}

	}

	/**
	 * HTTP 客户端接入配置。
	 */
	public static class Client {

		private boolean enabled;

		private String defaultPolicyId;

		private String defaultKeyRef;

		private boolean logPayload;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getDefaultPolicyId() {
			return this.defaultPolicyId;
		}

		public void setDefaultPolicyId(String defaultPolicyId) {
			this.defaultPolicyId = defaultPolicyId;
		}

		public String getDefaultKeyRef() {
			return this.defaultKeyRef;
		}

		public void setDefaultKeyRef(String defaultKeyRef) {
			this.defaultKeyRef = defaultKeyRef;
		}

		public boolean isLogPayload() {
			return this.logPayload;
		}

		public void setLogPayload(boolean logPayload) {
			this.logPayload = logPayload;
		}

	}

	/**
	 * 单个 SDE 策略配置。
	 */
	public static class Policy {

		private SecureInputMode requestBodyMode = SecureInputMode.DISABLED;

		private SecureResponseMode responseBodyMode = SecureResponseMode.DISABLED;

		private boolean cryptoEnabled = true;

		private boolean signatureEnabled = true;

		private String cryptoHandler = "aesGcmCryptoHandler";

		private String signatureHandler = "hmacSha256SignatureHandler";

		private String keyResolver = "staticSecureKeyResolver";

		private String nonceGenerator = "secureRandomNonceGenerator";

		private String replayGuard = "replayGuard";

		private String envelope = "default";

		private Duration timestampWindow = Duration.ofMinutes(5);

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

		public String getCryptoHandler() {
			return this.cryptoHandler;
		}

		public void setCryptoHandler(String cryptoHandler) {
			this.cryptoHandler = cryptoHandler;
		}

		public String getSignatureHandler() {
			return this.signatureHandler;
		}

		public void setSignatureHandler(String signatureHandler) {
			this.signatureHandler = signatureHandler;
		}

		public String getKeyResolver() {
			return this.keyResolver;
		}

		public void setKeyResolver(String keyResolver) {
			this.keyResolver = keyResolver;
		}

		public String getNonceGenerator() {
			return this.nonceGenerator;
		}

		public void setNonceGenerator(String nonceGenerator) {
			this.nonceGenerator = nonceGenerator;
		}

		public String getReplayGuard() {
			return this.replayGuard;
		}

		public void setReplayGuard(String replayGuard) {
			this.replayGuard = replayGuard;
		}

		public String getEnvelope() {
			return this.envelope;
		}

		public void setEnvelope(String envelope) {
			this.envelope = envelope;
		}

		public Duration getTimestampWindow() {
			return this.timestampWindow;
		}

		public void setTimestampWindow(Duration timestampWindow) {
			this.timestampWindow = timestampWindow;
		}

	}

}
