package com.power4j.fist.sde.core;

import java.util.Arrays;

public class SecureKey {

	private final String keyRef;

	private final String algorithm;

	private final byte[] encoded;

	public SecureKey(String keyRef, String algorithm, byte[] encoded) {
		this.keyRef = keyRef;
		this.algorithm = algorithm;
		this.encoded = encoded == null ? new byte[0] : Arrays.copyOf(encoded, encoded.length);
	}

	public String getKeyRef() {
		return this.keyRef;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public byte[] getEncoded() {
		return Arrays.copyOf(this.encoded, this.encoded.length);
	}

}
