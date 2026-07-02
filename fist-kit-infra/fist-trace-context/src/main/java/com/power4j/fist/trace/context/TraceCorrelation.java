package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 链路关联 ID 读取工具。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class TraceCorrelation {

	private TraceCorrelation() {
	}

	public static Optional<String> current(TraceContextRuntime runtime) {
		return TraceContexts.current().flatMap(context -> resolve(runtime, context));
	}

	public static Optional<String> resolve(TraceContextRuntime runtime, TraceContext context) {
		return correlationItem(runtime).flatMap(item -> item.resolve(context));
	}

	public static Optional<String> resolve(TraceContextRuntime runtime, TraceContextSnapshot snapshot) {
		return correlationItem(runtime).flatMap(item -> item.resolve(snapshot));
	}

	public static Optional<CorrelationTraceContextItem> correlationItem(TraceContextRuntime runtime) {
		return runtime.registry()
			.items()
			.stream()
			.filter(CorrelationTraceContextItem.class::isInstance)
			.map(CorrelationTraceContextItem.class::cast)
			.findFirst();
	}

}
