package com.power4j.fist.sde.client;

public class SecureRequestEnvelopeEncoder {

	private final SecureExchangeOperations operations;

	public SecureRequestEnvelopeEncoder(SecureExchangeOperations operations) {
		this.operations = operations;
	}

	public byte[] encode(byte[] body, SecureExchangeClientContext context) {
		return this.operations.encodeRequest(body, context);
	}

}
