package com.power4j.fist.sde.core.signature;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;

public class SignContext {

	private final SecureExchangeContext exchangeContext;

	private final SecureKey key;

	public SignContext(SecureExchangeContext exchangeContext, SecureKey key) {
		this.exchangeContext = exchangeContext;
		this.key = key;
	}

	public static SignContext inbound(SecureScope scope, SecureKey key) {
		return new SignContext(SecureExchangeContext.inbound(scope), key);
	}

	public static SignContext outbound(SecureScope scope, SecureKey key) {
		return new SignContext(SecureExchangeContext.outbound(scope), key);
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

	public SecureKey getKey() {
		return this.key;
	}

}
