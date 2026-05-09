package com.power4j.fist.sde.core.codec;

/**
 * envelope 协议逻辑字段到传输字段名的映射配置。
 * <p>
 * 逻辑字段名保持稳定，字段映射只影响 JSON 或 Map 中实际使用的字段名。
 */
public class SecureEnvelopeFieldMapping {

	private String versionField = "version";

	private String scopeField = "scope";

	private String payloadField = "data";

	private String signatureField = "sign";

	private String timestampField = "timestamp";

	private String nonceField = "nonce";

	private String keyRefField = "keyRef";

	private String algorithmField = "algorithm";

	private String policyIdField = "policyId";

	private String metadataField = "metadata";

	/**
	 * 创建默认字段映射。
	 * @return 默认字段映射
	 */
	public static SecureEnvelopeFieldMapping defaults() {
		return new SecureEnvelopeFieldMapping();
	}

	public String getVersionField() {
		return this.versionField;
	}

	public void setVersionField(String versionField) {
		this.versionField = versionField;
	}

	public String getScopeField() {
		return this.scopeField;
	}

	public void setScopeField(String scopeField) {
		this.scopeField = scopeField;
	}

	public String getPayloadField() {
		return this.payloadField;
	}

	public void setPayloadField(String payloadField) {
		this.payloadField = payloadField;
	}

	public String getSignatureField() {
		return this.signatureField;
	}

	public void setSignatureField(String signatureField) {
		this.signatureField = signatureField;
	}

	public String getTimestampField() {
		return this.timestampField;
	}

	public void setTimestampField(String timestampField) {
		this.timestampField = timestampField;
	}

	public String getNonceField() {
		return this.nonceField;
	}

	public void setNonceField(String nonceField) {
		this.nonceField = nonceField;
	}

	public String getKeyRefField() {
		return this.keyRefField;
	}

	public void setKeyRefField(String keyRefField) {
		this.keyRefField = keyRefField;
	}

	public String getAlgorithmField() {
		return this.algorithmField;
	}

	public void setAlgorithmField(String algorithmField) {
		this.algorithmField = algorithmField;
	}

	public String getPolicyIdField() {
		return this.policyIdField;
	}

	public void setPolicyIdField(String policyIdField) {
		this.policyIdField = policyIdField;
	}

	public String getMetadataField() {
		return this.metadataField;
	}

	public void setMetadataField(String metadataField) {
		this.metadataField = metadataField;
	}

}
