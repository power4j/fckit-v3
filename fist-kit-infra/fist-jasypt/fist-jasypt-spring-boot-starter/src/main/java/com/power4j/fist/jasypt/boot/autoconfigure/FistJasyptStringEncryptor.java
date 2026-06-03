package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmTextEncryptor;
import com.power4j.fist.jasypt.core.GmEncryptedValue;

import org.jasypt.encryption.StringEncryptor;

/**
 * 适配 Jasypt `StringEncryptor` 的国密文本加解密器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class FistJasyptStringEncryptor implements StringEncryptor {

	private final GmTextEncryptor delegate;

	private final String masterKey;

	private final String cipherPrefix;

	private final String cipherSuffix;

	FistJasyptStringEncryptor(GmTextEncryptor delegate, String masterKey) {
		this(delegate, masterKey, GmEncryptedValue.PREFIX, GmEncryptedValue.SUFFIX);
	}

	FistJasyptStringEncryptor(GmTextEncryptor delegate, String masterKey, String cipherPrefix, String cipherSuffix) {
		this.delegate = delegate;
		this.masterKey = masterKey;
		this.cipherPrefix = cipherPrefix;
		this.cipherSuffix = cipherSuffix;
	}

	@Override
	public String encrypt(String message) {
		return this.delegate.encrypt(message, this.masterKey, this.cipherPrefix, this.cipherSuffix);
	}

	@Override
	public String decrypt(String encryptedMessage) {
		if (encryptedMessage.startsWith(this.cipherPrefix) && encryptedMessage.endsWith(this.cipherSuffix)) {
			return this.delegate.decrypt(encryptedMessage, this.masterKey, this.cipherPrefix, this.cipherSuffix);
		}
		return this.delegate.decryptBody(encryptedMessage, this.masterKey);
	}

}
