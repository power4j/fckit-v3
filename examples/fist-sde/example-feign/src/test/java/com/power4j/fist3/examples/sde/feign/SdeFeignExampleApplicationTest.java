package com.power4j.fist3.examples.sde.feign;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SdeFeignExampleApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		properties = { "sde.example.run-on-startup=false", "server.port=19081",
				"sde.example.remote-url=http://localhost:19081" })
class SdeFeignExampleApplicationTest {

	@Autowired
	private RemoteOrderClient client;

	@Test
	void shouldCallSecureFeignEndpoint() {
		OrderResponse response = this.client.create(new OrderRequest("TEST-FEIGN-1", new java.math.BigDecimal("8.00")));
		org.assertj.core.api.Assertions.assertThat(response.getStatus()).isEqualTo("FEIGN_ACCEPTED");
	}

}
