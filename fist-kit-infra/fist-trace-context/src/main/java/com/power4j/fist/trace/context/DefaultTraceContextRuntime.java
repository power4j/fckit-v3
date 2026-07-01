package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 默认追踪上下文运行时。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class DefaultTraceContextRuntime implements TraceContextRuntime {

	public static final String DEFAULT_SPAN_MDC_NAME = "spanId";

	private final TraceContextRegistry registry;

	public DefaultTraceContextRuntime(TraceContextRegistry registry) {
		this.registry = registry;
	}

	@Override
	public TraceContextRegistry registry() {
		return this.registry;
	}

	@Override
	public TraceContext inbound(InboundTraceContext context) {
		for (TraceContextItem item : this.registry.items()) {
			item.onInbound(context);
		}
		TraceContext traceContext = TraceContexts.create(context.values());
		TraceContextHolder.set(traceContext);
		return traceContext;
	}

	@Override
	public void outbound(OutboundTraceContext context) {
		if (TraceContexts.current().isEmpty()) {
			return;
		}
		for (TraceContextItem item : this.registry.items()) {
			item.onOutbound(context);
		}
	}

	@Override
	public void syncMdc(TraceMdcContext context) {
		for (TraceContextItem item : this.registry.items()) {
			item.onMdc(context);
		}
		TraceContexts.current()
			.flatMap(TraceContext::getSpanId)
			.ifPresent(spanId -> context.putMdc(DEFAULT_SPAN_MDC_NAME, spanId));
	}

	@Override
	public TraceContextSnapshot capture() {
		return TraceContexts.capture();
	}

	@Override
	public TraceScope restore(TraceContextSnapshot snapshot) {
		return TraceContexts.restore(snapshot);
	}

	static Optional<TraceContext> current() {
		return TraceContexts.current();
	}

}
