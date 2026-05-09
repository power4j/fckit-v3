package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureExchangeExceptionTranslator;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.web.SecureRequestBodyAdvice;
import com.power4j.fist.sde.web.SecureResponseBodyAdvice;
import com.power4j.fist.sde.web.SecureWebExchangeService;

import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * SDE 服务端 Spring MVC 自动配置。
 * <p>
 * 仅在 Servlet Web 应用并启用 {@code fist.sde.web.enabled} 时注册 RequestBodyAdvice、
 * ResponseBodyAdvice 与服务端处理服务。
 */
@AutoConfiguration(after = { SdeCoreAutoConfiguration.class, SdeCodecAutoConfiguration.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "fist.sde", name = { "enabled", "web.enabled" }, havingValue = "true")
public class SdeWebAutoConfiguration {

	/**
	 * 组装服务端 SDE 处理服务。
	 * @param policyRegistry 策略注册表
	 * @param envelopeCodec envelope 编解码器
	 * @param canonicalizer 签名规范化器
	 * @param jsonCodec JSON 序列化器
	 * @param cryptoHandlers 加解密处理器集合
	 * @param signatureHandlers 签名处理器集合
	 * @param keyResolvers 密钥解析器集合
	 * @param nonceGenerators nonce 生成器集合
	 * @param replayGuards 重放校验器集合
	 * @param exceptionTranslator 异常转换器提供器
	 * @return 服务端 SDE 处理服务
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureWebExchangeService secureWebExchangeService(SecurePolicyRegistry policyRegistry,
			SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer, SecureJsonCodec jsonCodec,
			Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
			Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
			Map<String, ReplayGuard> replayGuards,
			ObjectProvider<SecureExchangeExceptionTranslator> exceptionTranslator) {
		return SecureWebExchangeService.builder()
			.policyRegistry(policyRegistry)
			.envelopeCodec(envelopeCodec)
			.canonicalizer(canonicalizer)
			.jsonCodec(jsonCodec)
			.cryptoHandlers(cryptoHandlers)
			.signatureHandlers(signatureHandlers)
			.keyResolvers(keyResolvers)
			.nonceGenerators(nonceGenerators)
			.replayGuards(replayGuards)
			.exceptionTranslator(exceptionTranslator.getIfAvailable())
			.build();
	}

	/**
	 * 提供请求体解密 Advice。
	 * @param service 服务端 SDE 处理服务
	 * @return 请求体 Advice
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureRequestBodyAdvice secureRequestBodyAdvice(SecureWebExchangeService service) {
		return new SecureRequestBodyAdvice(service);
	}

	/**
	 * 提供响应体加密 Advice。
	 * @param service 服务端 SDE 处理服务
	 * @return 响应体 Advice
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureResponseBodyAdvice secureResponseBodyAdvice(SecureWebExchangeService service) {
		return new SecureResponseBodyAdvice(service);
	}

}
