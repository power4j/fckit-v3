package com.power4j.fist.sde.extra.nonce;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.nonce.NonceContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomNonceGeneratorTest {

	@Test
	void shouldGenerateBase64UrlNonceWithAtLeast128BitsEntropy() {
		SecureRandomNonceGenerator generator = new SecureRandomNonceGenerator();

		String nonce = generator.generate(new NonceContext(SecureExchangeContext.outbound(SecureScope.BODY)));

		assertThat(nonce).matches("[A-Za-z0-9_-]+");
		assertThat(nonce.length()).isGreaterThanOrEqualTo(22);
	}

}
