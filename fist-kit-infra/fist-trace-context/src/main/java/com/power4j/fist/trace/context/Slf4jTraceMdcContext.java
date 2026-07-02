package com.power4j.fist.trace.context;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 SLF4J MDC 的追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class Slf4jTraceMdcContext implements TraceMdcContext, AutoCloseable {

	private final Map<String, String> previousValues = new LinkedHashMap<>();

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void putMdc(String name, String value) {
		if (!this.previousValues.containsKey(name)) {
			this.previousValues.put(name, MDC.get(name));
		}
		MDC.put(name, value);
	}

	@Override
	public void close() {
		this.previousValues.forEach((name, value) -> {
			if (value == null) {
				MDC.remove(name);
			}
			else {
				MDC.put(name, value);
			}
		});
	}

}
