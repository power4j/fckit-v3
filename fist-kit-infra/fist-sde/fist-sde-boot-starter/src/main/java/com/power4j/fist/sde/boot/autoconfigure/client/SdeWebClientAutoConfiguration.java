package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.webclient.SecureWebClientExchangeFilterFunction;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SdeClientAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
@ConditionalOnBean(SecureExchangeOperations.class)
public class SdeWebClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SecureWebClientExchangeFilterFunction secureWebClientExchangeFilterFunction(
			SecureExchangeOperations operations) {
		return new SecureWebClientExchangeFilterFunction(operations);
	}

	@Bean
	@ConditionalOnMissingBean(name = "secureSdeWebClientCustomizer")
	public WebClientCustomizer secureSdeWebClientCustomizer(SecureWebClientExchangeFilterFunction filterFunction) {
		return (builder) -> builder.filter(filterFunction);
	}

}
