package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.boot.annotation.TraceSpan;
import com.power4j.fist.trace.boot.annotation.TraceSpanGroup;
import com.power4j.fist.trace.boot.autoconfigure.span.TraceSpanAspect;
import com.power4j.fist.trace.boot.autoconfigure.TraceContextAutoConfiguration;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceSpanAspect} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceSpanAspectTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class))
		.withUserConfiguration(TestSpanConfiguration.class)
		.withPropertyValues("fist.trace-context.enabled=true");

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		MDC.clear();
	}

	@Test
	void shouldPushMethodSpanWithClassGroup() {
		this.runner.run((context) -> {
			TestSpanService service = context.getBean(TestSpanService.class);

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				assertThat(service.submit()).isEqualTo("bank.submit");
				assertThat(TraceContexts.requireCurrent().getSpanId()).isEmpty();
				assertThat(MDC.get("spanId")).isNull();
			}
		});
	}

	@Test
	void shouldNotPrefixAbsoluteMethodSpan() {
		this.runner.run((context) -> {
			TestSpanService service = context.getBean(TestSpanService.class);

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				assertThat(service.absolute()).isEqualTo("remote.query");
			}
		});
	}

	@Test
	void shouldDisableSpanAspectByProperty() {
		this.runner.withPropertyValues("fist.trace-context.span.enabled=false")
			.run((context) -> assertThat(context).doesNotHaveBean(TraceSpanAspect.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestSpanConfiguration {

		@Bean
		TestSpanService testSpanService() {
			return new TestSpanService();
		}

	}

	@TraceSpanGroup("bank")
	public static class TestSpanService {

		@TraceSpan("submit")
		public String submit() {
			assertThat(MDC.get("spanId")).isEqualTo("bank.submit");
			return TraceContexts.requireCurrent().getSpanId().orElseThrow();
		}

		@TraceSpan(".remote.query")
		public String absolute() {
			return TraceContexts.requireCurrent().getSpanId().orElseThrow();
		}

	}

}
