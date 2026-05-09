package com.power4j.fist.sde.core;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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

	@Builder
	public SecureExchangeContext(SecureScope scope, SecureDirection direction, @Nullable String policyId,
			@Nullable String algorithm, @Nullable String keyRef, @Nullable Duration timestampWindow,
			@Nullable Map<String, Object> requestContext) {
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
		return SecureExchangeContext.builder()
			.scope(scope)
			.direction(SecureDirection.INBOUND)
			.timestampWindow(Duration.ofMinutes(5))
			.build();
	}

	public static SecureExchangeContext outbound(SecureScope scope) {
		return SecureExchangeContext.builder()
			.scope(scope)
			.direction(SecureDirection.OUTBOUND)
			.timestampWindow(Duration.ofMinutes(5))
			.build();
	}

	public SecureExchangeContext withPolicy(@Nullable String policyId, @Nullable String algorithm,
			@Nullable String keyRef, @Nullable Duration timestampWindow) {
		return SecureExchangeContext.builder()
			.scope(this.scope)
			.direction(this.direction)
			.policyId(policyId)
			.algorithm(algorithm)
			.keyRef(keyRef)
			.timestampWindow(timestampWindow == null ? this.timestampWindow : timestampWindow)
			.requestContext(this.requestContext)
			.build();
	}

	public SecureScope getScope() {
		return this.scope;
	}

	public SecureDirection getDirection() {
		return this.direction;
	}

	public @Nullable String getPolicyId() {
		return this.policyId;
	}

	public @Nullable String getAlgorithm() {
		return this.algorithm;
	}

	public @Nullable String getKeyRef() {
		return this.keyRef;
	}

	public @Nullable Duration getTimestampWindow() {
		return this.timestampWindow;
	}

	public Map<String, Object> getRequestContext() {
		return this.requestContext;
	}

}
