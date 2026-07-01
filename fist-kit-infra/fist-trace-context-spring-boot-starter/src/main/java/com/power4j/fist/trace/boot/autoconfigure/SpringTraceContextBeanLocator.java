package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.TraceContextBeanLocator;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

class SpringTraceContextBeanLocator implements TraceContextBeanLocator {

	private final ApplicationContext applicationContext;

	SpringTraceContextBeanLocator(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public <T> Optional<T> find(Class<T> type) {
		return this.applicationContext.getBeansOfType(type).values().stream().findFirst();
	}

	@Override
	public <T> List<T> findAll(Class<T> type) {
		return List.copyOf(this.applicationContext.getBeansOfType(type).values());
	}

}
