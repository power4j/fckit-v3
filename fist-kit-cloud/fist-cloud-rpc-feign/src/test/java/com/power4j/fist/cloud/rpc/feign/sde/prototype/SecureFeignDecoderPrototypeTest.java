package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureScope;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureFeignDecoderPrototypeTest {

	@Test
	void shouldDecryptSecureResponseBeforeDelegateDecoder() throws Exception {
		PrototypeSecureFeignSupport support = PrototypeSecureFeignSupport.defaults();
		CapturingJsonDecoder delegate = new CapturingJsonDecoder();
		Decoder decoder = new PrototypeSecureFeignDecoder(delegate, support);
		Request request = request();
		Response response = Response.builder()
			.status(202)
			.reason("Accepted")
			.headers(Collections.singletonMap("X-Trace-Id", Collections.singletonList("trace-1")))
			.request(request)
			.body(support.encryptAndSign("{\"name\":\"fist\"}".getBytes(StandardCharsets.UTF_8),
					SecureScope.RESPONSE_BODY))
			.build();

		Object decoded = decoder.decode(response, JsonNode.class);

		assertThat(((JsonNode) decoded).get("name").asText()).isEqualTo("fist");
		assertThat(delegate.status).isEqualTo(202);
		assertThat(delegate.reason).isEqualTo("Accepted");
		assertThat(delegate.headers).containsKey("X-Trace-Id");
		assertThat(delegate.request).isSameAs(request);
	}

	@Test
	void shouldRejectReplayedSecureResponse() throws Exception {
		PrototypeSecureFeignSupport support = PrototypeSecureFeignSupport.defaults();
		Decoder decoder = new PrototypeSecureFeignDecoder(new CapturingJsonDecoder(), support);
		Response response = Response.builder()
			.status(200)
			.reason("OK")
			.headers(Collections.emptyMap())
			.request(request())
			.body(support.encryptAndSign("{\"name\":\"fist\"}".getBytes(StandardCharsets.UTF_8),
					SecureScope.RESPONSE_BODY))
			.build();

		decoder.decode(response, JsonNode.class);

		assertThatThrownBy(() -> decoder.decode(response, JsonNode.class))
			.isInstanceOf(com.power4j.fist.sde.core.exception.SecureReplayException.class)
			.hasMessageContaining("replayed");
	}

	static Request request() {
		return Request.create(Request.HttpMethod.POST, "https://example.test/orders", Collections.emptyMap(), null,
				StandardCharsets.UTF_8, null);
	}

	static final class CapturingJsonDecoder implements Decoder {

		private final ObjectMapper objectMapper = new ObjectMapper();

		private int status;

		private String reason;

		private Map<String, Collection<String>> headers;

		private Request request;

		@Override
		public Object decode(Response response, java.lang.reflect.Type type) throws java.io.IOException {
			this.status = response.status();
			this.reason = response.reason();
			this.headers = response.headers();
			this.request = response.request();
			return this.objectMapper.readTree(response.body().asInputStream());
		}

	}

}
