package com.power4j.fist.sde.core.exception;

/**
 * payload 加密或解密失败时抛出的异常。
 */
public class SecureCryptoException extends SecureExchangeException {

	public SecureCryptoException(String message, Throwable cause) {
		super(message, cause);
	}

}
