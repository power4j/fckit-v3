package com.power4j.fist.sde.core.exception;

/**
 * 时间戳窗口校验或 nonce 重放校验失败时抛出的异常。
 */
public class SecureReplayException extends SecureExchangeException {

	public SecureReplayException(String message) {
		super(message);
	}

}
