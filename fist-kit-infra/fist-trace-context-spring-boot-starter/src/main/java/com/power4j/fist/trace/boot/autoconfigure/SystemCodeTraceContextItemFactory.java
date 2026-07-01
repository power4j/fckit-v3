package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.InboundTraceContext;
import com.power4j.fist.trace.context.SystemCodeProvider;
import com.power4j.fist.trace.context.TraceContextConfigurationException;
import com.power4j.fist.trace.context.TraceContextItem;
import com.power4j.fist.trace.context.TraceContextItemFactory;
import com.power4j.fist.trace.context.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.TraceContextItemSpec;

import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * 系统代码追踪上下文项工厂。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class SystemCodeTraceContextItemFactory implements TraceContextItemFactory {

	public static final String PROCESSOR = "system-code";

	private final Environment environment;

	public SystemCodeTraceContextItemFactory(Environment environment) {
		this.environment = environment;
	}

	@Override
	public String processor() {
		return PROCESSOR;
	}

	@Override
	public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
		Optional<String> systemCode = context.beanLocator()
			.find(SystemCodeProvider.class)
			.flatMap(SystemCodeProvider::getSystemCode)
			.or(() -> Optional.ofNullable(this.environment.getProperty("spring.application.name")));
		systemCode.ifPresent(value -> {
			if (value.isBlank()) {
				throw new TraceContextConfigurationException("System code must not be blank");
			}
		});
		return new AbstractPropDrivenTraceContextItem(spec) {
			@Override
			protected Optional<String> generateValue(InboundTraceContext inbound) {
				return systemCode;
			}
		};
	}

}
