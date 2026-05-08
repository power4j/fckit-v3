package com.power4j.fist.sde.extra.crypto;

import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;

public class Sm4CryptoHandler implements CryptoHandler {

	@Override
	public byte[] encrypt(byte[] plain, CryptoContext context) {
		throw unavailable();
	}

	@Override
	public byte[] decrypt(byte[] cipher, CryptoContext context) {
		throw unavailable();
	}

	private SecureAlgorithmUnavailableException unavailable() {
		return new SecureAlgorithmUnavailableException("SM4 requires an explicitly registered provider", null);
	}

}
