package com.power4j.fist.sde.core.signature;

public interface SignatureHandler {

	byte[] sign(byte[] input, SignContext context);

	boolean verify(byte[] input, byte[] signature, SignContext context);

}
