package com.power4j.fist.trace.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 Map 的出口载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class MapOutboundTraceContext implements OutboundTraceContext {

	private final Map<String, String> headers = new LinkedHashMap<>();

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void writeHeader(String name, String value) {
		this.headers.put(name, value);
	}

	@Override
	public boolean hasHeader(String name) {
		return this.headers.containsKey(name);
	}

	public Map<String, String> headers() {
		return Map.copyOf(this.headers);
	}

}
