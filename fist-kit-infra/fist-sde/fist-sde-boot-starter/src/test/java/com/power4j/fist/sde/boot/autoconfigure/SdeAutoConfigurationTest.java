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

	@Test
	void shouldRejectCryptoWithoutSignature() {
		this.runner
			.withPropertyValues("fist.sde.enabled=true", "fist.sde.web.default-policy-id=invalid-v1",
					"fist.sde.policies.invalid-v1.request-body-mode=required",
					"fist.sde.policies.invalid-v1.crypto-enabled=true",
					"fist.sde.policies.invalid-v1.signature-enabled=false")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasMessageContaining("signature must be enabled when crypto is enabled"));
	}

	@Test
	void shouldRejectDisabledCryptoAndSignatureForSecureExchange() {
		this.runner
			.withPropertyValues("fist.sde.enabled=true", "fist.sde.web.default-policy-id=invalid-v1",
					"fist.sde.policies.invalid-v1.request-body-mode=required",
					"fist.sde.policies.invalid-v1.crypto-enabled=false",
					"fist.sde.policies.invalid-v1.signature-enabled=false")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasMessageContaining("crypto and signature cannot both be disabled for secure exchange"));
	}

	@Test
	void shouldAllowSignOnlyPolicy() {
		this.runner
			.withPropertyValues("fist.sde.enabled=true", "fist.sde.web.default-policy-id=sign-only-v1",
					"fist.sde.policies.sign-only-v1.request-body-mode=required",
					"fist.sde.policies.sign-only-v1.crypto-enabled=false",
					"fist.sde.policies.sign-only-v1.signature-enabled=true")
			.run((context) -> assertThat(context).hasNotFailed());
	}

	@Test
	void shouldAllowPlainPolicyWithoutCryptoAndSignature() {
		this.runner.withPropertyValues("fist.sde.enabled=true", "fist.sde.web.default-policy-id=plain-v1",
				"fist.sde.policies.plain-v1.request-body-mode=plain",
				"fist.sde.policies.plain-v1.response-body-mode=disabled",
				"fist.sde.policies.plain-v1.crypto-enabled=false", "fist.sde.policies.plain-v1.signature-enabled=false")
			.run((context) -> assertThat(context).hasNotFailed());
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
