package com.power4j.fist.trace.context.carrier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 Map 的入口载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class MapInboundTraceContext implements InboundTraceContext {

	private final Map<String, String> headers;

	private final Map<String, String> values = new LinkedHashMap<>();

	public MapInboundTraceContext(Map<String, String> headers) {
		this.headers = new LinkedHashMap<>(headers);
	}

	@Override
	public Optional<String> readHeader(String name) {
		return Optional.ofNullable(this.headers.get(name));
	}

	@Override
	public Optional<String> getValue(String name) {
		return Optional.ofNullable(this.values.get(name));
	}

	@Override
	public void putValue(String name, String value) {
		this.values.put(name, value);
	}

	@Override
	public Map<String, String> values() {
		return Map.copyOf(this.values);
	}

}
