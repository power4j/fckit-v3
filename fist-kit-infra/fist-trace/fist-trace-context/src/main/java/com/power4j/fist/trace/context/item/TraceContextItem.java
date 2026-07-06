package com.power4j.fist.trace.context.item;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.carrier.OutboundTraceContext;
import com.power4j.fist.trace.context.carrier.TraceMdcContext;

/**
 * 追踪上下文项。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextItem {

	String processor();

	default void onInbound(InboundTraceContext context) {
	}

	default void onOutbound(OutboundTraceContext context) {
	}

	default void onMdc(TraceMdcContext context) {
	}

}
