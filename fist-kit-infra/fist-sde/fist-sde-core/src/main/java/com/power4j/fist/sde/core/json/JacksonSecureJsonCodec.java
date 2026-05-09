package com.power4j.fist.sde.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.exception.SecureMessageBindingException;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * 基于 Jackson {@link ObjectMapper} 的 JSON 序列化实现。
 * <p>
 * 该实现用于将业务对象序列化为待加密或待签名的明文字节。
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
