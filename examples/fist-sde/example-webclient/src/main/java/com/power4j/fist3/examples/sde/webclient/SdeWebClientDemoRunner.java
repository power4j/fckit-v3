package com.power4j.fist3.examples.sde.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "sde.example", name = "run-on-startup", havingValue = "true", matchIfMissing = true)
class SdeWebClientDemoRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SdeWebClientDemoRunner.class);

	private final WebServerApplicationContext server;

	private final WebClient.Builder builder;

	private final ConfigurableApplicationContext applicationContext;

	SdeWebClientDemoRunner(WebServerApplicationContext server, WebClient.Builder builder,
			ConfigurableApplicationContext applicationContext) {
		this.server = server;
		this.builder = builder;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) {
		WebClient client = this.builder.baseUrl(url()).build();
		OrderResponse response = client.post()
			.uri("/orders")
			.bodyValue(new OrderRequest("SDE-WEBCLIENT-1001", new BigDecimal("99.00")))
			.retrieve()
			.bodyToMono(OrderResponse.class)
			.block();
		log.info("WebClient business response: orderNo={}, status={}", response.getOrderNo(), response.getStatus());
		this.applicationContext.close();
	}

	private String url() {
		return "http://localhost:" + this.server.getWebServer().getPort();
	}

}
