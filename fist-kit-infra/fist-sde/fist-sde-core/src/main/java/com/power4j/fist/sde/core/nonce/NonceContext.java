package com.power4j.fist.sde.core.nonce;

import com.power4j.fist.sde.core.SecureExchangeContext;

/**
 * nonce 生成器使用的上下文。
 */
public class NonceContext {

	private final SecureExchangeContext exchangeContext;

	public NonceContext(SecureExchangeContext exchangeContext) {
		this.exchangeContext = exchangeContext;
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

}
