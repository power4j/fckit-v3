package com.power4j.fist.trace.context.item;

import com.power4j.fist.trace.context.exception.TraceContextConfigurationException;

/**
 * 追踪上下文项基类。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public abstract class AbstractTraceContextItem implements TraceContextItem {

	private final String processor;

	protected AbstractTraceContextItem(String processor) {
		this.processor = processor;
	}

	@Override
	public String processor() {
		return this.processor;
	}

	protected final String requiredProp(TraceContextItemSpec spec, String name) {
		return spec.optionalProp(name)
			.filter(value -> !value.isBlank())
			.orElseThrow(() -> new TraceContextConfigurationException(
					"Trace context item '%s' requires prop '%s'".formatted(spec.id(), name)));
	}

}
