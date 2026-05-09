package com.power4j.fist.sde.extra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.exception.SecureMessageBindingException;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * 基于 Jackson {@link ObjectMapper} 的 JSON 序列化实现。
 * <p>
 * 该类仅负责将业务响应对象转换为明文字节，反序列化仍交给 Spring MVC 原有 {@code HttpMessageConverter}。
 */
public class JacksonSecureJsonCodec implements SecureJsonCodec {

	private final ObjectMapper objectMapper;

	/**
	 * 创建 JSON 编码器。
	 * @param objectMapper Jackson 对象映射器
	 */
	public JacksonSecureJsonCodec(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public byte[] serialize(@Nullable Object value, @Nullable Type valueType) {
		try {
			return this.objectMapper.writeValueAsBytes(value);
		}
		catch (Exception ex) {
			throw new SecureMessageBindingException("failed to serialize secure response body", ex);
		}
	}

}
