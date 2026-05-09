package com.power4j.fist.sde.core.exception;

/**
 * envelope 编解码或字段规范校验失败时抛出的异常。
 */
public class SecureEnvelopeException extends SecureExchangeException {

	public SecureEnvelopeException(String message) {
		super(message);
	}

	public SecureEnvelopeException(String message, Throwable cause) {
		super(message, cause);
	}

}
