package com.power4j.fist3.examples.sde.restclient;

import com.power4j.fist.sde.core.annotation.SecureExchange;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OrderController {

	@PostMapping("/orders")
	@SecureExchange(SdeRestClientExampleConfiguration.POLICY_ID)
	OrderResponse create(@RequestBody OrderRequest request) {
		return new OrderResponse(request.getOrderNo(), "ACCEPTED");
	}

}
