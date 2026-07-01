package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.MapInboundTraceContext;
import com.power4j.fist.trace.context.MapOutboundTraceContext;
import com.power4j.fist.trace.context.MapTraceMdcContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceContextAutoConfiguration} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class));

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void shouldStayDisabledByDefault() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean(TraceContextRuntime.class));
	}

	@Test
	void shouldCreateRuntimeWhenEnabled() {
		this.runner.withPropertyValues("fist.trace-context.enabled=true")
			.run((context) -> assertThat(context).hasSingleBean(TraceContextRuntime.class));
	}

	@Test
	void defaultCorrelationIdShouldGenerateAndRelayRequestId() {
		this.runner.withPropertyValues("fist.trace-context.enabled=true").run((context) -> {
			TraceContextRuntime runtime = context.getBean(TraceContextRuntime.class);
			runtime.inbound(new MapInboundTraceContext(Map.of()));

			MapOutboundTraceContext outbound = new MapOutboundTraceContext();
			MapTraceMdcContext mdc = new MapTraceMdcContext();
			runtime.outbound(outbound);
			runtime.syncMdc(mdc);

			assertThat(outbound.headers()).containsKey("X-REQ-UID");
			assertThat(mdc.values()).containsKey("requestId");
			assertThat(outbound.headers().get("X-REQ-UID")).isEqualTo(mdc.values().get("requestId"));
		});
	}

	@Test
	void defaultSystemCodeShouldReadSpringApplicationName() {
		this.runner.withPropertyValues("fist.trace-context.enabled=true", "spring.application.name=bank-web")
			.run((context) -> {
				TraceContextRuntime runtime = context.getBean(TraceContextRuntime.class);
				runtime.inbound(new MapInboundTraceContext(Map.of()));
				MapTraceMdcContext mdc = new MapTraceMdcContext();
				runtime.syncMdc(mdc);

				assertThat(mdc.values()).containsEntry("systemCode", "bank-web");
			});
	}

}
