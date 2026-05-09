package com.power4j.fist3.examples.sde.feign;

import com.power4j.fist.sde.core.annotation.SecureExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecureExchange(SdeFeignExampleConfiguration.POLICY_ID)
class RemoteOrderController {

	private static final Logger log = LoggerFactory.getLogger(RemoteOrderController.class);

	@PostMapping("/remote-orders")
	OrderResponse create(@RequestBody OrderRequest request) {
		log.info("Server Controller received decrypted Feign request POJO: {}", request);
		OrderResponse response = new OrderResponse(request.getOrderNo(), "FEIGN_ACCEPTED", request.getAmount());
		log.info("Server Controller returns response POJO before SDE response advice: {}", response);
		return response;
	}

}
