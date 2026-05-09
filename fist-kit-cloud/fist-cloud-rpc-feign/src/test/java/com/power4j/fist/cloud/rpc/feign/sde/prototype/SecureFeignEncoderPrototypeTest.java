package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import feign.RequestTemplate;
import feign.codec.Encoder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SecureFeignEncoderPrototypeTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldWriteSecureEnvelopeWithoutChangingQueryOrHeaders() throws Exception {
		PrototypeSecureFeignSupport support = PrototypeSecureFeignSupport.defaults();
		Encoder encoder = new PrototypeSecureFeignEncoder(new JsonBodyEncoder(), support);
		RequestTemplate template = new RequestTemplate().method("POST")
			.uri("/orders?keyword=plain")
			.header("X-Request-Id", "rid-1");

		encoder.encode(Collections.singletonMap("name", "fist"), MapType.INSTANCE, template);

		SecureEnvelope envelope = new JacksonSecureEnvelopeCodec().decode(template.body(),
				SecureEnvelopeContext.defaults());
		assertThat(envelope.getScope()).isEqualTo(SecureScope.BODY.getValue());
		assertThat(envelope.getKeyRef()).isEqualTo("tenant-a");
		assertThat(envelope.getPolicyId()).isEqualTo("body-strict-v1");
		assertThat(this.objectMapper.readTree(template.body()).has("payload" + "Digest")).isFalse();
		assertThat(template.queries()).containsKey("keyword");
		assertThat(template.headers()).containsKey("X-Request-Id");

		byte[] plain = support.decryptAndVerify(envelope, SecureScope.BODY);
		JsonNode node = this.objectMapper.readTree(new String(plain, StandardCharsets.UTF_8));
		assertThat(node.get("name").asText()).isEqualTo("fist");
	}

	static final class MapType implements Type {

		static final MapType INSTANCE = new MapType();

	}

}
