package com.power4j.fist.sde.core.exception;

/**
 * nonce 生成或解析失败时抛出的异常。
 */
public class SecureNonceException extends SecureExchangeException {

	public SecureNonceException(String message, Throwable cause) {
		super(message, cause);
	}

}
