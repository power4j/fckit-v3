package com.power4j.fist3.examples.sde.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.client.SecureExchangeClientContext;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
class ExampleSecureEnvelopeClient {

	private final ObjectMapper objectMapper;

	private final SecureExchangeOperations operations;

	ExampleSecureEnvelopeClient(ObjectMapper objectMapper, SecureExchangeOperations operations) {
		this.objectMapper = objectMapper;
		this.operations = operations;
	}

	String encodeRequest(Object body) throws IOException {
		byte[] plain = this.objectMapper.writeValueAsBytes(body);
		return new String(this.operations.encodeRequest(plain, context()), StandardCharsets.UTF_8);
	}

	<T> T decodeResponse(String responseBody, Class<T> responseType) throws IOException {
		byte[] plain = this.operations.decodeResponse(responseBody.getBytes(StandardCharsets.UTF_8), context());
		return this.objectMapper.readValue(plain, responseType);
	}

	String prettyJson(String json) throws IOException {
		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.objectMapper.readTree(json));
	}

	String prettyObject(Object body) throws IOException {
		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
	}

	private SecureExchangeClientContext context() {
		return new SecureExchangeClientContext(SdeWebExampleConfiguration.POLICY_ID,
				SdeWebExampleConfiguration.KEY_REF);
	}

}
