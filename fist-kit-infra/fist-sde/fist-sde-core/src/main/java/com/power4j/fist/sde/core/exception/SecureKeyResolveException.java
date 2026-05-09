package com.power4j.fist.sde.core.exception;

/**
 * 根据 keyRef 和使用目的解析密钥失败时抛出的异常。
 */
public class SecureKeyResolveException extends SecureExchangeException {

	public SecureKeyResolveException(String message) {
		super(message);
	}

}
