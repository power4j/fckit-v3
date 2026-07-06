package com.power4j.fist.trace.context.carrier;

import java.util.Optional;

/**
 * MDC 同步载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceMdcContext {

	Optional<String> getValue(String name);

	void putMdc(String name, String value);

}
