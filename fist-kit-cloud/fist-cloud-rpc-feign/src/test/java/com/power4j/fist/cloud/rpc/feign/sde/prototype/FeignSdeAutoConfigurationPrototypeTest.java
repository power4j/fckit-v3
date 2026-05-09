package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.power4j.fist.cloud.autoconfigure.rpc.feign.FeignClientAutoConfiguration;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class FeignSdeAutoConfigurationPrototypeTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(
				AutoConfigurations.of(FeignClientAutoConfiguration.class, SdeFeignPrototypeConfiguration.class))
		.withUserConfiguration(UserFeignCodecConfiguration.class);

	@Test
	void shouldStayDisabledByDefault() {
		this.runner.run((context) -> {
			assertThat(context).hasSingleBean(Encoder.class);
			assertThat(context).getBean(Encoder.class).isNotInstanceOf(PrototypeSecureFeignEncoder.class);
			assertThat(context).getBean(Decoder.class).isNotInstanceOf(PrototypeSecureFeignDecoder.class);
		});
	}

	@Test
	void shouldWrapExistingEncoderAndDecoderWhenEnabled() {
		this.runner.withPropertyValues("fist.sde.feign.prototype.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(PrototypeSecureFeignEncoder.class)
				.hasSingleBean(PrototypeSecureFeignDecoder.class)
				.hasSingleBean(feign.RequestInterceptor.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class UserFeignCodecConfiguration {

		@Bean
		Encoder jsonBodyEncoder() {
			return new JsonBodyEncoder();
		}

		@Bean
		Decoder jsonNodeDecoder() {
			return new SecureFeignDecoderPrototypeTest.CapturingJsonDecoder();
		}

	}

}
