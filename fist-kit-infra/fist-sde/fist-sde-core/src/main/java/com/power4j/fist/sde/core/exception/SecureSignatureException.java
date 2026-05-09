package com.power4j.fist.sde.core.exception;

/**
 * 签名生成或验签失败时抛出的异常。
 */
public class SecureSignatureException extends SecureExchangeException {

	public SecureSignatureException(String message) {
		super(message);
	}

	public SecureSignatureException(String message, Throwable cause) {
		super(message, cause);
	}

}
