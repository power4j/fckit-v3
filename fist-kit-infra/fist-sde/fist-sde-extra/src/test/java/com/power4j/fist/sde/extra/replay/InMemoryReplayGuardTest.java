package com.power4j.fist.sde.extra.replay;

import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.exception.SecureReplayException;
import com.power4j.fist.sde.core.replay.ReplayContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryReplayGuardTest {

	@Test
	void shouldRejectReplayAndExpiredTimestamp() {
		InMemoryReplayGuard guard = new InMemoryReplayGuard(Duration.ofMinutes(5));
		ReplayContext context = ReplayContext.builder()
			.exchangeContext(SecureExchangeContext.inbound(SecureScope.BODY))
			.keyRef("key-ref")
			.policyId("policy")
			.nonce("nonce-1")
			.timestamp(Instant.now().toString())
			.build();

		guard.checkAndMark(context);

		assertThatThrownBy(() -> guard.checkAndMark(context)).isInstanceOf(SecureReplayException.class);

		ReplayContext expired = ReplayContext.builder()
			.exchangeContext(SecureExchangeContext.inbound(SecureScope.BODY))
			.keyRef("key-ref")
			.policyId("policy")
			.nonce("nonce-2")
			.timestamp(Instant.now().minus(Duration.ofMinutes(10)).toString())
			.build();
		assertThatThrownBy(() -> guard.checkAndMark(expired)).isInstanceOf(SecureReplayException.class)
			.hasMessageContaining("timestamp");
	}

	@Test
	void shouldUseExchangeTimestampWindowWhenPresent() {
		InMemoryReplayGuard guard = new InMemoryReplayGuard(Duration.ofMinutes(5));
		SecureExchangeContext exchange = SecureExchangeContext.inbound(SecureScope.BODY)
			.withPolicy("strict-policy", null, "key-ref", Duration.ofSeconds(1));
		ReplayContext expired = ReplayContext.builder()
			.exchangeContext(exchange)
			.keyRef("key-ref")
			.policyId("strict-policy")
			.nonce("nonce-3")
			.timestamp(Instant.now().minus(Duration.ofSeconds(3)).toString())
			.build();

		assertThatThrownBy(() -> guard.checkAndMark(expired)).isInstanceOf(SecureReplayException.class)
			.hasMessageContaining("timestamp");
	}

}
