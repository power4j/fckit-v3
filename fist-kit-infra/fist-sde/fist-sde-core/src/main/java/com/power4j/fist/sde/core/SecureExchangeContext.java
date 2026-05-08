package com.power4j.fist.sde.core;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SecureExchangeContext {

	private final SecureScope scope;

	private final SecureDirection direction;

	private final String policyId;

	private final String algorithm;

	private final String keyRef;

	private final Duration timestampWindow;

	private final Map<String, Object> requestContext;

	public SecureExchangeContext(SecureScope scope, SecureDirection direction, String policyId, String algorithm,
			String keyRef, Duration timestampWindow, Map<String, Object> requestContext) {
		this.scope = scope;
		this.direction = direction;
		this.policyId = policyId;
		this.algorithm = algorithm;
		this.keyRef = keyRef;
		this.timestampWindow = timestampWindow;
		this.requestContext = requestContext == null ? Collections.<String, Object>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<>(requestContext));
	}

	public static SecureExchangeContext inbound(SecureScope scope) {
		return new SecureExchangeContext(scope, SecureDirection.INBOUND, null, null, null, Duration.ofMinutes(5), null);
	}

	public static SecureExchangeContext outbound(SecureScope scope) {
		return new SecureExchangeContext(scope, SecureDirection.OUTBOUND, null, null, null, Duration.ofMinutes(5),
				null);
	}

	public SecureExchangeContext withPolicy(String policyId, String algorithm, String keyRef,
			Duration timestampWindow) {
		return new SecureExchangeContext(this.scope, this.direction, policyId, algorithm, keyRef,
				timestampWindow == null ? this.timestampWindow : timestampWindow, this.requestContext);
	}

	public SecureScope getScope() {
		return this.scope;
	}

	public SecureDirection getDirection() {
		return this.direction;
	}

	public String getPolicyId() {
		return this.policyId;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public Duration getTimestampWindow() {
		return this.timestampWindow;
	}

	public Map<String, Object> getRequestContext() {
		return this.requestContext;
	}

}
