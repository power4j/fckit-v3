package com.power4j.fist.jasypt.core;

/**
 * 国密配置密文处理异常。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
public class GmConfigCryptoException extends RuntimeException {

	public GmConfigCryptoException(String message) {
		super(message);
	}

	public GmConfigCryptoException(String message, Throwable cause) {
		super(message, cause);
	}

}
