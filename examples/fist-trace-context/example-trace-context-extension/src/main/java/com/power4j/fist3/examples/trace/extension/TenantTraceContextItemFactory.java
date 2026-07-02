package com.power4j.fist3.examples.trace.extension;

import com.power4j.fist.trace.context.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.TraceContextItem;
import com.power4j.fist.trace.context.TraceContextItemFactory;
import com.power4j.fist.trace.context.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.TraceContextItemSpec;
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
