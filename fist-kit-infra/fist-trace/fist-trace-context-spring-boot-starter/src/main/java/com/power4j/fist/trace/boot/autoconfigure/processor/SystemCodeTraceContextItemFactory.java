package com.power4j.fist.trace.boot.autoconfigure.processor;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.exception.TraceContextConfigurationException;
import com.power4j.fist.trace.context.item.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItem;
import com.power4j.fist.trace.context.item.TraceContextItemFactory;
import com.power4j.fist.trace.context.item.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.item.TraceContextItemSpec;
import com.power4j.fist.trace.context.spi.SystemCodeProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * 系统代码追踪上下文项工厂。
 * <p>
 * 系统代码在 item 创建期解析并冻结，不随运行期 {@link SystemCodeProvider} 返回值变化。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class SystemCodeTraceContextItemFactory implements TraceContextItemFactory {

	public static final String PROCESSOR = "system-code";

	private static final String PROP_VALUE = "value";

	private final Environment environment;

	private final ObjectProvider<SystemCodeProvider> systemCodeProvider;

	public SystemCodeTraceContextItemFactory(Environment environment,
			ObjectProvider<SystemCodeProvider> systemCodeProvider) {
		this.environment = environment;
		this.systemCodeProvider = systemCodeProvider;
	}

	@Override
	public String processor() {
		return PROCESSOR;
	}

	@Override
	public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
		Optional<String> systemCode = this.systemCodeProvider.stream()
			.findFirst()
			.flatMap(SystemCodeProvider::getSystemCode)
			.or(() -> spec.optionalProp(PROP_VALUE))
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
