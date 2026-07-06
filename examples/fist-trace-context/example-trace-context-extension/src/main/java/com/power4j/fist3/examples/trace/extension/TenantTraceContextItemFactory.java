package com.power4j.fist3.examples.trace.extension;

import com.power4j.fist.trace.context.item.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItemFactory;
import com.power4j.fist.trace.context.item.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.item.TraceContextItemSpec;
import org.springframework.stereotype.Component;

@Component
public class TenantTraceContextItemFactory implements TraceContextItemFactory {

	public static final String PROCESSOR = "tenant-id";

	@Override
	public String processor() {
		return PROCESSOR;
	}

	@Override
	public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
		return new AbstractPropDrivenTraceContextItem(spec) {
		};
	}

}
