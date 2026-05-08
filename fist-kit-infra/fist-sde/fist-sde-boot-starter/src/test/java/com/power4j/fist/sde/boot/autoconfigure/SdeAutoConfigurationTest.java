package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import com.power4j.fist.sde.web.SecureRequestBodyAdvice;
import com.power4j.fist.sde.web.SecureResponseBodyAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SdeAutoConfigurationTest {

	private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(SdeCoreAutoConfiguration.class, SdeCodecAutoConfiguration.class,
				SdeWebAutoConfiguration.class));

	@Test
	void shouldStayDisabledByDefault() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean(SecureRequestBodyAdvice.class));
	}

	@Test
	void shouldCreateWebAdviceWhenExplicitlyEnabled() {
		this.runner.withUserConfiguration(UserBeans.class)
			.withPropertyValues("fist.sde.enabled=true", "fist.sde.web.enabled=true",
					"fist.sde.web.default-policy-id=body-strict-v1",
					"fist.sde.policies.body-strict-v1.request-body-mode=required",
					"fist.sde.policies.body-strict-v1.response-body-mode=follow-request")
			.run((context) -> assertThat(context).hasSingleBean(SecureRequestBodyAdvice.class)
				.hasSingleBean(SecureResponseBodyAdvice.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class UserBeans {

		@Bean
		CryptoHandler aesGcmCryptoHandler() {
			return new AesGcmCryptoHandler();
		}

		@Bean
		HmacSha256SignatureHandler hmacSha256SignatureHandler() {
			return new HmacSha256SignatureHandler();
		}

		@Bean
		SecureRandomNonceGenerator secureRandomNonceGenerator() {
			return new SecureRandomNonceGenerator();
		}

		@Bean
		InMemoryReplayGuard replayGuard() {
			return new InMemoryReplayGuard(Duration.ofMinutes(5));
		}

		@Bean
		StaticSecureKeyResolver staticSecureKeyResolver() {
			return StaticSecureKeyResolver.symmetric("tenant-a", "0123456789abcdef".getBytes());
		}

	}

}
