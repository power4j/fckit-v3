package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.boot.autoconfigure.SdeCodecAutoConfiguration;
import com.power4j.fist.sde.boot.autoconfigure.SdeCoreAutoConfiguration;
import com.power4j.fist.sde.boot.autoconfigure.SdeProperties;
import com.power4j.fist.sde.client.DefaultSecureExchangeOperations;
import com.power4j.fist.sde.client.SecureExchangeClientLogger;
import com.power4j.fist.sde.client.SecureExchangeClientProperties;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * SDE HTTP 客户端通用自动配置。
 * <p>
 * 负责组装 {@link SecureExchangeOperations}，不主动导入测试级密钥解析器或默认算法实现。
 */
@AutoConfiguration(after = { SdeCoreAutoConfiguration.class, SdeCodecAutoConfiguration.class })
@ConditionalOnProperty(prefix = "fist.sde", name = { "enabled", "client.enabled" }, havingValue = "true")
public class SdeClientAutoConfiguration {

	/**
	 * 将 starter 属性转换为客户端运行属性。
	 * @param properties SDE 自动配置属性
	 * @return 客户端运行属性
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureExchangeClientProperties secureExchangeClientProperties(SdeProperties properties) {
		SecureExchangeClientProperties clientProperties = new SecureExchangeClientProperties();
		clientProperties.setDefaultPolicyId(properties.getClient().getDefaultPolicyId());
		clientProperties.setDefaultKeyRef(properties.getClient().getDefaultKeyRef());
		clientProperties.setLogPayload(properties.getClient().isLogPayload());
		return clientProperties;
	}

	/**
	 * 组装客户端 SDE 编解码与加解密操作入口。
	 * @param policyRegistry 策略注册表
	 * @param envelopeCodec envelope 编解码器
	 * @param canonicalizer 签名规范化器
	 * @param cryptoHandlers 加解密处理器集合
	 * @param signatureHandlers 签名处理器集合
	 * @param keyResolvers 密钥解析器集合
	 * @param nonceGenerators nonce 生成器集合
	 * @param replayGuards 重放校验器集合
	 * @param properties 客户端运行属性
	 * @param logger 客户端日志扩展点
	 * @return 客户端 SDE 操作入口
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureExchangeOperations secureExchangeOperations(SecurePolicyRegistry policyRegistry,
			SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer,
			Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
			Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
			Map<String, ReplayGuard> replayGuards, SecureExchangeClientProperties properties,
			org.springframework.beans.factory.ObjectProvider<SecureExchangeClientLogger> logger) {
		return DefaultSecureExchangeOperations.builder()
			.policyRegistry(policyRegistry)
			.envelopeCodec(envelopeCodec)
			.canonicalizer(canonicalizer)
			.cryptoHandlers(cryptoHandlers)
			.signatureHandlers(signatureHandlers)
			.keyResolvers(keyResolvers)
			.nonceGenerators(nonceGenerators)
			.replayGuards(replayGuards)
			.properties(properties)
			.logger(logger.getIfAvailable(() -> SecureExchangeClientLogger.NONE))
			.build();
	}

}
