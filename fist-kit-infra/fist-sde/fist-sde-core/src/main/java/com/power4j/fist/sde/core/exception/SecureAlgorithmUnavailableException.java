package com.power4j.fist.sde.core.exception;

/**
 * 当前运行环境缺少指定密码学算法时抛出的异常。
 */
public class SecureAlgorithmUnavailableException extends SecureExchangeException {

	public SecureAlgorithmUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
