package com.power4j.fist.trace.context;

/**
 * 配置驱动的单值追踪上下文项基类。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public abstract class AbstractPropDrivenTraceContextItem extends AbstractSingleValueTraceContextItem {

	protected static final String PROP_CONTEXT_NAME = "context-name";

	protected static final String PROP_INBOUND_HEADER = "inbound-header";

	protected static final String PROP_OUTBOUND_HEADER = "outbound-header";

	protected static final String PROP_MDC_NAME = "mdc-name";

	protected AbstractPropDrivenTraceContextItem(TraceContextItemSpec spec) {
		super(spec.processor(), requiredContextName(spec), spec.optionalProp(PROP_INBOUND_HEADER),
				spec.optionalProp(PROP_OUTBOUND_HEADER), spec.optionalProp(PROP_MDC_NAME));
	}

	private static String requiredContextName(TraceContextItemSpec spec) {
		return spec.optionalProp(PROP_CONTEXT_NAME)
			.filter(value -> !value.isBlank())
			.orElseThrow(() -> new TraceContextConfigurationException(
					"Trace context item '%s' requires prop '%s'".formatted(spec.id(), PROP_CONTEXT_NAME)));
	}

}
