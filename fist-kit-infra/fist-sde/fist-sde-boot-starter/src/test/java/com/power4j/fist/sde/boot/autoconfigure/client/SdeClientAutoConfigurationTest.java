package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.feign.SecureFeignDecoder;
import com.power4j.fist.sde.client.feign.SecureFeignEncoder;
import com.power4j.fist.sde.client.restclient.SecureRestClientInterceptor;
import com.power4j.fist.sde.client.webclient.SecureWebClientExchangeFilterFunction;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

class SdeClientAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(com.power4j.fist.sde.boot.autoconfigure.SdeCoreAutoConfiguration.class,
				com.power4j.fist.sde.boot.autoconfigure.SdeCodecAutoConfiguration.class,
				SdeClientAutoConfiguration.class, SdeFeignClientAutoConfiguration.class,
				SdeRestClientAutoConfiguration.class, SdeWebClientAutoConfiguration.class));

	@Test
	void shouldStayDisabledByDefault() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean(SecureExchangeOperations.class));
	}

	@Test
	void shouldRegisterClientOperationsWithoutTestDefaults() {
		this.runner.withPropertyValues("fist.sde.enabled=true", "fist.sde.client.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(SecureExchangeOperations.class)
				.doesNotHaveBean(SecureKeyResolver.class)
				.doesNotHaveBean(ReplayGuard.class));
	}

	@Test
	void shouldRegisterFeignAdaptersWhenDelegateBeansExist() {
		this.runner.withUserConfiguration(FeignDelegateBeans.class)
			.withPropertyValues("fist.sde.enabled=true", "fist.sde.client.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(SecureFeignEncoder.class)
				.hasSingleBean(SecureFeignDecoder.class));
	}

	@Test
	void shouldRegisterRestClientAdapterOnClasspath() {
		this.runner.withPropertyValues("fist.sde.enabled=true", "fist.sde.client.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(SecureRestClientInterceptor.class)
				.hasSingleBean(RestClientCustomizer.class));
	}

	@Test
	void shouldRegisterWebClientAdapterOnClasspath() {
		this.runner.withPropertyValues("fist.sde.enabled=true", "fist.sde.client.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(SecureWebClientExchangeFilterFunction.class)
				.hasSingleBean(WebClientCustomizer.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class FeignDelegateBeans {

		@Bean
		Encoder encoder() {
			return (object, bodyType, template) -> {
			};
		}

		@Bean
		Decoder decoder() {
			return new Decoder() {
				@Override
				public Object decode(Response response, Type type) throws IOException {
					return null;
				}
			};
		}

	}

}
