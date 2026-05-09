package com.power4j.fist3.examples.sde.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

class DemoSecureFeignEncoder implements Encoder {

	private static final Logger log = LoggerFactory.getLogger(DemoSecureFeignEncoder.class);

	private final ObjectMapper objectMapper;

	private final ExampleFeignEnvelopeSupport support;

	DemoSecureFeignEncoder(ObjectMapper objectMapper, ExampleFeignEnvelopeSupport support) {
		this.objectMapper = objectMapper;
		this.support = support;
	}

	@Override
	public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
		try {
			Method method = template.methodMetadata().method();
			SecureExchange annotation = method.getAnnotation(SecureExchange.class);
			String policyId = annotation == null || annotation.value().isEmpty()
					? SdeFeignExampleConfiguration.POLICY_ID : annotation.value();
			byte[] plain = this.objectMapper.writeValueAsBytes(object);
			log.info("Feign method {} uses SDE policyId={}", method.getName(), policyId);
			log.info("Feign encoder raw request body:\n{}",
					this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
			String envelope = this.support.encryptAndSign(plain, SecureScope.BODY, policyId);
			log.info("Feign encoder request envelope:\n{}", this.support.prettyJson(envelope));
			template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			template.body(envelope.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		}
		catch (Exception ex) {
			throw new EncodeException("SDE Feign request encoding failed", ex);
		}
	}

}
