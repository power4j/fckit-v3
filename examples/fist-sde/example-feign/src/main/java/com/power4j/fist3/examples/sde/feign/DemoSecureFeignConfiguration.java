package com.power4j.fist3.examples.sde.feign;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.feign.SecureFeignDecoder;
import com.power4j.fist.sde.client.feign.SecureFeignEncoder;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

class DemoSecureFeignConfiguration {

	@Bean
	Encoder demoSecureFeignEncoder(ObjectFactory<HttpMessageConverters> messageConverters,
			SecureExchangeOperations operations) {
		return new SecureFeignEncoder(new SpringEncoder(messageConverters), operations);
	}

	@Bean
	Decoder demoSecureFeignDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
			SecureExchangeOperations operations) {
		return new SecureFeignDecoder(new SpringDecoder(messageConverters), operations);
	}

	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

}
