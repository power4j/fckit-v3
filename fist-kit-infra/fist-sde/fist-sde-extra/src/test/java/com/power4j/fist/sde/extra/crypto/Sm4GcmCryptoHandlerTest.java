package com.power4j.fist.sde.extra.crypto;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Sm4GcmCryptoHandlerTest {

	private final Sm4GcmCryptoHandler handler = new Sm4GcmCryptoHandler();

	@Test
	void shouldEncryptAndDecryptWithSm4GcmWhenProviderExists() {
		ensureBouncyCastle();
		CryptoContext context = context();

		byte[] cipher = this.handler.encrypt("{\"name\":\"fist\"}".getBytes(StandardCharsets.UTF_8), context);
		byte[] plain = this.handler.decrypt(cipher, context);

		assertThat(new String(plain, StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"fist\"}");
		assertThat(new String(cipher, StandardCharsets.UTF_8)).isNotEqualTo("{\"name\":\"fist\"}");
	}

	@Test
	void shouldRejectWhenSm4ProviderMissing() {
		Security.removeProvider("BC");
		try {
			assertThatThrownBy(() -> this.handler.encrypt("body".getBytes(StandardCharsets.UTF_8), context()))
				.isInstanceOf(SecureAlgorithmUnavailableException.class)
				.hasMessageContaining("SM4-GCM");
		}
		finally {
			ensureBouncyCastle();
		}
	}

	private static CryptoContext context() {
		return new CryptoContext(SecureExchangeContext.outbound(SecureScope.BODY),
				new SecureKey("k1", "SM4", "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
	}

	private static void ensureBouncyCastle() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

}
