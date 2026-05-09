package com.power4j.fist.sde.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.json.JacksonSecureJsonCodec;
import com.power4j.fist.sde.core.json.SecureJsonCodec;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SDE 编解码组件自动配置。
 * <p>
 * 注册 envelope 编解码器和业务对象 JSON 序列化器，二者均只处理数据结构转换。
 */
@AutoConfiguration(after = SdeCoreAutoConfiguration.class)
@ConditionalOnProperty(prefix = "fist.sde", name = "enabled", havingValue = "true")
public class SdeCodecAutoConfiguration {

	/**
	 * 提供 SDE 编解码使用的 Jackson 对象映射器。
	 * @return Jackson 对象映射器
	 */
	@Bean
	@ConditionalOnMissingBean
	public ObjectMapper secureEnvelopeObjectMapper() {
		return new ObjectMapper();
	}

	/**
	 * 提供 envelope 编解码器。
	 * @param objectMapper Jackson 对象映射器
	 * @return envelope 编解码器
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureEnvelopeCodec secureEnvelopeCodec(ObjectMapper objectMapper) {
		return new JacksonSecureEnvelopeCodec(objectMapper);
	}

	/**
	 * 提供业务对象 JSON 序列化器。
	 * @param objectMapper Jackson 对象映射器
	 * @return JSON 序列化器
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureJsonCodec secureJsonCodec(ObjectMapper objectMapper) {
		return new JacksonSecureJsonCodec(objectMapper);
	}

}
