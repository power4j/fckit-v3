package com.power4j.fist.sde.extra.replay;

import com.power4j.fist.sde.core.exception.SecureReplayException;
import com.power4j.fist.sde.core.replay.ReplayContext;
import com.power4j.fist.sde.core.replay.ReplayGuard;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReplayGuard implements ReplayGuard {

	private final Duration window;

	private final ConcurrentHashMap<String, Instant> seen = new ConcurrentHashMap<>();

	public InMemoryReplayGuard(Duration window) {
		this.window = window == null ? Duration.ofMinutes(5) : window;
	}

	@Override
	public void checkAndMark(ReplayContext context) {
		Instant timestamp = parseTimestamp(context.getTimestamp());
		Instant now = Instant.now();
		if (timestamp.isBefore(now.minus(this.window)) || timestamp.isAfter(now.plus(this.window))) {
			throw new SecureReplayException("timestamp is outside replay window");
		}
		cleanup(now);
		String key = context.getKeyRef() + "|" + context.getPolicyId() + "|"
				+ context.getExchangeContext().getScope().getValue() + "|" + context.getNonce();
		Instant previous = this.seen.putIfAbsent(key, timestamp);
		if (previous != null) {
			throw new SecureReplayException("replayed secure envelope nonce");
		}
	}

	private Instant parseTimestamp(String timestamp) {
		try {
			return Instant.parse(timestamp);
		}
		catch (Exception ex) {
			throw new SecureReplayException("timestamp is invalid");
		}
	}

	private void cleanup(Instant now) {
		Iterator<Map.Entry<String, Instant>> iterator = this.seen.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Instant> entry = iterator.next();
			if (entry.getValue().isBefore(now.minus(this.window))) {
				iterator.remove();
			}
		}
	}

}
