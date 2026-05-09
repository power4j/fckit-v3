package com.power4j.fist.sde.client;

public class SecureExchangeClientProperties {

	private String defaultPolicyId;

	private String defaultKeyRef;

	private boolean logPayload;

	public String getDefaultPolicyId() {
		return this.defaultPolicyId;
	}

	public void setDefaultPolicyId(String defaultPolicyId) {
		this.defaultPolicyId = defaultPolicyId;
	}

	public String getDefaultKeyRef() {
		return this.defaultKeyRef;
	}

	public void setDefaultKeyRef(String defaultKeyRef) {
		this.defaultKeyRef = defaultKeyRef;
	}

	public boolean isLogPayload() {
		return this.logPayload;
	}

	public void setLogPayload(boolean logPayload) {
		this.logPayload = logPayload;
	}

}
