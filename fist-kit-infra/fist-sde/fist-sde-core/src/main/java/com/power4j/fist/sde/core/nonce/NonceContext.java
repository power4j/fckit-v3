package com.power4j.fist.sde.core.nonce;

import com.power4j.fist.sde.core.SecureExchangeContext;

public class NonceContext {

	private final SecureExchangeContext exchangeContext;

	public NonceContext(SecureExchangeContext exchangeContext) {
		this.exchangeContext = exchangeContext;
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

}
