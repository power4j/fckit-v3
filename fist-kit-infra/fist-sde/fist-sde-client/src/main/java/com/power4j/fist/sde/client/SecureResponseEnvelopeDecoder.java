package com.power4j.fist.sde.client;

public class SecureResponseEnvelopeDecoder {

	private final SecureExchangeOperations operations;

	public SecureResponseEnvelopeDecoder(SecureExchangeOperations operations) {
		this.operations = operations;
	}

	public byte[] decode(byte[] envelope, SecureExchangeClientContext context) {
		return this.operations.decodeResponse(envelope, context);
	}

}
