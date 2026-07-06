package com.power4j.fist.trace.context.item;

import java.util.Map;
import java.util.Optional;

/**
 * 追踪上下文项配置规格。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextItemSpec {

	String id();

	String processor();

	int order();

	Map<String, String> props();

	default Optional<String> optionalProp(String name) {
		return Optional.ofNullable(props().get(name));
	}

}
