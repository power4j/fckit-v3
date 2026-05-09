package com.power4j.fist.sde.core.crypto;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;

/**
 * 加解密处理器使用的上下文。
 */
public class CryptoContext {

	private final SecureExchangeContext exchangeContext;

	private final SecureKey key;

	public CryptoContext(SecureExchangeContext exchangeContext, SecureKey key) {
		this.exchangeContext = exchangeContext;
		this.key = key;
	}

	public static CryptoContext inbound(SecureScope scope, SecureKey key) {
		return new CryptoContext(SecureExchangeContext.inbound(scope), key);
	}

	public static CryptoContext outbound(SecureScope scope, SecureKey key) {
		return new CryptoContext(SecureExchangeContext.outbound(scope), key);
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

	public SecureKey getKey() {
		return this.key;
	}

}
