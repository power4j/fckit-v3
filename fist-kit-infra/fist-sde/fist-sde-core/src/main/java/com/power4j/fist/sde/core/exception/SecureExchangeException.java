package com.power4j.fist.sde.core.exception;

/**
 * SDE 处理过程的基础运行时异常。
 */
public class SecureExchangeException extends RuntimeException {

	public SecureExchangeException(String message) {
		super(message);
	}

	public SecureExchangeException(String message, Throwable cause) {
		super(message, cause);
	}

}
