package com.power4j.fist.sde.extra.crypto;

import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import com.power4j.fist.sde.core.exception.SecureCryptoException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class Sm4GcmCryptoHandler implements CryptoHandler {

	private static final int IV_LENGTH = 12;

	private static final int TAG_LENGTH = 128;

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public byte[] encrypt(byte[] plain, CryptoContext context) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			this.secureRandom.nextBytes(iv);
			Cipher cipher = cipher();
			cipher.init(Cipher.ENCRYPT_MODE, key(context), new GCMParameterSpec(TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(plain);
			byte[] packet = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, packet, 0, iv.length);
			System.arraycopy(encrypted, 0, packet, iv.length, encrypted.length);
			return Base64.getUrlEncoder().withoutPadding().encode(packet);
		}
		catch (SecureAlgorithmUnavailableException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new SecureCryptoException("SM4-GCM encryption failed", ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] cipherText, CryptoContext context) {
		try {
			byte[] packet = Base64.getUrlDecoder().decode(cipherText);
			byte[] iv = Arrays.copyOfRange(packet, 0, IV_LENGTH);
			byte[] encrypted = Arrays.copyOfRange(packet, IV_LENGTH, packet.length);
			Cipher cipher = cipher();
			cipher.init(Cipher.DECRYPT_MODE, key(context), new GCMParameterSpec(TAG_LENGTH, iv));
			return cipher.doFinal(encrypted);
		}
		catch (SecureAlgorithmUnavailableException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new SecureCryptoException("SM4-GCM decryption failed", ex);
		}
	}

	private static Cipher cipher() {
		try {
			return Cipher.getInstance("SM4/GCM/NoPadding");
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new SecureAlgorithmUnavailableException("SM4-GCM algorithm unavailable", ex);
		}
	}

	private static SecretKeySpec key(CryptoContext context) {
		return new SecretKeySpec(context.getKey().getEncoded(), "SM4");
	}

}
