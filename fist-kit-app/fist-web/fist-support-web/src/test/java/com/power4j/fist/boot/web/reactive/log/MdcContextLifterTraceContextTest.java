package com.power4j.fist.boot.web.reactive.log;

import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import com.power4j.fist.trace.context.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MdcContextLifter} 追踪上下文测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class MdcContextLifterTraceContextTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		MDC.clear();
	}

	@Test
	void shouldRestoreTraceContextAndMdcForSignal() {
		TraceContextRuntime runtime = runtime();
		AtomicReference<String> requestId = new AtomicReference<>();
		AtomicReference<String> currentValue = new AtomicReference<>();
		CoreSubscriber<String> subscriber = new TestSubscriber(Context.of(ReactiveTraceContext.KEY_TRACE_CONTEXT,
				TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot()), () -> {
					requestId.set(MDC.get("requestId"));
					currentValue.set(TraceContexts.current().flatMap(ctx -> ctx.getValue("requestId")).orElse(null));
				});

		new MdcContextLifter<>(subscriber, runtime).onNext("test");

		assertThat(requestId).hasValue("REQ-1");
		assertThat(currentValue).hasValue("REQ-1");
		assertThat(MDC.get("requestId")).isNull();
		assertThat(TraceContexts.current()).isEmpty();
	}

	private static TraceContextRuntime runtime() {
		return new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new AbstractSingleValueTraceContextItem("test", "requestId",
						Optional.empty(), Optional.empty(), Optional.of("requestId")) {
				})));
	}

	private record TestSubscriber(Context context, Runnable callback) implements CoreSubscriber<String> {

		@Override
		public void onSubscribe(Subscription subscription) {
		}

		@Override
		public void onNext(String value) {
			this.callback.run();
		}

		@Override
		public void onError(Throwable throwable) {
		}

		@Override
		public void onComplete() {
		}

		@Override
		public Context currentContext() {
			return this.context;
		}

	}

}
