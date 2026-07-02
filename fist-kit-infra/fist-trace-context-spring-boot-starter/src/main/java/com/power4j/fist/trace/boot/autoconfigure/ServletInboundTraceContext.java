package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.InboundTraceContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet 请求入口载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class ServletInboundTraceContext implements InboundTraceContext {

	private final HttpServletRequest request;

	private final Map<String, String> values = new LinkedHashMap<>();

	ServletInboundTraceContext(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public Optional<String> readHeader(String name) {
		return Optional.ofNullable(this.request.getHeader(name));
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
