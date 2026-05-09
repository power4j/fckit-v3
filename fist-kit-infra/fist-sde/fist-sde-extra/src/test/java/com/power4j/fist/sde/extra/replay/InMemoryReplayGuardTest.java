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
		ReplayContext context = new ReplayContext(SecureExchangeContext.inbound(SecureScope.BODY), "key-ref", "policy",
				"nonce-1", Instant.now().toString());

		guard.checkAndMark(context);

		assertThatThrownBy(() -> guard.checkAndMark(context)).isInstanceOf(SecureReplayException.class);

		ReplayContext expired = new ReplayContext(SecureExchangeContext.inbound(SecureScope.BODY), "key-ref", "policy",
				"nonce-2", Instant.now().minus(Duration.ofMinutes(10)).toString());
		assertThatThrownBy(() -> guard.checkAndMark(expired)).isInstanceOf(SecureReplayException.class)
			.hasMessageContaining("timestamp");
	}

	@Test
	void shouldUseExchangeTimestampWindowWhenPresent() {
		InMemoryReplayGuard guard = new InMemoryReplayGuard(Duration.ofMinutes(5));
		SecureExchangeContext exchange = SecureExchangeContext.inbound(SecureScope.BODY)
			.withPolicy("strict-policy", null, "key-ref", Duration.ofSeconds(1));
		ReplayContext expired = new ReplayContext(exchange, "key-ref", "strict-policy", "nonce-3",
				Instant.now().minus(Duration.ofSeconds(3)).toString());

		assertThatThrownBy(() -> guard.checkAndMark(expired)).isInstanceOf(SecureReplayException.class)
			.hasMessageContaining("timestamp");
	}

}
