package com.power4j.fist3.examples.sde.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.context.annotation.Bean;

class DemoSecureFeignConfiguration {

	@Bean
	Encoder demoSecureFeignEncoder(ObjectMapper objectMapper, ExampleFeignEnvelopeSupport support) {
		return new DemoSecureFeignEncoder(objectMapper, support);
	}

	@Bean
	Decoder demoSecureFeignDecoder(ObjectMapper objectMapper, ExampleFeignEnvelopeSupport support) {
		return new DemoSecureFeignDecoder(objectMapper, support);
	}

	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

}
