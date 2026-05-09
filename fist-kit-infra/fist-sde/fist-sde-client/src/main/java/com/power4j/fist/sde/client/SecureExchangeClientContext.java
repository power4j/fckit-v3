package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 客户端发起 SDE 交换时传入的上下文。
 * <p>
 * 上下文可覆盖默认策略、指定 keyRef，并携带调用方自定义请求上下文。
 */
public class SecureExchangeClientContext {

	private final String policyId;

	private final String keyRef;

	private final Map<String, Object> requestContext;

	public SecureExchangeClientContext(@Nullable String policyId, @Nullable String keyRef) {
		this(policyId, keyRef, null);
	}

	public SecureExchangeClientContext(@Nullable String policyId, @Nullable String keyRef,
			@Nullable Map<String, Object> requestContext) {
		this.policyId = policyId;
		this.keyRef = keyRef;
		this.requestContext = requestContext == null ? Collections.<String, Object>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<>(requestContext));
	}

	public @Nullable String getPolicyId() {
		return this.policyId;
	}

	public @Nullable String getKeyRef() {
		return this.keyRef;
	}

	public Map<String, Object> getRequestContext() {
		return this.requestContext;
	}

}
