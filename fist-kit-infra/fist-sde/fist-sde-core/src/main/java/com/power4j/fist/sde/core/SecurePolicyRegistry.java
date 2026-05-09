package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import org.jspecify.annotations.Nullable;

/**
 * SDE 策略和 envelope 编码上下文的注册表。
 */
public interface SecurePolicyRegistry {

	/**
	 * 获取默认策略 ID。
	 * @return 默认策略 ID
	 */
	String getDefaultPolicyId();

	/**
	 * 按策略 ID 获取策略。
	 * @param policyId 策略 ID
	 * @return 策略配置
	 */
	SecurePolicy getPolicy(String policyId);

	/**
	 * 按 envelope 名称获取编码上下文。
	 * @param envelopeName envelope 配置名称；为 {@code null} 时实现可返回默认上下文
	 * @return envelope 编码上下文
	 */
	SecureEnvelopeContext getEnvelopeContext(@Nullable String envelopeName);

}
