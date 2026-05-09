package com.power4j.fist3.examples.sde.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = SdeWebExampleApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		properties = { "sde.example.run-on-startup=false", "server.port=19080" })
class SdeWebExampleApplicationTest {

	@Autowired
	private ExampleSecureEnvelopeClient client;

	@Test
	void shouldExchangeSecureRequestAndResponseBody() throws Exception {
		String envelope = this.client.encodeRequest(new OrderRequest("TEST-WEB-1", new java.math.BigDecimal("6.00")));
		String responseBody = new RestTemplate().postForObject("http://localhost:19080/orders", entity(envelope),
				String.class);
		OrderResponse response = this.client.decodeResponse(responseBody, OrderResponse.class);
		Assertions.assertThat(response.getStatus()).isEqualTo("ACCEPTED");
	}

	private HttpEntity<String> entity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

}
