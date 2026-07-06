package com.power4j.fist.trace.context.exception;

/**
 * 追踪上下文配置异常。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextConfigurationException extends RuntimeException {

	public TraceContextConfigurationException(String message) {
		super(message);
	}

	public TraceContextConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
