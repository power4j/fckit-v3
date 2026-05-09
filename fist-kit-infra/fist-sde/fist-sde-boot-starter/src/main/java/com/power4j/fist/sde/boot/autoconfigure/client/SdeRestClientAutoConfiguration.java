package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.restclient.SecureRestClientInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SdeClientAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.web.client.RestClient")
@ConditionalOnBean(SecureExchangeOperations.class)
public class SdeRestClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SecureRestClientInterceptor secureRestClientInterceptor(SecureExchangeOperations operations) {
		return new SecureRestClientInterceptor(operations);
	}

	@Bean
	@ConditionalOnMissingBean(name = "secureSdeRestClientCustomizer")
	public RestClientCustomizer secureSdeRestClientCustomizer(SecureRestClientInterceptor interceptor) {
		return (builder) -> builder.requestInterceptor(interceptor);
	}

}
