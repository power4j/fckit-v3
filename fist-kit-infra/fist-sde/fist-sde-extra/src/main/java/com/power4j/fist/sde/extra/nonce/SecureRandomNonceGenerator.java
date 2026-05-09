package com.power4j.fist.sde.extra.nonce;

import com.power4j.fist.sde.core.exception.SecureNonceException;
import com.power4j.fist.sde.core.nonce.NonceContext;
import com.power4j.fist.sde.core.nonce.NonceGenerator;

import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomNonceGenerator implements NonceGenerator {

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate(NonceContext context) {
		try {
			byte[] bytes = new byte[16];
			this.secureRandom.nextBytes(bytes);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		}
		catch (Exception ex) {
			throw new SecureNonceException("secure nonce generation failed", ex);
		}
	}

}
