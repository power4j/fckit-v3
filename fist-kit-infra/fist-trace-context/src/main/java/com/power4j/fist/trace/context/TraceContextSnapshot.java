package com.power4j.fist.trace.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 可跨线程保存和恢复的追踪上下文快照。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class TraceContextSnapshot {

	private final Map<String, String> values;

	private final Optional<String> spanId;

	public TraceContextSnapshot(Map<String, String> values, Optional<String> spanId) {
		this.values = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(values, "values")));
		this.spanId = Objects.requireNonNull(spanId, "spanId");
	}

	public Map<String, String> values() {
		return this.values;
	}

	public Optional<String> spanId() {
		return this.spanId;
	}

	TraceContext restore() {
		return new DefaultTraceContext(this.values, this.spanId);
	}

}
