package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.Sm4GcmCryptoHandler;
import com.power4j.fist.sde.extra.signature.HmacSm3SignatureHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GmSecureExchangeClientConfiguration {

	@Bean("gmSm4GcmCryptoHandler")
	@ConditionalOnMissingBean(name = "gmSm4GcmCryptoHandler")
	public CryptoHandler gmSm4GcmCryptoHandler() {
		return new Sm4GcmCryptoHandler();
	}

	@Bean("gmHmacSm3SignatureHandler")
	@ConditionalOnMissingBean(name = "gmHmacSm3SignatureHandler")
	public SignatureHandler gmHmacSm3SignatureHandler() {
		return new HmacSm3SignatureHandler();
	}

}
