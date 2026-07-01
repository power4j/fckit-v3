package com.power4j.fist.trace.context;

/**
 * 追踪上下文作用域。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class TraceScope implements AutoCloseable {

	private final TraceContext previous;

	private boolean closed;

	TraceScope(TraceContext previous) {
		this.previous = previous;
	}

	@Override
	public void close() {
		if (!this.closed) {
			TraceContextHolder.set(this.previous);
			this.closed = true;
		}
	}

}
