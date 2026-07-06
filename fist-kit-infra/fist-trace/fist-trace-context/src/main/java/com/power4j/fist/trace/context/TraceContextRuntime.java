package com.power4j.fist.trace.context;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.carrier.OutboundTraceContext;
import com.power4j.fist.trace.context.carrier.TraceMdcContext;
import com.power4j.fist.trace.context.runtime.TraceContextRegistry;

/**
 * 追踪上下文运行时入口。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextRuntime {

	TraceContextRegistry registry();

	TraceContext inbound(InboundTraceContext context);

	void outbound(OutboundTraceContext context);

	void syncMdc(TraceMdcContext context);

	TraceContextSnapshot capture();

	TraceScope restore(TraceContextSnapshot snapshot);

}
