package com.power4j.fist.sde.extra.digest;

import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sm3Digest {

	public byte[] digest(byte[] input) {
		try {
			return MessageDigest.getInstance("SM3").digest(input);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new SecureAlgorithmUnavailableException("SM3 algorithm unavailable", ex);
		}
	}

}
