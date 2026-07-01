package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 出口追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface OutboundTraceContext {

	Optional<String> getValue(String name);

	void writeHeader(String name, String value);

	boolean hasHeader(String name);

}
