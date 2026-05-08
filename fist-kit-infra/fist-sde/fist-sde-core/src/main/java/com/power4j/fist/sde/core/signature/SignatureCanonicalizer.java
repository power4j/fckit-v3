package com.power4j.fist.sde.core.signature;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;

public interface SignatureCanonicalizer {

	byte[] canonicalize(SecureEnvelope envelope, SecureExchangeContext context);

}
