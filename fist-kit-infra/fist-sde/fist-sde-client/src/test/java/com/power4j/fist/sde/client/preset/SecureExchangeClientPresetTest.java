package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.crypto.Sm4GcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import com.power4j.fist.sde.extra.signature.HmacSm3SignatureHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SecureExchangeClientPresetTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	@Test
	void shouldRegisterStandardAlgorithmsOnlyWhenImported() {
		this.contextRunner.withUserConfiguration(StandardSecureExchangeClientConfiguration.class).run((context) -> {
			Assertions.assertThat(context).hasBean("standardAesGcmCryptoHandler");
			Assertions.assertThat(context).hasBean("standardHmacSha256SignatureHandler");
			Assertions.assertThat(context).hasBean("standardSecureRandomNonceGenerator");
			Assertions.assertThat(context.getBean("standardAesGcmCryptoHandler", CryptoHandler.class))
				.isInstanceOf(AesGcmCryptoHandler.class);
			Assertions.assertThat(context.getBean("standardHmacSha256SignatureHandler", SignatureHandler.class))
				.isInstanceOf(HmacSha256SignatureHandler.class);
			Assertions.assertThat(context.getBean("standardSecureRandomNonceGenerator", NonceGenerator.class))
				.isInstanceOf(SecureRandomNonceGenerator.class);
			Assertions.assertThat(context).doesNotHaveBean(SecureKeyResolver.class);
			Assertions.assertThat(context).doesNotHaveBean(ReplayGuard.class);
		});
	}

	@Test
	void shouldRegisterGmAlgorithmsOnlyWhenImported() {
		this.contextRunner.withUserConfiguration(GmSecureExchangeClientConfiguration.class).run((context) -> {
			Assertions.assertThat(context).hasBean("gmSm4GcmCryptoHandler");
			Assertions.assertThat(context).hasBean("gmHmacSm3SignatureHandler");
			Assertions.assertThat(context.getBean("gmSm4GcmCryptoHandler", CryptoHandler.class))
				.isInstanceOf(Sm4GcmCryptoHandler.class);
			Assertions.assertThat(context.getBean("gmHmacSm3SignatureHandler", SignatureHandler.class))
				.isInstanceOf(HmacSm3SignatureHandler.class);
			Assertions.assertThat(context).doesNotHaveBean(SecureKeyResolver.class);
			Assertions.assertThat(context).doesNotHaveBean(ReplayGuard.class);
		});
	}

	@Test
	void shouldRegisterTestKeyAndReplayOnlyWhenImported() {
		this.contextRunner
			.withPropertyValues("fist.sde.client.test.key-ref=tenant-a", "fist.sde.client.test.secret=0123456789abcdef")
			.withUserConfiguration(TestSecureExchangeClientConfiguration.class)
			.run((context) -> {
				Assertions.assertThat(context).hasBean("testStaticSecureKeyResolver");
				Assertions.assertThat(context).hasBean("testInMemoryReplayGuard");
				Assertions.assertThat(context.getBean("testStaticSecureKeyResolver", SecureKeyResolver.class))
					.isInstanceOf(StaticSecureKeyResolver.class);
				Assertions.assertThat(context.getBean("testInMemoryReplayGuard", ReplayGuard.class))
					.isInstanceOf(InMemoryReplayGuard.class);
			});
	}

}
