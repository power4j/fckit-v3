package com.power4j.fist.sde.extra.replay;

import com.power4j.fist.sde.core.exception.SecureReplayException;
import com.power4j.fist.sde.core.replay.ReplayContext;
import com.power4j.fist.sde.core.replay.ReplayGuard;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于本地内存的重放校验实现。
 * <p>
 * 该实现仅在单进程内记录已见 nonce，适合测试、示例和本地验证；多实例生产环境需要替换为共享存储实现。
 */
public class InMemoryReplayGuard implements ReplayGuard {

	private final Duration window;

	private final ConcurrentHashMap<String, Instant> seen = new ConcurrentHashMap<>();

	/**
	 * 创建内存重放校验器。
	 * @param window 时间戳允许窗口，传入 {@code null} 时使用 5 分钟
	 */
	public InMemoryReplayGuard(Duration window) {
		this.window = window == null ? Duration.ofMinutes(5) : window;
	}

	@Override
	public void checkAndMark(ReplayContext context) {
		Duration effectiveWindow = window(context);
		Instant timestamp = parseTimestamp(context.getTimestamp());
		Instant now = Instant.now();
		if (timestamp.isBefore(now.minus(effectiveWindow)) || timestamp.isAfter(now.plus(effectiveWindow))) {
			throw new SecureReplayException("timestamp is outside replay window");
		}
		cleanup(now);
		String key = context.getKeyRef() + "|" + context.getPolicyId() + "|"
				+ context.getExchangeContext().getScope().getValue() + "|" + context.getNonce();
		Instant previous = this.seen.putIfAbsent(key, timestamp.plus(effectiveWindow));
		if (previous != null) {
			throw new SecureReplayException("replayed secure envelope nonce");
		}
	}

	private Duration window(ReplayContext context) {
		if (context.getExchangeContext() != null && context.getExchangeContext().getTimestampWindow() != null) {
			return context.getExchangeContext().getTimestampWindow();
		}
		return this.window;
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
			if (entry.getValue().isBefore(now)) {
				iterator.remove();
			}
		}
	}

}
