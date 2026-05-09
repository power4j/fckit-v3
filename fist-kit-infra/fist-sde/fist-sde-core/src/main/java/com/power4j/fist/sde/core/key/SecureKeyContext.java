package com.power4j.fist.sde.core.key;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKeyUsage;

/**
 * 密钥解析器使用的上下文。
 */
public class SecureKeyContext {

	private final SecureExchangeContext exchangeContext;

	private final String keyRef;

	private final SecureKeyUsage usage;

	public SecureKeyContext(SecureExchangeContext exchangeContext, String keyRef, SecureKeyUsage usage) {
		this.exchangeContext = exchangeContext;
		this.keyRef = keyRef;
		this.usage = usage;
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public SecureKeyUsage getUsage() {
		return this.usage;
	}

}
