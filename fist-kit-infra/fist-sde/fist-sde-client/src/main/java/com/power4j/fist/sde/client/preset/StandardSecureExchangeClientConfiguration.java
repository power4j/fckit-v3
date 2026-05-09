package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class StandardSecureExchangeClientConfiguration {

	@Bean("standardAesGcmCryptoHandler")
	@ConditionalOnMissingBean(name = "standardAesGcmCryptoHandler")
	public CryptoHandler standardAesGcmCryptoHandler() {
		return new AesGcmCryptoHandler();
	}

	@Bean("standardHmacSha256SignatureHandler")
	@ConditionalOnMissingBean(name = "standardHmacSha256SignatureHandler")
	public SignatureHandler standardHmacSha256SignatureHandler() {
		return new HmacSha256SignatureHandler();
	}

	@Bean("standardSecureRandomNonceGenerator")
	@ConditionalOnMissingBean(name = "standardSecureRandomNonceGenerator")
	public NonceGenerator standardSecureRandomNonceGenerator() {
		return new SecureRandomNonceGenerator();
	}

}
