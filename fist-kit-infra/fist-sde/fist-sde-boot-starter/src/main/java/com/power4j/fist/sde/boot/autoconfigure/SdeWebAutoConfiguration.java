package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureExchangeExceptionTranslator;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.web.SecureRequestBodyAdvice;
import com.power4j.fist.sde.web.SecureResponseBodyAdvice;
import com.power4j.fist.sde.web.SecureWebExchangeService;

import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = { SdeCoreAutoConfiguration.class, SdeCodecAutoConfiguration.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "fist.sde", name = { "enabled", "web.enabled" }, havingValue = "true")
public class SdeWebAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SecureWebExchangeService secureWebExchangeService(SecurePolicyRegistry policyRegistry,
			SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer, SecureJsonCodec jsonCodec,
			Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
			Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
			Map<String, ReplayGuard> replayGuards,
			ObjectProvider<SecureExchangeExceptionTranslator> exceptionTranslator) {
		return new SecureWebExchangeService(policyRegistry, envelopeCodec, canonicalizer, jsonCodec, cryptoHandlers,
				signatureHandlers, keyResolvers, nonceGenerators, replayGuards, exceptionTranslator.getIfAvailable());
	}

	@Bean
	@ConditionalOnMissingBean
	public SecureRequestBodyAdvice secureRequestBodyAdvice(SecureWebExchangeService service) {
		return new SecureRequestBodyAdvice(service);
	}

	@Bean
	@ConditionalOnMissingBean
	public SecureResponseBodyAdvice secureResponseBodyAdvice(SecureWebExchangeService service) {
		return new SecureResponseBodyAdvice(service);
	}

}
