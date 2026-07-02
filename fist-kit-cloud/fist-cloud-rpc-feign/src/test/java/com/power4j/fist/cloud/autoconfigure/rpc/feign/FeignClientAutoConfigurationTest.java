package com.power4j.fist.cloud.autoconfigure.rpc.feign;

import com.power4j.fist.boot.security.context.UserContextHolder;
import com.power4j.fist.boot.security.core.SecurityConstant;
import com.power4j.fist.cloud.rpc.feign.RelayHandler;
import com.power4j.fist.cloud.rpc.feign.RelayInterceptor;
import com.power4j.fist.cloud.rpc.feign.TraceRelayHandler;
import com.power4j.fist.cloud.rpc.feign.UserRelayHandler;
import com.power4j.fist.trace.context.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FeignClientAutoConfiguration} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class FeignClientAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(FeignClientAutoConfiguration.class));

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		UserContextHolder.setOriginalValue(null);
	}

	@Test
	void shouldRegisterSingleRelayInterceptorWithoutTraceRuntime() {
		this.runner.run((context) -> assertThat(context).hasSingleBean(RequestInterceptor.class)
			.hasSingleBean(RelayInterceptor.class)
			.hasSingleBean(UserRelayHandler.class)
			.doesNotHaveBean(TraceRelayHandler.class));
	}

	@Test
	void shouldRegisterTraceRelayHandlerWhenTraceRuntimeExists() {
		this.runner.withUserConfiguration(TraceRuntimeConfiguration.class)
			.run((context) -> assertThat(context).hasSingleBean(RequestInterceptor.class)
				.hasSingleBean(RelayInterceptor.class)
				.hasSingleBean(TraceRelayHandler.class)
				.hasSingleBean(UserRelayHandler.class)
				.getBeans(RelayHandler.class)
				.hasSize(2));
	}

	@Test
	void traceRelayHandlerShouldWriteTraceHeader() {
		this.runner.withUserConfiguration(TraceRuntimeConfiguration.class).run((context) -> {
			RequestInterceptor interceptor = context.getBean(RequestInterceptor.class);
			RequestTemplate template = new RequestTemplate();

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				interceptor.apply(template);
			}

			assertThat(template.headers()).containsKey("X-REQ-UID");
			assertThat(template.headers().get("X-REQ-UID")).containsExactly("REQ-1");
		});
	}

	@Test
	void traceRelayHandlerShouldKeepUserRelayHandler() {
		this.runner.withUserConfiguration(TraceRuntimeConfiguration.class).run((context) -> {
			RequestInterceptor interceptor = context.getBean(RequestInterceptor.class);
			RequestTemplate template = new RequestTemplate();
			UserContextHolder.setOriginalValue("USER-1");

			try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
				interceptor.apply(template);
			}

			assertThat(template.headers().get("X-REQ-UID")).containsExactly("REQ-1");
			assertThat(template.headers().get(SecurityConstant.HEADER_USER_TOKEN_INNER)).containsExactly("USER-1");
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class TraceRuntimeConfiguration {

		@Bean
		TraceContextRuntime traceContextRuntime() {
			return new DefaultTraceContextRuntime(
					new DefaultTraceContextRegistry(List.of(new AbstractSingleValueTraceContextItem("test", "requestId",
							Optional.empty(), Optional.of("X-REQ-UID"), Optional.empty()) {
					})));
		}

	}

}
