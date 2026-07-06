package com.power4j.fist.trace.context.runtime;

import com.power4j.fist.trace.context.item.TraceContextItem;
import java.util.List;

/**
 * 默认追踪上下文项注册表。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class DefaultTraceContextRegistry implements TraceContextRegistry {

	private final List<TraceContextItem> items;

	public DefaultTraceContextRegistry(List<TraceContextItem> items) {
		this.items = List.copyOf(items);
	}

	@Override
	public List<TraceContextItem> items() {
		return this.items;
	}

}
