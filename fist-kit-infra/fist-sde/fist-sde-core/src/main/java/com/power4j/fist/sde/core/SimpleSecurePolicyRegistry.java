package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.exception.SecurePolicyNotFoundException;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于本地 Map 的 SDE 策略注册表实现。
 * <p>
 * 该实现适合从配置文件或测试夹具构造策略集合，不负责动态刷新或远程配置读取。
 */
public class SimpleSecurePolicyRegistry implements SecurePolicyRegistry {

	private final String defaultPolicyId;

	private final Map<String, SecurePolicy> policies;

	private final Map<String, SecureEnvelopeContext> envelopes;

	/**
	 * 创建策略注册表。
	 * @param defaultPolicyId 默认策略 ID
	 * @param policies 策略集合，键为策略 ID
	 * @param envelopes envelope 编解码上下文集合，键为 envelope 名称
	 */
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
