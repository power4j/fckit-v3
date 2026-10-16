package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmEncryptedValue;

import org.jspecify.annotations.Nullable;
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

	/**
	 * 主密钥来源，仅接受 file 或 raw；未配置时保留文件优先、属性来源次之的旧逻辑。
	 */
	private @Nullable String masterKeySource;

	/**
	 * raw 来源使用的主密钥文本，不能使用依赖该主密钥解密的密文。
	 */
	private @Nullable String masterKeyValue;

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

	public @Nullable String getMasterKeySource() {
		return this.masterKeySource;
	}

	public void setMasterKeySource(@Nullable String masterKeySource) {
		this.masterKeySource = masterKeySource;
	}

	public @Nullable String getMasterKeyValue() {
		return this.masterKeyValue;
	}

	public void setMasterKeyValue(@Nullable String masterKeyValue) {
		this.masterKeyValue = masterKeyValue;
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
