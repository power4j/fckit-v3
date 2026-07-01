package com.power4j.fist.trace.context;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 追踪上下文项工厂上下文。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextItemFactoryContext {

	private final Supplier<String> idGenerator;

	private final Clock clock;

	private final TraceContextBeanLocator beanLocator;

	public TraceContextItemFactoryContext(Supplier<String> idGenerator, Clock clock,
			TraceContextBeanLocator beanLocator) {
		this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
		this.clock = Objects.requireNonNull(clock, "clock");
		this.beanLocator = Objects.requireNonNull(beanLocator, "beanLocator");
	}

	public Supplier<String> idGenerator() {
		return this.idGenerator;
	}

	public Clock clock() {
		return this.clock;
	}

	public TraceContextBeanLocator beanLocator() {
		return this.beanLocator;
	}

}
