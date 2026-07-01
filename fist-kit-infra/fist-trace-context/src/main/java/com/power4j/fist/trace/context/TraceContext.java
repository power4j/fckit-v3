package com.power4j.fist.trace.context;

import java.util.Map;
import java.util.Optional;

/**
 * 当前执行片段的只读追踪上下文。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContext {

	Optional<String> getValue(String name);

	Map<String, String> asMap();

	Optional<String> getSpanId();

	TraceContextSnapshot snapshot();

}
