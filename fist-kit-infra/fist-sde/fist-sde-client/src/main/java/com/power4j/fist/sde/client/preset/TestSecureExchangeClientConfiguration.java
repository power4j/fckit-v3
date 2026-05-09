package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class TestSecureExchangeClientConfiguration {

	@Bean("testStaticSecureKeyResolver")
	@ConditionalOnMissingBean(name = "testStaticSecureKeyResolver")
	public SecureKeyResolver testStaticSecureKeyResolver(@Value("${fist.sde.client.test.key-ref:test}") String keyRef,
			@Value("${fist.sde.client.test.secret:0123456789abcdef}") String secret) {
		return StaticSecureKeyResolver.symmetric(keyRef, secret.getBytes(StandardCharsets.UTF_8));
	}

	@Bean("testInMemoryReplayGuard")
	@ConditionalOnMissingBean(name = "testInMemoryReplayGuard")
	public ReplayGuard testInMemoryReplayGuard(
			@Value("${fist.sde.client.test.replay-window:PT5M}") String replayWindow) {
		return new InMemoryReplayGuard(Duration.parse(replayWindow));
	}

}
