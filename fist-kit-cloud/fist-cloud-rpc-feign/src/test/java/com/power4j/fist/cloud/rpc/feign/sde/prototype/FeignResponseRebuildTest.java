package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class FeignResponseRebuildTest {

	@Test
	void shouldPreserveResponseContextWhenReplacingBody() throws Exception {
		Request request = SecureFeignDecoderPrototypeTest.request();
		Response original = Response.builder()
			.status(207)
			.reason("Multi-Status")
			.headers(Collections.singletonMap("X-Trace-Id", Collections.singletonList("trace-1")))
			.request(request)
			.body("secure", StandardCharsets.UTF_8)
			.build();

		Response rebuilt = PrototypeFeignResponses.replaceBody(original,
				"{\"name\":\"fist\"}".getBytes(StandardCharsets.UTF_8));

		assertThat(rebuilt.status()).isEqualTo(207);
		assertThat(rebuilt.reason()).isEqualTo("Multi-Status");
		assertThat(rebuilt.headers()).containsKey("X-Trace-Id");
		assertThat(rebuilt.request()).isSameAs(request);
		assertThat(new String(rebuilt.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8))
			.isEqualTo("{\"name\":\"fist\"}");
	}

}
