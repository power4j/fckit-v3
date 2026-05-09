package com.power4j.fist.sde.core.codec;

import com.power4j.fist.sde.core.SecureEnvelope;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonSecureEnvelopeCodecTest {

	private final JacksonSecureEnvelopeCodec codec = new JacksonSecureEnvelopeCodec();

	@Test
	void shouldMapTransportFieldsToLogicalEnvelopeFields() {
		String body = "{\"version\":\"1\",\"scope\":\"body\",\"data\":\"abc\",\"sign\":\"sig\","
				+ "\"timestamp\":\"2026-05-08T12:00:00Z\",\"nonce\":\"n\",\"keyRef\":\"k\"," + "\"policyId\":\"p\"}";

		SecureEnvelope envelope = this.codec.decode(body.getBytes(StandardCharsets.UTF_8),
				SecureEnvelopeContext.defaults());

		assertThat(envelope.getPayload()).isEqualTo("abc");
		assertThat(envelope.getSignature()).isEqualTo("sig");
		assertThat(envelope.getKeyRef()).isEqualTo("k");
	}

	@Test
	void shouldEncodeToBodyWithoutCryptoOrSignatureSideEffects() {
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope("responseBody");
		envelope.setPayload("cipher");
		envelope.setSignature("sig");
		envelope.setTimestamp("2026-05-08T12:00:00Z");
		envelope.setNonce("n");
		envelope.setKeyRef("k");

		Object body = this.codec.encodeToBody(envelope, SecureEnvelopeContext.defaults());

		assertThat(body).isInstanceOf(Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> envelopeBody = (Map<String, Object>) body;
		assertThat(envelopeBody).containsEntry("data", "cipher").containsEntry("sign", "sig");
	}

}
