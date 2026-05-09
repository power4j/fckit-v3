package com.power4j.fist.sde.core;

import org.jspecify.annotations.Nullable;

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

	public @Nullable String getVersion() {
		return this.version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
	}

	public @Nullable String getScope() {
		return this.scope;
	}

	public void setScope(@Nullable String scope) {
		this.scope = scope;
	}

	public @Nullable String getPayload() {
		return this.payload;
	}

	public void setPayload(@Nullable String payload) {
		this.payload = payload;
	}

	public @Nullable String getSignature() {
		return this.signature;
	}

	public void setSignature(@Nullable String signature) {
		this.signature = signature;
	}

	public @Nullable String getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(@Nullable String timestamp) {
		this.timestamp = timestamp;
	}

	public @Nullable String getNonce() {
		return this.nonce;
	}

	public void setNonce(@Nullable String nonce) {
		this.nonce = nonce;
	}

	public @Nullable String getKeyRef() {
		return this.keyRef;
	}

	public void setKeyRef(@Nullable String keyRef) {
		this.keyRef = keyRef;
	}

	public @Nullable String getAlgorithm() {
		return this.algorithm;
	}

	public void setAlgorithm(@Nullable String algorithm) {
		this.algorithm = algorithm;
	}

	public @Nullable String getPolicyId() {
		return this.policyId;
	}

	public void setPolicyId(@Nullable String policyId) {
		this.policyId = policyId;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(@Nullable Map<String, String> metadata) {
		this.metadata = metadata == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<>(metadata);
	}

}
