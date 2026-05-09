package com.power4j.fist3.examples.sde.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "sde.example", name = "run-on-startup", havingValue = "true", matchIfMissing = true)
class SdeWebDemoRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SdeWebDemoRunner.class);

	private final WebServerApplicationContext server;

	private final ExampleSecureEnvelopeClient client;

	SdeWebDemoRunner(WebServerApplicationContext server, ExampleSecureEnvelopeClient client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		OrderRequest request = new OrderRequest("SDE-WEB-1001", new BigDecimal("128.50"));
		log.info("Client business request POJO:\n{}", this.client.prettyObject(request));
		String requestEnvelope = this.client.encodeRequest(request);
		log.info("Client sends request envelope:\n{}", this.client.prettyJson(requestEnvelope));
		String responseEnvelope = new RestTemplate().postForObject(url(), entity(requestEnvelope), String.class);
		log.info("Client receives response envelope:\n{}", this.client.prettyJson(responseEnvelope));
		OrderResponse response = this.client.decodeResponse(responseEnvelope, OrderResponse.class);
		log.info("Client decrypted response POJO:\n{}", this.client.prettyObject(response));
	}

	private String url() {
		return "http://localhost:" + this.server.getWebServer().getPort() + "/orders";
	}

	private HttpEntity<String> entity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

}
