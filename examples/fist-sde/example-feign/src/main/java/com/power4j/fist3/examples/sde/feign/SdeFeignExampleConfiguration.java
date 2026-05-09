package com.power4j.fist3.examples.sde.feign;

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
class SdeFeignExampleConfiguration {

	static final String KEY_REF = "tenant-a";

	static final String POLICY_ID = "body-strict-v1";

	private static final Logger log = LoggerFactory.getLogger(SdeFeignExampleConfiguration.class);

	private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

	@Bean
	AesGcmCryptoHandler aesGcmCryptoHandler() {
		log.info("SDE Feign example crypto handler: aesGcmCryptoHandler / AES-GCM");
		return new AesGcmCryptoHandler();
	}

	@Bean
	HmacSha256SignatureHandler hmacSha256SignatureHandler() {
		log.info("SDE Feign example signature handler: hmacSha256SignatureHandler / HMAC-SHA256");
		return new HmacSha256SignatureHandler();
	}

	@Bean
	SecureRandomNonceGenerator secureRandomNonceGenerator() {
		log.info("SDE Feign example nonce generator: secureRandomNonceGenerator");
		return new SecureRandomNonceGenerator();
	}

	@Bean
	InMemoryReplayGuard replayGuard() {
		log.info("SDE Feign example replay guard: replayGuard / InMemoryReplayGuard for local demo only");
		return new InMemoryReplayGuard(Duration.ofMinutes(5));
	}

	@Bean
	StaticSecureKeyResolver staticSecureKeyResolver() {
		log.info("SDE Feign example key resolver: staticSecureKeyResolver, keyRef={}", KEY_REF);
		return StaticSecureKeyResolver.symmetric(KEY_REF, KEY);
	}

}
