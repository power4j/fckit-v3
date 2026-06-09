package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmEncryptedValue;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FIST 国密配置加密属性。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
@ConfigurationProperties(prefix = "fist.jasypt")
public class FistJasyptProperties {

	private boolean enabled;

	private String masterKeyEnv = "FIST_JASYPT_MASTER_KEY";

	private String masterKeyFile;

	private String cipherPrefix = GmEncryptedValue.PREFIX;

	private String cipherSuffix = GmEncryptedValue.SUFFIX;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getMasterKeyEnv() {
		return this.masterKeyEnv;
	}

	public void setMasterKeyEnv(String masterKeyEnv) {
		this.masterKeyEnv = masterKeyEnv;
	}

	public String getMasterKeyFile() {
		return this.masterKeyFile;
	}

	public void setMasterKeyFile(String masterKeyFile) {
		this.masterKeyFile = masterKeyFile;
	}

	public String getCipherPrefix() {
		return this.cipherPrefix;
	}

	public void setCipherPrefix(String cipherPrefix) {
		this.cipherPrefix = cipherPrefix;
	}

	public String getCipherSuffix() {
		return this.cipherSuffix;
	}

	public void setCipherSuffix(String cipherSuffix) {
		this.cipherSuffix = cipherSuffix;
	}

}
