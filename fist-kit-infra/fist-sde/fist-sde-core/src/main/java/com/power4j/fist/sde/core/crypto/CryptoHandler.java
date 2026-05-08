package com.power4j.fist.sde.core.crypto;

public interface CryptoHandler {

	byte[] encrypt(byte[] plain, CryptoContext context);

	byte[] decrypt(byte[] cipher, CryptoContext context);

}
