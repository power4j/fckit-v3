package com.power4j.fist3.examples.sde.feign;

import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sdeRemoteOrderClient", url = "${sde.example.remote-url}",
		configuration = DemoSecureFeignConfiguration.class)
interface RemoteOrderClient {

	@SecureExchange(value = SdeFeignExampleConfiguration.POLICY_ID, requestBody = SecureInputMode.REQUIRED,
			responseBody = SecureResponseMode.ENABLED)
	@PostMapping("/remote-orders")
	OrderResponse create(@RequestBody OrderRequest request);

}
