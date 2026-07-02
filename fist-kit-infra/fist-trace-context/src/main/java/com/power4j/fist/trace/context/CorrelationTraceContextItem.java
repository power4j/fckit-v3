package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 表示链路关联 ID 的追踪上下文项。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface CorrelationTraceContextItem {

	String contextName();

	default Optional<String> resolve(TraceContext context) {
		return context.getValue(contextName());
	}

	default Optional<String> resolve(TraceContextSnapshot snapshot) {
		return Optional.ofNullable(snapshot.values().get(contextName()));
	}

}
