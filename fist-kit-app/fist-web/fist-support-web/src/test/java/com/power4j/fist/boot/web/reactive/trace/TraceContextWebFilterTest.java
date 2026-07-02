package com.power4j.fist.boot.web.reactive.trace;

import com.power4j.fist.trace.context.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceContextWebFilter} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextWebFilterTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldRunWithHighestPrecedence() {
		Order order = AnnotationUtils.findAnnotation(TraceContextWebFilter.class, Order.class);

		assertThat(order).isNotNull();
		assertThat(order.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
	}

	@Test
	void shouldWriteTraceSnapshotToExchangeAndReactorContext() {
		TraceContextWebFilter filter = new TraceContextWebFilter(runtime());
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.get("/test").header("X-REQ-UID", "REQ-1").build());
		AtomicReference<String> exchangeRequestId = new AtomicReference<>();
		AtomicReference<String> contextRequestId = new AtomicReference<>();
		WebFilterChain chain = chainExchange -> Mono.deferContextual(context -> {
			exchangeRequestId.set(ReactiveTraceContext.getSnapshot(chainExchange)
				.map(snapshot -> snapshot.values().get("requestId"))
				.orElse(null));
			contextRequestId.set(ReactiveTraceContext.getSnapshot(context)
				.map(snapshot -> snapshot.values().get("requestId"))
				.orElse(null));
			return Mono.empty();
		});

		filter.filter(exchange, chain).block();

		assertThat(exchangeRequestId).hasValue("REQ-1");
		assertThat(contextRequestId).hasValue("REQ-1");
		assertThat(TraceContexts.current()).isEmpty();
	}

	private static TraceContextRuntime runtime() {
		return new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new AbstractSingleValueTraceContextItem("test", "requestId",
						Optional.of("X-REQ-UID"), Optional.empty(), Optional.empty()) {
				})));
	}

}
