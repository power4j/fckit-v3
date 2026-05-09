package com.power4j.fist3.examples.sde.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.client.SecureExchangeClientContext;
import com.power4j.fist.sde.client.SecureExchangeClientLogger;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
class SdeWebClientExampleConfiguration {

	static final String KEY_REF = "tenant-a";

	static final String POLICY_ID = "body-strict-v1";

	private static final Logger log = LoggerFactory.getLogger(SdeWebClientExampleConfiguration.class);

	private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

	@Bean
	AesGcmCryptoHandler aesGcmCryptoHandler() {
		return new AesGcmCryptoHandler();
	}

	@Bean
	HmacSha256SignatureHandler hmacSha256SignatureHandler() {
		return new HmacSha256SignatureHandler();
	}

	@Bean
	SecureRandomNonceGenerator secureRandomNonceGenerator() {
		return new SecureRandomNonceGenerator();
	}

	@Bean
	InMemoryReplayGuard replayGuard() {
		return new InMemoryReplayGuard(Duration.ofMinutes(5));
	}

	@Bean
	StaticSecureKeyResolver staticSecureKeyResolver() {
		return StaticSecureKeyResolver.symmetric(KEY_REF, KEY);
	}

	@Bean
	SecureExchangeClientLogger webClientSecureExchangeClientLogger(ObjectMapper objectMapper) {
		return new SecureExchangeClientLogger() {
			@Override
			public void requestPlain(byte[] body, SecureExchangeClientContext context) {
				log.info("WebClient raw request body:\n{}", pretty(objectMapper, body));
			}

			@Override
			public void requestEnvelope(byte[] envelope, SecureExchangeClientContext context) {
				log.info("WebClient request envelope:\n{}", pretty(objectMapper, envelope));
			}

			@Override
			public void responseEnvelope(byte[] envelope, SecureExchangeClientContext context) {
				log.info("WebClient response envelope:\n{}", pretty(objectMapper, envelope));
			}

			@Override
			public void responsePlain(byte[] body, SecureExchangeClientContext context) {
				log.info("WebClient decrypted response body:\n{}", pretty(objectMapper, body));
			}
		};
	}

	private String pretty(ObjectMapper objectMapper, byte[] body) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(body));
		}
		catch (Exception ex) {
			return new String(body, StandardCharsets.UTF_8);
		}
	}

}
