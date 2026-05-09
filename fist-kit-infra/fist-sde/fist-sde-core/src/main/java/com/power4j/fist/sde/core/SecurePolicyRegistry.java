package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import org.jspecify.annotations.Nullable;

public interface SecurePolicyRegistry {

	String getDefaultPolicyId();

	SecurePolicy getPolicy(String policyId);

	SecureEnvelopeContext getEnvelopeContext(@Nullable String envelopeName);

}
