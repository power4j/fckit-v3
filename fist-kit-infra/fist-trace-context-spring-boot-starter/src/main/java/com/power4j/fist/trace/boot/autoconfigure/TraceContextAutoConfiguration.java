package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.DefaultTraceContextItemSpec;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextItemFactory;
import com.power4j.fist.trace.context.TraceContextItemSpec;
import com.power4j.fist.trace.context.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.TraceContextRegistry;
import com.power4j.fist.trace.context.TraceContextRegistryBuilder;
import com.power4j.fist.trace.context.TraceContextRuntime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 追踪上下文自动配置。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@AutoConfiguration
@EnableConfigurationProperties(TraceContextProperties.class)
@ConditionalOnProperty(prefix = TraceContextProperties.PREFIX, name = "enabled", havingValue = "true")
@Import(TraceSpanAopConfiguration.class)
public class TraceContextAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	CorrelationIdTraceContextItemFactory correlationIdTraceContextItemFactory() {
		return new CorrelationIdTraceContextItemFactory();
	}

	@Bean
	@ConditionalOnMissingBean
	SystemCodeTraceContextItemFactory systemCodeTraceContextItemFactory(Environment environment) {
		return new SystemCodeTraceContextItemFactory(environment);
	}

	@Bean
	@ConditionalOnMissingBean
	TraceContextItemFactoryContext traceContextItemFactoryContext(ApplicationContext applicationContext) {
		return new TraceContextItemFactoryContext(() -> UUID.randomUUID().toString(),
				new SpringTraceContextBeanLocator(applicationContext));
	}

	@Bean
	@ConditionalOnMissingBean
	TraceContextRegistry traceContextRegistry(TraceContextProperties properties,
			List<TraceContextItemFactory> factories, TraceContextItemFactoryContext context) {
		return new TraceContextRegistryBuilder(factories, context).build(toSpecs(properties));
	}

	@Bean
	@ConditionalOnMissingBean
	TraceContextRuntime traceContextRuntime(TraceContextRegistry registry) {
		return new DefaultTraceContextRuntime(registry);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	TraceContextServletFilter traceContextServletFilter(TraceContextRuntime runtime) {
		return new TraceContextServletFilter(runtime);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(RestClient.class)
	TraceContextRestClientInterceptor traceContextRestClientInterceptor(TraceContextRuntime runtime) {
		return new TraceContextRestClientInterceptor(runtime);
	}

	@Bean
	@ConditionalOnMissingBean(name = "traceContextRestClientCustomizer")
	@ConditionalOnClass(RestClient.class)
	RestClientCustomizer traceContextRestClientCustomizer(TraceContextRestClientInterceptor interceptor) {
		return builder -> builder.requestInterceptor(interceptor);
	}

	@Bean
	@ConditionalOnMissingBean
	TaskDecorator traceContextTaskDecorator(TraceContextRuntime runtime) {
		return new TraceContextTaskDecorator(runtime);
	}

	@Bean
	static BeanPostProcessor traceContextTaskDecoratorBeanPostProcessor(ObjectProvider<TraceContextRuntime> runtime) {
		return new TraceContextTaskDecoratorBeanPostProcessor(runtime);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = TraceContextProperties.PREFIX + ".span", name = "enabled", havingValue = "true",
			matchIfMissing = true)
	TraceSpanAspect traceSpanAspect(TraceContextRuntime runtime) {
		return new TraceSpanAspect(runtime);
	}

	private static List<TraceContextItemSpec> toSpecs(TraceContextProperties properties) {
		Map<String, TraceContextProperties.Item> items = properties.getItems();
		if (items.isEmpty()) {
			return defaultSpecs();
		}
		List<TraceContextItemSpec> specs = new ArrayList<>();
		items.forEach((id, item) -> {
			if (item.isEnabled()) {
				specs.add(new DefaultTraceContextItemSpec(id, item.getProcessor(), item.getOrder(), item.getProps()));
			}
		});
		return specs;
	}

	private static List<TraceContextItemSpec> defaultSpecs() {
		return List.of(
				new DefaultTraceContextItemSpec("correlation", CorrelationIdTraceContextItemFactory.PROCESSOR, 0,
						Map.of("context-name", "requestId", "inbound-header", "X-REQ-UID", "outbound-header",
								"X-REQ-UID", "mdc-name", "requestId")),
				new DefaultTraceContextItemSpec("systemCode", SystemCodeTraceContextItemFactory.PROCESSOR, 10,
						Map.of("context-name", "systemCode", "mdc-name", "systemCode")));
	}

}
