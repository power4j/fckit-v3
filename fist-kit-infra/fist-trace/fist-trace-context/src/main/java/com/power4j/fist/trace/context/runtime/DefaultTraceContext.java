package com.power4j.fist.trace.context.runtime;

import com.power4j.fist.trace.context.TraceContext;
import com.power4j.fist.trace.context.TraceContextSnapshot;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultTraceContext implements TraceContext {

	private final Map<String, String> values;

	private final Optional<String> spanId;

	public DefaultTraceContext(Map<String, String> values, Optional<String> spanId) {
		this.values = Collections.unmodifiableMap(copyValues(values));
		this.spanId = Objects.requireNonNull(spanId, "spanId");
	}

	@Override
	public Optional<String> getValue(String name) {
		return Optional.ofNullable(this.values.get(name));
	}

	@Override
	public Map<String, String> asMap() {
		return this.values;
	}

	@Override
	public Optional<String> getSpanId() {
		return this.spanId;
	}

	@Override
	public TraceContextSnapshot snapshot() {
		return new TraceContextSnapshot(this.values, this.spanId);
	}

	private static Map<String, String> copyValues(Map<String, String> values) {
		Objects.requireNonNull(values, "values");
		Map<String, String> copy = new LinkedHashMap<>();
		values.forEach((key, value) -> {
			if (key == null) {
				throw new IllegalArgumentException("Trace context name must not be null");
			}
			if (value != null) {
				copy.put(key, value);
			}
		});
		return copy;
	}

}
