package com.power4j.fist.sde.core.signature;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSignatureCanonicalizerTest {

	private final DefaultSignatureCanonicalizer canonicalizer = new DefaultSignatureCanonicalizer();

	@Test
	void shouldSortLogicalFieldsAndSignPayloadDirectly() {
		SecureEnvelope envelope = envelope();
		envelope.setSignature("ignored");
		envelope.setMetadata(new LinkedHashMap<String, String>() {
			{
				put("z", "last");
				put("a", "first");
			}
		});

		byte[] input = this.canonicalizer.canonicalize(envelope, SecureExchangeContext.outbound(SecureScope.BODY));

		assertThat(new String(input, StandardCharsets.UTF_8))
			.isEqualTo("algorithm=AES-GCM+HMAC-SHA256\n" + "keyRef=tenant-a.sde.2026-05\n" + "metadata=a=first&z=last\n"
					+ "nonce=nonce-001\n" + "payload=cipher-text\n" + "policyId=body-strict-v1\n" + "scope=body\n"
					+ "timestamp=2026-05-08T12:00:00Z\n" + "version=1");
	}

	@Test
	void shouldIgnoreBlankOptionalFieldsAndRejectBlankRequiredFields() {
		SecureEnvelope envelope = envelope();
		envelope.setAlgorithm("");
		envelope.setPolicyId("");

		byte[] input = this.canonicalizer.canonicalize(envelope, SecureExchangeContext.outbound(SecureScope.BODY));

		assertThat(new String(input, StandardCharsets.UTF_8)).doesNotContain("algorithm=").doesNotContain("policyId=");

		envelope.setKeyRef("");
		assertThatThrownBy(
				() -> this.canonicalizer.canonicalize(envelope, SecureExchangeContext.outbound(SecureScope.BODY)))
			.isInstanceOf(SecureEnvelopeException.class)
			.hasMessageContaining("keyRef");
	}

	private static SecureEnvelope envelope() {
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope("body");
		envelope.setPayload("cipher-text");
		envelope.setSignature("signature");
		envelope.setTimestamp("2026-05-08T12:00:00Z");
		envelope.setNonce("nonce-001");
		envelope.setKeyRef("tenant-a.sde.2026-05");
		envelope.setAlgorithm("AES-GCM+HMAC-SHA256");
		envelope.setPolicyId("body-strict-v1");
		return envelope;
	}

}
