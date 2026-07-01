package com.power4j.fist.trace.context;

/**
 * 追踪上下文项工厂。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextItemFactory {

	String processor();

	TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context);

}
