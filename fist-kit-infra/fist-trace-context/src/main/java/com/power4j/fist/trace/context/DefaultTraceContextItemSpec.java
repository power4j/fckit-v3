package com.power4j.fist.trace.context;

import java.util.Map;

/**
 * 默认追踪上下文项配置规格。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public record DefaultTraceContextItemSpec(String id, String processor, int order,
		Map<String, String> props) implements TraceContextItemSpec {

	public DefaultTraceContextItemSpec {
		props = Map.copyOf(props);
	}

}
