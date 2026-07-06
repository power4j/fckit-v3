package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.boot.autoconfigure.task.TraceContextTaskDecorator;
import com.power4j.fist.trace.context.TraceContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceContextTaskDecorator} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextTaskDecoratorTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class))
		.withPropertyValues("fist.trace-context.enabled=true");

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		MDC.clear();
	}

	@Test
	void shouldRegisterTaskDecoratorWhenEnabled() {
		this.runner.run((context) -> assertThat(context).hasSingleBean(TaskDecorator.class));
	}

	@Test
	void shouldRestoreSnapshotAndCleanupAfterTask() {
		this.runner.run((context) -> {
			TaskDecorator decorator = context.getBean(TaskDecorator.class);
			AtomicReference<String> taskRequestId = new AtomicReference<>();
			AtomicReference<String> taskMdc = new AtomicReference<>();

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				Runnable task = decorator.decorate(() -> {
					taskRequestId.set(TraceContexts.requireCurrent().getValue("requestId").orElseThrow());
					taskMdc.set(MDC.get("requestId"));
				});
				TraceContexts.clear();

				task.run();
			}

			assertThat(taskRequestId).hasValue("REQ-1");
			assertThat(taskMdc).hasValue("REQ-1");
			assertThat(TraceContexts.current()).isEmpty();
			assertThat(MDC.get("requestId")).isNull();
		});
	}

	@Test
	void shouldComposeUserTaskDecoratorWithTraceTaskDecorator() {
		this.runner.withUserConfiguration(UserTaskDecoratorConfiguration.class).run((context) -> {
			TaskDecorator decorator = context.getBean(TaskDecorator.class);
			AtomicReference<String> taskRequestId = new AtomicReference<>();
			AtomicReference<String> userMarker = new AtomicReference<>();

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				Runnable task = decorator.decorate(() -> {
					taskRequestId.set(TraceContexts.requireCurrent().getValue("requestId").orElseThrow());
					userMarker.set(MDC.get("userMarker"));
				});
				TraceContexts.clear();

				task.run();
			}

			assertThat(taskRequestId).hasValue("REQ-1");
			assertThat(userMarker).hasValue("USER");
			assertThat(MDC.get("userMarker")).isNull();
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class UserTaskDecoratorConfiguration {

		@Bean
		TaskDecorator userTaskDecorator() {
			return (runnable) -> () -> {
				MDC.put("userMarker", "USER");
				try {
					runnable.run();
				}
				finally {
					MDC.remove("userMarker");
				}
			};
		}

	}

}
