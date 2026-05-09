package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

public interface SecureExchangeOperations {

	byte[] encodeRequest(byte[] body, @Nullable SecureExchangeClientContext context);

	byte[] encodeResponse(byte[] body, @Nullable SecureExchangeClientContext context);

	byte[] decodeResponse(byte[] envelope, @Nullable SecureExchangeClientContext context);

}
