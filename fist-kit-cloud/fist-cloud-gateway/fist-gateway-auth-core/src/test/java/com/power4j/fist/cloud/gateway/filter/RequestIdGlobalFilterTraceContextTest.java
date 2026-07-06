package com.power4j.fist.cloud.gateway.filter;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.item.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.runtime.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.runtime.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import com.power4j.fist.boot.web.constant.HttpConstant;
import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RequestIdGlobalFilter} 追踪上下文测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class RequestIdGlobalFilterTraceContextTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldWriteTraceSnapshotAndKeepRequestHeader() {
		RequestIdGlobalFilter filter = new RequestIdGlobalFilter(HttpConstant.Header.KEY_REQUEST_ID, runtime());
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.get("/test").header(HttpConstant.Header.KEY_REQUEST_ID, "REQ-1").build());
		AtomicReference<String> contextRequestId = new AtomicReference<>();
		GatewayFilterChain chain = chainExchange -> Mono.deferContextual(context -> {
			contextRequestId.set(ReactiveTraceContext.getSnapshot(context)
				.map(snapshot -> snapshot.values().get("requestId"))
				.orElse(null));
			assertThat(chainExchange.getRequest().getHeaders().getFirst(HttpConstant.Header.KEY_REQUEST_ID))
				.isEqualTo("REQ-1");
			assertThat(
					ReactiveTraceContext.getSnapshot(chainExchange).map(snapshot -> snapshot.values().get("requestId")))
				.hasValue("REQ-1");
			return Mono.empty();
		});

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(contextRequestId).hasValue("REQ-1");
		assertThat(TraceContexts.current()).isEmpty();
	}

	@Test
	void shouldGenerateRequestHeaderFromTraceRuntime() {
		RequestIdGlobalFilter filter = new RequestIdGlobalFilter(HttpConstant.Header.KEY_REQUEST_ID, runtime());
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		AtomicReference<String> headerValue = new AtomicReference<>();
		GatewayFilterChain chain = chainExchange -> Mono.deferContextual(context -> {
			headerValue.set(chainExchange.getRequest().getHeaders().getFirst(HttpConstant.Header.KEY_REQUEST_ID));
			assertThat(ReactiveTraceContext.getSnapshot(context).map(snapshot -> snapshot.values().get("requestId")))
				.hasValue("REQ-GEN");
			return Mono.empty();
		});

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(headerValue).hasValue("REQ-GEN");
		assertThat(TraceContexts.current()).isEmpty();
	}

	private static TraceContextRuntime runtime() {
		return new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new AbstractSingleValueTraceContextItem("test", "requestId",
						Optional.of(HttpConstant.Header.KEY_REQUEST_ID),
						Optional.of(HttpConstant.Header.KEY_REQUEST_ID), Optional.of("requestId")) {
					@Override
					protected Optional<String> generateValue(InboundTraceContext context) {
						return Optional.of("REQ-GEN");
					}
				})));
	}

}
