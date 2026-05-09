package com.power4j.fist.sde.extra.crypto;

import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureCryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 标准版 AES-GCM payload 加解密实现。
 * <p>
 * 加密结果使用 URL-safe Base64 编码，原始密文包格式为 IV、密文与认证标签的顺序拼接。
 */
public class AesGcmCryptoHandler implements CryptoHandler {

	private static final int IV_LENGTH = 12;

	private static final int TAG_LENGTH = 128;

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public byte[] encrypt(byte[] plain, CryptoContext context) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			this.secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key(context), new GCMParameterSpec(TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(plain);
			byte[] packet = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, packet, 0, iv.length);
			System.arraycopy(encrypted, 0, packet, iv.length, encrypted.length);
			return Base64.getUrlEncoder().withoutPadding().encode(packet);
		}
		catch (Exception ex) {
			throw new SecureCryptoException("AES-GCM encryption failed", ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] cipherText, CryptoContext context) {
		try {
			byte[] packet = Base64.getUrlDecoder().decode(cipherText);
			byte[] iv = Arrays.copyOfRange(packet, 0, IV_LENGTH);
			byte[] encrypted = Arrays.copyOfRange(packet, IV_LENGTH, packet.length);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key(context), new GCMParameterSpec(TAG_LENGTH, iv));
			return cipher.doFinal(encrypted);
		}
		catch (Exception ex) {
			throw new SecureCryptoException("AES-GCM decryption failed", ex);
		}
	}

	private static SecretKeySpec key(CryptoContext context) {
		return new SecretKeySpec(context.getKey().getEncoded(), "AES");
	}

}
