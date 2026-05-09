package com.power4j.fist3.examples.sde.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "sde.example", name = "run-on-startup", havingValue = "true", matchIfMissing = true)
class SdeFeignDemoRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SdeFeignDemoRunner.class);

	private final RemoteOrderClient client;

	private final ObjectMapper objectMapper;

	private final ConfigurableApplicationContext applicationContext;

	SdeFeignDemoRunner(RemoteOrderClient client, ObjectMapper objectMapper,
			ConfigurableApplicationContext applicationContext) {
		this.client = client;
		this.objectMapper = objectMapper;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		OrderRequest request = new OrderRequest("SDE-FEIGN-1001", new BigDecimal("256.00"));
		log.info("Feign caller business request POJO:\n{}",
				this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
		OrderResponse response = this.client.create(request);
		log.info("Feign caller business response POJO:\n{}",
				this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
		this.applicationContext.close();
	}

}
