package com.power4j.fist.sde.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SecureExchangeClientContext {

	private final String policyId;

	private final String keyRef;

	private final Map<String, Object> requestContext;

	public SecureExchangeClientContext(String policyId, String keyRef) {
		this(policyId, keyRef, null);
	}

	public SecureExchangeClientContext(String policyId, String keyRef, Map<String, Object> requestContext) {
		this.policyId = policyId;
		this.keyRef = keyRef;
		this.requestContext = requestContext == null ? Collections.<String, Object>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<>(requestContext));
	}

	public String getPolicyId() {
		return this.policyId;
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public Map<String, Object> getRequestContext() {
		return this.requestContext;
	}

}
