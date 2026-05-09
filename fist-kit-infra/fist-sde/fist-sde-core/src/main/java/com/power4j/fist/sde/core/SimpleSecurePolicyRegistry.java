package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.exception.SecurePolicyNotFoundException;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleSecurePolicyRegistry implements SecurePolicyRegistry {

	private final String defaultPolicyId;

	private final Map<String, SecurePolicy> policies;

	private final Map<String, SecureEnvelopeContext> envelopes;

	public SimpleSecurePolicyRegistry(String defaultPolicyId, Map<String, SecurePolicy> policies,
			Map<String, SecureEnvelopeContext> envelopes) {
		this.defaultPolicyId = defaultPolicyId;
		this.policies = new LinkedHashMap<>(policies);
		this.envelopes = new LinkedHashMap<>(envelopes);
	}

	@Override
	public String getDefaultPolicyId() {
		return this.defaultPolicyId;
	}

	@Override
	public SecurePolicy getPolicy(String policyId) {
		SecurePolicy policy = this.policies.get(policyId);
		if (policy == null) {
			throw new SecurePolicyNotFoundException("secure policy not found: " + policyId);
		}
		return policy;
	}

	@Override
	public SecureEnvelopeContext getEnvelopeContext(@Nullable String envelopeName) {
		SecureEnvelopeContext context = this.envelopes.get(envelopeName);
		return context == null ? SecureEnvelopeContext.defaults() : context;
	}

}
