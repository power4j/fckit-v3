package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.boot.autoconfigure.SdeCodecAutoConfiguration;
import com.power4j.fist.sde.boot.autoconfigure.SdeCoreAutoConfiguration;
import com.power4j.fist.sde.boot.autoconfigure.SdeProperties;
import com.power4j.fist.sde.client.DefaultSecureExchangeOperations;
import com.power4j.fist.sde.client.SecureExchangeClientLogger;
import com.power4j.fist.sde.client.SecureExchangeClientProperties;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@AutoConfiguration(after = { SdeCoreAutoConfiguration.class, SdeCodecAutoConfiguration.class })
@ConditionalOnProperty(prefix = "fist.sde", name = { "enabled", "client.enabled" }, havingValue = "true")
public class SdeClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SecureExchangeClientProperties secureExchangeClientProperties(SdeProperties properties) {
		SecureExchangeClientProperties clientProperties = new SecureExchangeClientProperties();
		clientProperties.setDefaultPolicyId(properties.getClient().getDefaultPolicyId());
		clientProperties.setDefaultKeyRef(properties.getClient().getDefaultKeyRef());
		clientProperties.setLogPayload(properties.getClient().isLogPayload());
		return clientProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public SecureExchangeOperations secureExchangeOperations(SecurePolicyRegistry policyRegistry,
			SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer,
			Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
			Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
			Map<String, ReplayGuard> replayGuards, SecureExchangeClientProperties properties,
			org.springframework.beans.factory.ObjectProvider<SecureExchangeClientLogger> logger) {
		return new DefaultSecureExchangeOperations(policyRegistry, envelopeCodec, canonicalizer, cryptoHandlers,
				signatureHandlers, keyResolvers, nonceGenerators, replayGuards, properties,
				logger.getIfAvailable(() -> SecureExchangeClientLogger.NONE));
	}

}
