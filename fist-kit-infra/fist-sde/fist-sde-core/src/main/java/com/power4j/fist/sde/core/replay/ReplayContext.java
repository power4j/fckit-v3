package com.power4j.fist.sde.core.replay;

import com.power4j.fist.sde.core.SecureExchangeContext;
import lombok.Builder;

/**
 * 重放校验器使用的上下文。
 */
public class ReplayContext {

	private final SecureExchangeContext exchangeContext;

	private final String keyRef;

	private final String policyId;

	private final String nonce;

	private final String timestamp;

	@Builder
	public ReplayContext(SecureExchangeContext exchangeContext, String keyRef, String policyId, String nonce,
			String timestamp) {
		this.exchangeContext = exchangeContext;
		this.keyRef = keyRef;
		this.policyId = policyId;
		this.nonce = nonce;
		this.timestamp = timestamp;
	}

	public SecureExchangeContext getExchangeContext() {
		return this.exchangeContext;
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public String getPolicyId() {
		return this.policyId;
	}

	public String getNonce() {
		return this.nonce;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

}
