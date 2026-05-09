package com.power4j.fist3.examples.sde.web;

import com.power4j.fist.sde.core.annotation.SecureExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecureExchange(SdeWebExampleConfiguration.POLICY_ID)
class OrderController {

	private static final Logger log = LoggerFactory.getLogger(OrderController.class);

	@PostMapping("/orders")
	OrderResponse create(@RequestBody OrderRequest request) {
		log.info("Controller received decrypted request POJO: {}", request);
		OrderResponse response = new OrderResponse(request.getOrderNo(), "ACCEPTED", request.getAmount());
		log.info("Controller returns response POJO before SDE response advice: {}", response);
		return response;
	}

}
