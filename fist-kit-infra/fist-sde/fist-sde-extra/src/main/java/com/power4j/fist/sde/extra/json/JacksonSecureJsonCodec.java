package com.power4j.fist.sde.extra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.exception.SecureMessageBindingException;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

public class JacksonSecureJsonCodec implements SecureJsonCodec {

	private final ObjectMapper objectMapper;

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
