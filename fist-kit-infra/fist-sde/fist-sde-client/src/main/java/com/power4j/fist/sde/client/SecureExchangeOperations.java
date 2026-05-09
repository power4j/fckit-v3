package com.power4j.fist.sde.client;

public interface SecureExchangeOperations {

	byte[] encodeRequest(byte[] body, SecureExchangeClientContext context);

	byte[] encodeResponse(byte[] body, SecureExchangeClientContext context);

	byte[] decodeResponse(byte[] envelope, SecureExchangeClientContext context);

}
