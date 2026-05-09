package com.power4j.fist.sde.core.codec;

import com.power4j.fist.sde.core.SecureEnvelope;

public interface SecureEnvelopeCodec {

	SecureEnvelope decode(byte[] input, SecureEnvelopeContext context);

	byte[] encodeToBytes(SecureEnvelope envelope, SecureEnvelopeContext context);

	Object encodeToBody(SecureEnvelope envelope, SecureEnvelopeContext context);

}
