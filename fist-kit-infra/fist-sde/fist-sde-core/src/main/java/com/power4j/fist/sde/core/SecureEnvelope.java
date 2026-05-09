package com.power4j.fist.sde.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecureEnvelope {

	private String version;

	private String scope;

	private String payload;

	private String signature;

	private String timestamp;

	private String nonce;

	private String keyRef;

	private String algorithm;

	private String policyId;

	private Map<String, String> metadata = new LinkedHashMap<>();

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScope() {
		return this.scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getPayload() {
		return this.payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getSignature() {
		return this.signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getNonce() {
		return this.nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public void setKeyRef(String keyRef) {
		this.keyRef = keyRef;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getPolicyId() {
		return this.policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<>(metadata);
	}

}
