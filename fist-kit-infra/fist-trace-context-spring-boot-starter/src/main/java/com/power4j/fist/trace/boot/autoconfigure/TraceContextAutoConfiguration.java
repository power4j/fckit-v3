package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.DefaultTraceContextItemSpec;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextItemFactory;
import com.power4j.fist.trace.context.TraceContextItemSpec;
import com.power4j.fist.trace.context.TraceContextItemFactoryContext;
import com.power4j.fist.trace.context.TraceContextRegistry;
import com.power4j.fist.trace.context.TraceContextRegistryBuilder;
import com.power4j.fist.trace.context.TraceContextRuntime;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
@EnableAspectJAutoProxy
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
		return new TraceContextItemFactoryContext(() -> UUID.randomUUID().toString(), Clock.systemUTC(),
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
	@ConditionalOnMissingBean
	TraceSpanAspect traceSpanAspect(TraceContextRuntime runtime) {
		return new TraceSpanAspect(runtime);
	}

	private static List<TraceContextItemSpec> toSpecs(TraceContextProperties properties) {
		Map<String, TraceContextProperties.Item> items = properties.getItems().isEmpty() ? defaultItems()
				: properties.getItems();
		List<TraceContextItemSpec> specs = new ArrayList<>();
		items.forEach((id, item) -> {
			if (item.isEnabled()) {
				specs.add(new DefaultTraceContextItemSpec(id, item.getProcessor(), item.getOrder(), item.getProps()));
			}
		});
		return specs;
	}

	private static Map<String, TraceContextProperties.Item> defaultItems() {
		Map<String, TraceContextProperties.Item> items = new LinkedHashMap<>();
		TraceContextProperties.Item correlation = new TraceContextProperties.Item();
		correlation.setProcessor(CorrelationIdTraceContextItemFactory.PROCESSOR);
		correlation.setOrder(0);
		correlation.setProps(Map.of("context-name", "requestId", "inbound-header", "X-REQ-UID", "outbound-header",
				"X-REQ-UID", "mdc-name", "requestId"));
		items.put("correlation", correlation);
		TraceContextProperties.Item systemCode = new TraceContextProperties.Item();
		systemCode.setProcessor(SystemCodeTraceContextItemFactory.PROCESSOR);
		systemCode.setOrder(10);
		systemCode.setProps(Map.of("context-name", "systemCode", "mdc-name", "systemCode"));
		items.put("systemCode", systemCode);
		return items;
	}

}
