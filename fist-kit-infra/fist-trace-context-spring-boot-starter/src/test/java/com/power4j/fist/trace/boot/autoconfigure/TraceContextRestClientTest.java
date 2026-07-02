package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceContextRestClientInterceptor} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextRestClientTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class))
		.withPropertyValues("fist.trace-context.enabled=true");

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldRegisterRestClientCustomizerWhenEnabled() {
		this.runner.run((context) -> assertThat(context).hasSingleBean(TraceContextRestClientInterceptor.class)
			.hasSingleBean(RestClientCustomizer.class));
	}

	@Test
	void interceptorShouldWriteTraceHeaders() {
		this.runner.run((context) -> {
			TraceContextRestClientInterceptor interceptor = context.getBean(TraceContextRestClientInterceptor.class);
			MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET,
					URI.create("https://example.org"));

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				interceptor.intercept(request, new byte[0], (httpRequest, body) -> {
					assertThat(httpRequest.getHeaders().getFirst("X-REQ-UID")).isEqualTo("REQ-1");
					return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
				});
			}
		});
	}

}
