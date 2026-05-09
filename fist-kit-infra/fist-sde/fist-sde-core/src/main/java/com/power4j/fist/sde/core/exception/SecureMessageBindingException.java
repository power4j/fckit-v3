package com.power4j.fist.sde.core.exception;

/**
 * 业务对象与 SDE 明文字节之间转换失败时抛出的异常。
 */
public class SecureMessageBindingException extends SecureExchangeException {

	public SecureMessageBindingException(String message, Throwable cause) {
		super(message, cause);
	}

}
