package com.power4j.fist.sde.core.codec;

import com.power4j.fist.sde.core.SecureEnvelope;
import org.jspecify.annotations.Nullable;

public interface SecureEnvelopeCodec {

	SecureEnvelope decode(byte[] input, @Nullable SecureEnvelopeContext context);

	byte[] encodeToBytes(SecureEnvelope envelope, @Nullable SecureEnvelopeContext context);

	Object encodeToBody(SecureEnvelope envelope, @Nullable SecureEnvelopeContext context);

}
