package com.power4j.fist.sde.core.exception;

/**
 * 未找到指定 policyId 对应策略时抛出的异常。
 */
public class SecurePolicyNotFoundException extends SecureExchangeException {

	public SecurePolicyNotFoundException(String message) {
		super(message);
	}

}
