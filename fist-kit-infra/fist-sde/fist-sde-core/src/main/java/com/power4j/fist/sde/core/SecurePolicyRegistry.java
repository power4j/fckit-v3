package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;

public interface SecurePolicyRegistry {

	String getDefaultPolicyId();

	SecurePolicy getPolicy(String policyId);

	SecureEnvelopeContext getEnvelopeContext(String envelopeName);

}
