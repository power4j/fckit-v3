package com.power4j.fist.sde.extra.signature;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import com.power4j.fist.sde.core.signature.SignContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacSm3SignatureHandlerTest {

	private final HmacSm3SignatureHandler handler = new HmacSm3SignatureHandler();

	@Test
	void shouldSignAndVerifyWhenProviderExists() {
		ensureBouncyCastle();
		SignContext context = context();
		byte[] input = "payload=cipher".getBytes(StandardCharsets.UTF_8);

		byte[] signature = this.handler.sign(input, context);

		assertThat(this.handler.verify(input, signature, context)).isTrue();
		assertThat(this.handler.verify("payload=other".getBytes(StandardCharsets.UTF_8), signature, context)).isFalse();
	}

	@Test
	void shouldRejectWhenSm3ProviderMissing() {
		Security.removeProvider("BC");
		try {
			assertThatThrownBy(() -> this.handler.sign("body".getBytes(StandardCharsets.UTF_8), context()))
				.isInstanceOf(SecureAlgorithmUnavailableException.class)
				.hasMessageContaining("HMAC-SM3");
		}
		finally {
			ensureBouncyCastle();
		}
	}

	private static SignContext context() {
		return new SignContext(SecureExchangeContext.outbound(SecureScope.BODY),
				new SecureKey("k1", "HmacSM3", "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
	}

	private static void ensureBouncyCastle() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

}
