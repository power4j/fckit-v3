package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
