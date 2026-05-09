package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.feign.SecureFeignDecoder;
import com.power4j.fist.sde.client.feign.SecureFeignEncoder;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * SDE Feign 客户端自动配置。
 * <p>
 * 当应用已存在 Feign {@link Encoder} 或 {@link Decoder} 时，注册对应的 SDE 包装器。
 */
@AutoConfiguration(after = SdeClientAutoConfiguration.class)
@ConditionalOnClass(name = "feign.codec.Encoder")
@ConditionalOnBean(SecureExchangeOperations.class)
public class SdeFeignClientAutoConfiguration {

	/**
	 * 使用 SDE 编码器包装已有 Feign 编码器。
	 * @param delegate 原始 Feign 编码器
	 * @param operations SDE 客户端操作入口
	 * @return SDE Feign 编码器
	 */
	@Bean
	@ConditionalOnBean(Encoder.class)
	@ConditionalOnMissingBean(SecureFeignEncoder.class)
	public SecureFeignEncoder secureFeignEncoder(Encoder delegate, SecureExchangeOperations operations) {
		return new SecureFeignEncoder(delegate, operations);
	}

	/**
	 * 使用 SDE 解码器包装已有 Feign 解码器。
	 * @param delegate 原始 Feign 解码器
	 * @param operations SDE 客户端操作入口
	 * @return SDE Feign 解码器
	 */
	@Bean
	@ConditionalOnBean(Decoder.class)
	@ConditionalOnMissingBean(SecureFeignDecoder.class)
	public SecureFeignDecoder secureFeignDecoder(Decoder delegate, SecureExchangeOperations operations) {
		return new SecureFeignDecoder(delegate, operations);
	}

}
