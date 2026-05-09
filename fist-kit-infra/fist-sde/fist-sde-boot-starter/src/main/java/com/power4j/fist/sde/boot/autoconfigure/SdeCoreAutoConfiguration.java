package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.SimpleSecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.codec.SecureEnvelopeFieldMapping;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SdeProperties.class)
@ConditionalOnProperty(prefix = "fist.sde", name = "enabled", havingValue = "true")
public class SdeCoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SignatureCanonicalizer signatureCanonicalizer() {
		return new DefaultSignatureCanonicalizer();
	}

	@Bean
	@ConditionalOnMissingBean
	public SecurePolicyRegistry securePolicyRegistry(SdeProperties properties) {
		Map<String, SecurePolicy> policies = new LinkedHashMap<>();
		for (Map.Entry<String, SdeProperties.Policy> entry : properties.getPolicies().entrySet()) {
			policies.put(entry.getKey(), toPolicy(entry.getKey(), entry.getValue()));
		}
		if (!policies.containsKey(properties.getWeb().getDefaultPolicyId())) {
			SecurePolicy policy = new SecurePolicy();
			policy.setId(properties.getWeb().getDefaultPolicyId());
			policies.put(policy.getId(), policy);
		}
		Map<String, SecureEnvelopeContext> envelopes = new LinkedHashMap<>();
		for (Map.Entry<String, SecureEnvelopeFieldMapping> entry : properties.getEnvelopes().entrySet()) {
			envelopes.put(entry.getKey(), new SecureEnvelopeContext(entry.getKey(), entry.getValue(),
					StandardCharsets.UTF_8, "application/json", null, null));
		}
		envelopes.putIfAbsent("default", SecureEnvelopeContext.defaults());
		return new SimpleSecurePolicyRegistry(properties.getWeb().getDefaultPolicyId(), policies, envelopes);
	}

	private SecurePolicy toPolicy(String id, SdeProperties.Policy source) {
		SecurePolicy policy = new SecurePolicy();
		policy.setId(id);
		policy.setRequestBodyMode(source.getRequestBodyMode());
		policy.setResponseBodyMode(source.getResponseBodyMode());
		policy.setCryptoEnabled(source.isCryptoEnabled());
		policy.setSignatureEnabled(source.isSignatureEnabled());
		policy.setCryptoHandlerName(source.getCryptoHandler());
		policy.setSignatureHandlerName(source.getSignatureHandler());
		policy.setKeyResolverName(source.getKeyResolver());
		policy.setNonceGeneratorName(source.getNonceGenerator());
		policy.setReplayGuardName(source.getReplayGuard());
		policy.setEnvelopeName(source.getEnvelope());
		policy.setTimestampWindow(source.getTimestampWindow());
		validatePolicy(policy);
		return policy;
	}

	private void validatePolicy(SecurePolicy policy) {
		if (policy.isCryptoEnabled() && !policy.isSignatureEnabled()) {
			throw new IllegalStateException("signature must be enabled when crypto is enabled: " + policy.getId());
		}
		if (!policy.isCryptoEnabled() && !policy.isSignatureEnabled() && requiresSecureExchange(policy)) {
			throw new IllegalStateException(
					"crypto and signature cannot both be disabled for secure exchange: " + policy.getId());
		}
	}

	private boolean requiresSecureExchange(SecurePolicy policy) {
		return !(isPlainRequest(policy.getRequestBodyMode())
				&& policy.getResponseBodyMode() == SecureResponseMode.DISABLED);
	}

	private boolean isPlainRequest(SecureInputMode mode) {
		return mode == SecureInputMode.DISABLED || mode == SecureInputMode.PLAIN;
	}

}
