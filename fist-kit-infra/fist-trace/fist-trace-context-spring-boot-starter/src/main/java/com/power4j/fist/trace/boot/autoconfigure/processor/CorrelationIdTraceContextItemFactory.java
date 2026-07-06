package com.power4j.fist.trace.boot.autoconfigure.processor;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.item.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.item.CorrelationTraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItemFactory;
import com.power4j.fist.trace.context.item.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.item.TraceContextItemSpec;
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
		return new CorrelationIdTraceContextItem(spec) {
			@Override
			protected Optional<String> generateValue(InboundTraceContext inbound) {
				return Optional.of(context.idGenerator().get());
			}
		};
	}

	private abstract static class CorrelationIdTraceContextItem extends AbstractPropDrivenTraceContextItem
			implements CorrelationTraceContextItem {

		CorrelationIdTraceContextItem(TraceContextItemSpec spec) {
			super(spec);
		}

	}

}
