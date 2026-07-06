package com.power4j.fist.trace.context;

import com.power4j.fist.trace.context.runtime.DefaultTraceContext;
import com.power4j.fist.trace.context.runtime.TraceContextHolder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 追踪上下文工具方法。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class TraceContexts {

	private TraceContexts() {
	}

	public static TraceContext create(Map<String, String> values) {
		return create(values, Optional.empty());
	}

	public static TraceContext create(Map<String, String> values, Optional<String> spanId) {
		return new DefaultTraceContext(values, spanId);
	}

	public static Optional<TraceContext> current() {
		return TraceContextHolder.current();
	}

	public static TraceContext requireCurrent() {
		return TraceContextHolder.requireCurrent();
	}

	public static TraceContextSnapshot capture() {
		return requireCurrent().snapshot();
	}

	public static TraceScope restore(TraceContextSnapshot snapshot) {
		TraceContext previous = TraceContextHolder.get();
		TraceContextHolder.set(snapshot.restore());
		return new TraceScope(previous);
	}

	public static TraceScope pushSpan(String spanId) {
		TraceContext current = requireCurrent();
		TraceContext previous = TraceContextHolder.get();
		TraceContextHolder.set(new DefaultTraceContext(current.asMap(), Optional.of(spanId)));
		return new TraceScope(previous);
	}

	public static <T> T withSpan(String spanId, Supplier<T> supplier) {
		try (TraceScope ignored = pushSpan(spanId)) {
			return supplier.get();
		}
	}

	public static void withSpan(String spanId, Runnable runnable) {
		try (TraceScope ignored = pushSpan(spanId)) {
			runnable.run();
		}
	}

	public static void clear() {
		TraceContextHolder.clear();
	}

}
