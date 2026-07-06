package com.power4j.fist.trace.context.item;

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

	public TraceContextItemFactoryContext(Supplier<String> idGenerator) {
		this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
	}

	public Supplier<String> idGenerator() {
		return this.idGenerator;
	}

}
