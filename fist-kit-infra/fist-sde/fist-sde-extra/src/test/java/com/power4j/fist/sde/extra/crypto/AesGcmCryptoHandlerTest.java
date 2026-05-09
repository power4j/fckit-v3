package com.power4j.fist.sde.extra.crypto;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.exception.SecureCryptoException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmCryptoHandlerTest {

	private final AesGcmCryptoHandler handler = new AesGcmCryptoHandler();

	@Test
	void shouldEncryptAndDecryptWithAesGcm() {
		CryptoContext context = new CryptoContext(SecureExchangeContext.outbound(SecureScope.BODY),
				new SecureKey("k1", "AES", "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));

		byte[] cipher = this.handler.encrypt("{\"name\":\"fist\"}".getBytes(StandardCharsets.UTF_8), context);
		byte[] plain = this.handler.decrypt(cipher, context);

		assertThat(new String(plain, StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"fist\"}");
		assertThat(new String(cipher, StandardCharsets.UTF_8)).isNotEqualTo("{\"name\":\"fist\"}")
			.doesNotContain("{\"name\":\"fist\"}");
	}

	@Test
	void shouldRejectTamperedCipherText() {
		CryptoContext context = new CryptoContext(SecureExchangeContext.outbound(SecureScope.BODY),
				new SecureKey("k1", "AES", "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
		byte[] cipher = this.handler.encrypt("body".getBytes(StandardCharsets.UTF_8), context);
		byte[] packet = Base64.getUrlDecoder().decode(cipher);
		packet[packet.length - 1] = (byte) (packet[packet.length - 1] ^ 1);
		byte[] tampered = Base64.getUrlEncoder().withoutPadding().encode(packet);

		assertThatThrownBy(() -> this.handler.decrypt(tampered, context)).isInstanceOf(SecureCryptoException.class);
	}

}
