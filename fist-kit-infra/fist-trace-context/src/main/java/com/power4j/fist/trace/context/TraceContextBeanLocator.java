package com.power4j.fist.trace.context;

import java.util.List;
import java.util.Optional;

/**
 * 外部对象查找抽象。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextBeanLocator {

	<T> Optional<T> find(Class<T> type);

	<T> List<T> findAll(Class<T> type);

	static TraceContextBeanLocator empty() {
		return EmptyTraceContextBeanLocator.INSTANCE;
	}

}
