package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

class JsonBodyEncoder implements Encoder {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
		try {
			template.header("Content-Type", "application/json");
			template.body(this.objectMapper.writeValueAsBytes(object), StandardCharsets.UTF_8);
		}
		catch (Exception ex) {
			throw new EncodeException("failed to encode json body", ex);
		}
	}

}
