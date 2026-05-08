package com.power4j.fist.sde.extra.signature;

import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureHandler;

public class HmacSm3SignatureHandler implements SignatureHandler {

	@Override
	public byte[] sign(byte[] input, SignContext context) {
		throw unavailable();
	}

	@Override
	public boolean verify(byte[] input, byte[] signature, SignContext context) {
		throw unavailable();
	}

	private SecureAlgorithmUnavailableException unavailable() {
		return new SecureAlgorithmUnavailableException("HMAC-SM3 requires an explicitly registered provider", null);
	}

}
