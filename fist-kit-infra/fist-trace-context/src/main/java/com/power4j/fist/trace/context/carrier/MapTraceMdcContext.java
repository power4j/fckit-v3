package com.power4j.fist.trace.context.carrier;

import com.power4j.fist.trace.context.TraceContexts;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 Map 的 MDC 载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class MapTraceMdcContext implements TraceMdcContext {

	private final Map<String, String> values = new LinkedHashMap<>();

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void putMdc(String name, String value) {
		this.values.put(name, value);
	}

	public Map<String, String> values() {
		return Map.copyOf(this.values);
	}

}
