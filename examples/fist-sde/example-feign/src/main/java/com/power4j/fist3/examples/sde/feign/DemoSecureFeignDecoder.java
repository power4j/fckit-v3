package com.power4j.fist3.examples.sde.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureScope;
import feign.Response;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

class DemoSecureFeignDecoder implements Decoder {

	private static final Logger log = LoggerFactory.getLogger(DemoSecureFeignDecoder.class);

	private final ObjectMapper objectMapper;

	private final ExampleFeignEnvelopeSupport support;

	DemoSecureFeignDecoder(ObjectMapper objectMapper, ExampleFeignEnvelopeSupport support) {
		this.objectMapper = objectMapper;
		this.support = support;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		byte[] envelopeBody = response.body().asInputStream().readAllBytes();
		String envelopeJson = new String(envelopeBody, StandardCharsets.UTF_8);
		log.info("Feign decoder response envelope:\n{}", this.support.prettyJson(envelopeJson));
		byte[] plain = this.support.decryptAndVerify(envelopeJson, SecureScope.RESPONSE_BODY,
				SdeFeignExampleConfiguration.POLICY_ID);
		log.info("Feign decoder decrypted response body:\n{}",
				this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.objectMapper.readTree(plain)));
		return this.objectMapper.readValue(plain, this.objectMapper.constructType(type));
	}

}
