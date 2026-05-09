package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.Sm4GcmCryptoHandler;
import com.power4j.fist.sde.extra.signature.HmacSm3SignatureHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 国密版 SDE 客户端默认组件配置。
 * <p>
 * 注册 SM4-GCM 与 HMAC-SM3 组件，运行环境需要额外提供国密算法 Provider。
 */
@Configuration(proxyBeanMethods = false)
public class GmSecureExchangeClientConfiguration {

	/**
	 * 提供国密 SM4-GCM 加解密处理器。
	 * @return SM4-GCM 处理器
	 */
	@Bean("gmSm4GcmCryptoHandler")
	@ConditionalOnMissingBean(name = "gmSm4GcmCryptoHandler")
	public CryptoHandler gmSm4GcmCryptoHandler() {
		return new Sm4GcmCryptoHandler();
	}

	/**
	 * 提供国密 HMAC-SM3 签名处理器。
	 * @return HMAC-SM3 处理器
	 */
	@Bean("gmHmacSm3SignatureHandler")
	@ConditionalOnMissingBean(name = "gmHmacSm3SignatureHandler")
	public SignatureHandler gmHmacSm3SignatureHandler() {
		return new HmacSm3SignatureHandler();
	}

}
