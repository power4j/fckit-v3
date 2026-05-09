package com.power4j.fist3.examples.sde.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "sde.example", name = "run-on-startup", havingValue = "true", matchIfMissing = true)
class SdeRestClientDemoRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SdeRestClientDemoRunner.class);

	private final WebServerApplicationContext server;

	private final RestClient.Builder builder;

	private final ConfigurableApplicationContext applicationContext;

	SdeRestClientDemoRunner(WebServerApplicationContext server, RestClient.Builder builder,
			ConfigurableApplicationContext applicationContext) {
		this.server = server;
		this.builder = builder;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) {
		RestClient client = this.builder.baseUrl(url()).build();
		OrderResponse response = client.post()
			.uri("/orders")
			.body(new OrderRequest("SDE-RESTCLIENT-1001", new BigDecimal("88.00")))
			.retrieve()
			.body(OrderResponse.class);
		log.info("RestClient business response: orderNo={}, status={}", response.getOrderNo(), response.getStatus());
		this.applicationContext.close();
	}

	private String url() {
		return "http://localhost:" + this.server.getWebServer().getPort();
	}

}
