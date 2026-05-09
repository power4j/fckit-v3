package com.power4j.fist3.examples.sde.server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = SdeServerExampleApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		properties = { "sde.example.run-on-startup=false", "server.port=19080" })
@ExtendWith(OutputCaptureExtension.class)
class SdeServerExampleApplicationTest {

	@Autowired
	private ExampleSecureEnvelopeClient client;

	@Test
	void shouldExchangeSecureRequestAndResponseBody(CapturedOutput output) throws Exception {
		String envelope = this.client.encodeRequest(new OrderRequest("TEST-WEB-1", new java.math.BigDecimal("6.00")));
		String responseBody = new RestTemplate().postForObject("http://localhost:19080/orders", entity(envelope),
				String.class);
		OrderResponse response = this.client.decodeResponse(responseBody, OrderResponse.class);
		Assertions.assertThat(response.getStatus()).isEqualTo("ACCEPTED");
		Assertions.assertThat(output).contains("Web client raw request body:");
		Assertions.assertThat(output).contains("Web client request envelope:");
	}

	private HttpEntity<String> entity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

}
