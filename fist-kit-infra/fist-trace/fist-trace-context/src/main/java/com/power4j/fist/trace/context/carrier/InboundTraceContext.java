package com.power4j.fist.trace.context.carrier;

import java.util.Map;
import java.util.Optional;

/**
 * 入口追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface InboundTraceContext {

	Optional<String> readHeader(String name);

	Optional<String> getValue(String name);

	void putValue(String name, String value);

	Map<String, String> values();

}
