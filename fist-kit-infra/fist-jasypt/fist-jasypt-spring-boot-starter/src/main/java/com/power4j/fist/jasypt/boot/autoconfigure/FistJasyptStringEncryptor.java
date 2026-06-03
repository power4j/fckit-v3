package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmTextEncryptor;
import com.power4j.fist.jasypt.core.GmEncryptedValue;

import org.jasypt.encryption.StringEncryptor;

/**
 * 适配 Jasypt `StringEncryptor` 的国密文本加解密器。
 */
class FistJasyptStringEncryptor implements StringEncryptor {

	private final GmTextEncryptor delegate;

	private final String masterKey;

	FistJasyptStringEncryptor(GmTextEncryptor delegate, String masterKey) {
		this.delegate = delegate;
		this.masterKey = masterKey;
	}

	@Override
	public String encrypt(String message) {
		return this.delegate.encrypt(message, this.masterKey);
	}

	@Override
	public String decrypt(String encryptedMessage) {
		return this.delegate.decrypt(normalize(encryptedMessage), this.masterKey);
	}

	private static String normalize(String encryptedMessage) {
		if (encryptedMessage.startsWith(GmEncryptedValue.PREFIX)
				&& encryptedMessage.endsWith(GmEncryptedValue.SUFFIX)) {
			return encryptedMessage;
		}
		return GmEncryptedValue.PREFIX + encryptedMessage + GmEncryptedValue.SUFFIX;
	}

}
