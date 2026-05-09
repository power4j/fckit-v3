package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标准版 SDE 客户端默认组件配置。
 * <p>
 * 注册 AES-GCM、HMAC-SHA256 与 SecureRandom nonce 生成器，供客户端示例或显式导入使用。
 */
@Configuration(proxyBeanMethods = false)
public class StandardSecureExchangeClientConfiguration {

	/**
	 * 提供标准版 AES-GCM 加解密处理器。
	 * @return AES-GCM 处理器
	 */
	@Bean("standardAesGcmCryptoHandler")
	@ConditionalOnMissingBean(name = "standardAesGcmCryptoHandler")
	public CryptoHandler standardAesGcmCryptoHandler() {
		return new AesGcmCryptoHandler();
	}

	/**
	 * 提供标准版 HMAC-SHA256 签名处理器。
	 * @return HMAC-SHA256 处理器
	 */
	@Bean("standardHmacSha256SignatureHandler")
	@ConditionalOnMissingBean(name = "standardHmacSha256SignatureHandler")
	public SignatureHandler standardHmacSha256SignatureHandler() {
		return new HmacSha256SignatureHandler();
	}

	/**
	 * 提供基于 SecureRandom 的 nonce 生成器。
	 * @return nonce 生成器
	 */
	@Bean("standardSecureRandomNonceGenerator")
	@ConditionalOnMissingBean(name = "standardSecureRandomNonceGenerator")
	public NonceGenerator standardSecureRandomNonceGenerator() {
		return new SecureRandomNonceGenerator();
	}

}
