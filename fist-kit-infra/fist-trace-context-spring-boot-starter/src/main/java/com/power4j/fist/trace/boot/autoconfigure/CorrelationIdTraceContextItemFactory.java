package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.InboundTraceContext;
import com.power4j.fist.trace.context.TraceContextItem;
import com.power4j.fist.trace.context.TraceContextItemFactory;
import com.power4j.fist.trace.context.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.TraceContextItemSpec;

import java.util.Optional;

/**
 * 链路关联 ID 追踪上下文项工厂。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class CorrelationIdTraceContextItemFactory implements TraceContextItemFactory {

	public static final String PROCESSOR = "correlation-id";

	@Override
	public String processor() {
		return PROCESSOR;
	}

	@Override
	public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
		return new AbstractPropDrivenTraceContextItem(spec) {
			@Override
			protected Optional<String> generateValue(InboundTraceContext inbound) {
				return Optional.of(context.idGenerator().get());
			}
		};
	}

}
