package com.power4j.fist.jasypt.core;

import com.power4j.tile.crypto.bc.Spec;
import com.power4j.tile.crypto.core.CipherBlobDetails;
import com.power4j.tile.crypto.core.GeneralCryptoException;
import com.power4j.tile.crypto.core.QuickCipher;
import com.power4j.tile.crypto.core.QuickCipherBuilder;
import com.power4j.tile.crypto.utils.HmacSm3Util;
import com.power4j.tile.crypto.utils.Sm3Util;
import com.power4j.tile.crypto.utils.Sm4Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 国密配置文本加解密器。
 */
public class GmTextEncryptor {

	private static final int SALT_BYTES = 16;

	private static final byte[] ENC_LABEL = "fist-jasypt:v1:enc".getBytes(StandardCharsets.UTF_8);

	private static final byte[] MAC_LABEL = "fist-jasypt:v1:mac".getBytes(StandardCharsets.UTF_8);

	private final SecureRandom secureRandom;

	public GmTextEncryptor() {
		this(new SecureRandom());
	}

	GmTextEncryptor(SecureRandom secureRandom) {
		this.secureRandom = secureRandom;
	}

	/**
	 * 加密配置明文。
	 * @param plain 配置明文
	 * @param masterKey 解密主密钥
	 * @return `GMENC(...)` 配置密文
	 */
	public String encrypt(String plain, String masterKey) {
		return encrypt(plain, masterKey, GmEncryptedValue.PREFIX, GmEncryptedValue.SUFFIX);
	}

	public String encrypt(String plain, String masterKey, String prefix, String suffix) {
		byte[] salt = randomBytes(SALT_BYTES);
		byte[] iv = randomBytes(Sm4Util.BLOCK_SIZE);
		byte[] encKey = deriveKey(masterKey, salt, ENC_LABEL, 16);
		byte[] macKey = deriveKey(masterKey, salt, MAC_LABEL, HmacSm3Util.HMAC_SM3_BYTES);
		try {
			QuickCipher sm4 = cipher(encKey, iv);
			CipherBlobDetails blob = sm4.encrypt(plain.getBytes(StandardCharsets.UTF_8));
			byte[] mac = mac(macKey, salt, iv, blob.getCipher());
			return new GmEncryptedValue(salt, iv, blob.getCipher(), mac).format(prefix, suffix);
		}
		catch (GeneralCryptoException ex) {
			throw new GmConfigCryptoException("GMENC encryption failed", ex);
		}
	}

	/**
	 * 解密配置密文。
	 * @param cipherText `GMENC(...)` 配置密文
	 * @param masterKey 解密主密钥
	 * @return 配置明文
	 */
	public String decrypt(String cipherText, String masterKey) {
		return decrypt(cipherText, masterKey, GmEncryptedValue.PREFIX, GmEncryptedValue.SUFFIX);
	}

	public String decrypt(String cipherText, String masterKey, String prefix, String suffix) {
		GmEncryptedValue encrypted = GmEncryptedValue.parse(cipherText, prefix, suffix);
		return decrypt(encrypted, masterKey);
	}

	public String decryptBody(String cipherBody, String masterKey) {
		return decrypt(GmEncryptedValue.parseBody(cipherBody), masterKey);
	}

	private String decrypt(GmEncryptedValue encrypted, String masterKey) {
		byte[] encKey = deriveKey(masterKey, encrypted.getSalt(), ENC_LABEL, 16);
		byte[] macKey = deriveKey(masterKey, encrypted.getSalt(), MAC_LABEL, HmacSm3Util.HMAC_SM3_BYTES);
		byte[] expected = mac(macKey, encrypted.getSalt(), encrypted.getIv(), encrypted.getCipher());
		if (!MessageDigest.isEqual(expected, encrypted.getMac())) {
			throw new GmConfigCryptoException("GMENC MAC verification failed");
		}
		try {
			byte[] plain = cipher(encKey, encrypted.getIv()).decrypt(encrypted.getCipher());
			return new String(plain, StandardCharsets.UTF_8);
		}
		catch (GeneralCryptoException ex) {
			throw new GmConfigCryptoException("GMENC decryption failed", ex);
		}
	}

	private byte[] randomBytes(int length) {
		byte[] bytes = new byte[length];
		this.secureRandom.nextBytes(bytes);
		return bytes;
	}

	private static QuickCipher cipher(byte[] encKey, byte[] iv) {
		return QuickCipherBuilder.algorithm(Spec.ALGORITHM_SM4)
			.mode(Spec.MODE_CBC)
			.padding(Spec.PADDING_PKCS7)
			.secretKey(encKey)
			.ivParameter(iv)
			.build();
	}

	private static byte[] deriveKey(String masterKey, byte[] salt, byte[] label, int length) {
		byte[] master = masterKey.getBytes(StandardCharsets.UTF_8);
		byte[] input = new byte[label.length + salt.length + master.length];
		System.arraycopy(label, 0, input, 0, label.length);
		System.arraycopy(salt, 0, input, label.length, salt.length);
		System.arraycopy(master, 0, input, label.length + salt.length, master.length);
		return Sm3Util.hash(input, length, null);
	}

	private static byte[] mac(byte[] macKey, byte[] salt, byte[] iv, byte[] cipher) {
		byte[] version = GmEncryptedValue.VERSION.getBytes(StandardCharsets.UTF_8);
		byte[] input = new byte[version.length + salt.length + iv.length + cipher.length];
		System.arraycopy(version, 0, input, 0, version.length);
		System.arraycopy(salt, 0, input, version.length, salt.length);
		System.arraycopy(iv, 0, input, version.length + salt.length, iv.length);
		System.arraycopy(cipher, 0, input, version.length + salt.length + iv.length, cipher.length);
		return HmacSm3Util.sign(input, macKey);
	}

}
