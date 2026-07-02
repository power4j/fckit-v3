package com.power4j.fist.trace.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceCorrelation} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceCorrelationTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldResolveCorrelationByItemContextName() {
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new TestCorrelationItem("correlationId"))));
		TraceContextSnapshot snapshot = TraceContexts.create(Map.of("correlationId", "REQ-1")).snapshot();

		assertThat(TraceCorrelation.resolve(runtime, snapshot)).contains("REQ-1");
	}

	@Test
	void shouldReturnEmptyWhenCorrelationItemMissing() {
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(new DefaultTraceContextRegistry(List.of()));
		TraceContextSnapshot snapshot = TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot();

		assertThat(TraceCorrelation.resolve(runtime, snapshot)).isEmpty();
	}

	private static class TestCorrelationItem extends AbstractSingleValueTraceContextItem
			implements CorrelationTraceContextItem {

		TestCorrelationItem(String contextName) {
			super("test", contextName, Optional.empty(), Optional.empty(), Optional.empty());
		}

	}

}
