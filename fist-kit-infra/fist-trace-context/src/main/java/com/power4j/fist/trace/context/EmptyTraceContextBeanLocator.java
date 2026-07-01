package com.power4j.fist.trace.context;

import java.util.List;
import java.util.Optional;

enum EmptyTraceContextBeanLocator implements TraceContextBeanLocator {

	INSTANCE;

	@Override
	public <T> Optional<T> find(Class<T> type) {
		return Optional.empty();
	}

	@Override
	public <T> List<T> findAll(Class<T> type) {
		return List.of();
	}

}
