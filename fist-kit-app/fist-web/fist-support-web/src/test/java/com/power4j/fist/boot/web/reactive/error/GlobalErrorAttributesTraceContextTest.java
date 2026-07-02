package com.power4j.fist.boot.web.reactive.error;

import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import com.power4j.fist.trace.context.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.CorrelationTraceContextItem;
import com.power4j.fist.trace.context.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Test
	void shouldReadRequestIdFromConfiguredCorrelationContextName() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		ReactiveTraceContext.putSnapshot(exchange, TraceContexts.create(Map.of("correlationId", "REQ-1")).snapshot());
		ServerRequest request = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
		TestGlobalErrorAttributes errorAttributes = new TestGlobalErrorAttributes(new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new TestCorrelationItem("correlationId")))));

		Map<String, Object> attributes = errorAttributes.process(new LinkedHashMap<>(), request);

		assertThat(attributes).containsEntry("requestId", "REQ-1");
	}

	@Test
	void shouldFallbackToRequestIdWhenCorrelationItemMissing() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		ReactiveTraceContext.putSnapshot(exchange, TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot());
		ServerRequest request = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
		TestGlobalErrorAttributes errorAttributes = new TestGlobalErrorAttributes(
				new DefaultTraceContextRuntime(new DefaultTraceContextRegistry(List.of())));

		Map<String, Object> attributes = errorAttributes.process(new LinkedHashMap<>(), request);

		assertThat(attributes).containsEntry("requestId", "REQ-1");
	}

	private static class TestGlobalErrorAttributes extends GlobalErrorAttributes {

		TestGlobalErrorAttributes() {
		}

		TestGlobalErrorAttributes(DefaultTraceContextRuntime runtime) {
			super(runtime);
		}

		Map<String, Object> process(Map<String, Object> attributes, ServerRequest request) {
			return processErrorAttributes(attributes, request);
		}

	}

	private static class TestCorrelationItem extends AbstractSingleValueTraceContextItem
			implements CorrelationTraceContextItem {

		TestCorrelationItem(String contextName) {
			super("test", contextName, Optional.empty(), Optional.empty(), Optional.empty());
		}

	}

}
