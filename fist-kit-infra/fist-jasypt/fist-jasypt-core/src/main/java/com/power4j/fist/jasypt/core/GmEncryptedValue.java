package com.power4j.fist.jasypt.core;

import java.util.Base64;

/**
 * `GMENC(...)` 配置密文 envelope。
 */
public final class GmEncryptedValue {

	public static final String PREFIX = "GMENC(";

	public static final String SUFFIX = ")";

	public static final String VERSION = "v1";

	private final byte[] salt;

	private final byte[] iv;

	private final byte[] cipher;

	private final byte[] mac;

	public GmEncryptedValue(byte[] salt, byte[] iv, byte[] cipher, byte[] mac) {
		this.salt = salt.clone();
		this.iv = iv.clone();
		this.cipher = cipher.clone();
		this.mac = mac.clone();
	}

	public byte[] getSalt() {
		return this.salt.clone();
	}

	public byte[] getIv() {
		return this.iv.clone();
	}

	public byte[] getCipher() {
		return this.cipher.clone();
	}

	public byte[] getMac() {
		return this.mac.clone();
	}

	public String format() {
		Base64.Encoder encoder = Base64.getEncoder();
		return PREFIX + VERSION + ":" + encoder.encodeToString(this.salt) + ":" + encoder.encodeToString(this.iv) + ":"
				+ encoder.encodeToString(this.cipher) + ":" + encoder.encodeToString(this.mac) + SUFFIX;
	}

	public static GmEncryptedValue parse(String value) {
		if (!value.startsWith(PREFIX) || !value.endsWith(SUFFIX)) {
			throw new GmConfigCryptoException("Invalid GMENC envelope");
		}
		String content = value.substring(PREFIX.length(), value.length() - SUFFIX.length());
		String[] parts = content.split(":", -1);
		if (parts.length != 5 || !VERSION.equals(parts[0])) {
			throw new GmConfigCryptoException("Invalid GMENC envelope version or field count");
		}
		Base64.Decoder decoder = Base64.getDecoder();
		try {
			return new GmEncryptedValue(decoder.decode(parts[1]), decoder.decode(parts[2]), decoder.decode(parts[3]),
					decoder.decode(parts[4]));
		}
		catch (IllegalArgumentException ex) {
			throw new GmConfigCryptoException("Invalid GMENC Base64 field", ex);
		}
	}

}
