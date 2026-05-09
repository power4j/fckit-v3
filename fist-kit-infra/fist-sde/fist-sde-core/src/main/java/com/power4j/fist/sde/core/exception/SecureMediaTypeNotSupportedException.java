package com.power4j.fist.sde.core.exception;

/**
 * SDE 请求或响应遇到不支持的媒体类型时抛出的异常。
 */
public class SecureMediaTypeNotSupportedException extends SecureExchangeException {

	public SecureMediaTypeNotSupportedException(String message) {
		super(message);
	}

}
