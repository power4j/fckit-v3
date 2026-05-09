package com.power4j.fist.sde.extra.signature;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.signature.SignContext;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HmacSha256SignatureHandlerTest {

	private final HmacSha256SignatureHandler handler = new HmacSha256SignatureHandler();

	@Test
	void shouldSignAndVerifyCanonicalInput() {
		SignContext context = new SignContext(SecureExchangeContext.outbound(SecureScope.BODY),
				new SecureKey("k1", "HmacSHA256", "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
		byte[] input = "payload=cipher".getBytes(StandardCharsets.UTF_8);

		byte[] signature = this.handler.sign(input, context);

		assertThat(this.handler.verify(input, signature, context)).isTrue();
		assertThat(this.handler.verify("payload=other".getBytes(StandardCharsets.UTF_8), signature, context)).isFalse();
	}

}
