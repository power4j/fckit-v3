package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.exception.SecureEnvelopeException;

public enum SecureScope {

	BODY("body"), RESPONSE_BODY("responseBody");

	private final String value;

	SecureScope(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static SecureScope fromValue(String value) {
		for (SecureScope scope : values()) {
			if (scope.value.equals(value)) {
				return scope;
			}
		}
		throw new SecureEnvelopeException("unsupported scope: " + value);
	}

}
