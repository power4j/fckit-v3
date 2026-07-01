package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 当前线程追踪上下文持有器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class TraceContextHolder {

	private static final ThreadLocal<TraceContext> HOLDER = new ThreadLocal<>();

	private TraceContextHolder() {
	}

	public static Optional<TraceContext> current() {
		return Optional.ofNullable(HOLDER.get());
	}

	public static TraceContext requireCurrent() {
		return current().orElseThrow(() -> new IllegalStateException("No current trace context"));
	}

	static TraceContext get() {
		return HOLDER.get();
	}

	static void set(TraceContext context) {
		if (context == null) {
			HOLDER.remove();
		}
		else {
			HOLDER.set(context);
		}
	}

	public static void clear() {
		HOLDER.remove();
	}

}
