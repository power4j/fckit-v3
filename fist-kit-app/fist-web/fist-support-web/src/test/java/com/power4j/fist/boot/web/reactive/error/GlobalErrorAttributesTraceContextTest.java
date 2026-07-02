package com.power4j.fist.boot.web.reactive.error;

import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GlobalErrorAttributes} 追踪上下文测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class GlobalErrorAttributesTraceContextTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldReadRequestIdFromExchangeTraceSnapshot() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		ReactiveTraceContext.putSnapshot(exchange, TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot());
		ServerRequest request = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
		TestGlobalErrorAttributes errorAttributes = new TestGlobalErrorAttributes();

		Map<String, Object> attributes = errorAttributes.process(new LinkedHashMap<>(), request);

		assertThat(attributes).containsEntry("requestId", "REQ-1");
	}

	private static class TestGlobalErrorAttributes extends GlobalErrorAttributes {

		Map<String, Object> process(Map<String, Object> attributes, ServerRequest request) {
			return processErrorAttributes(attributes, request);
		}

	}

}
