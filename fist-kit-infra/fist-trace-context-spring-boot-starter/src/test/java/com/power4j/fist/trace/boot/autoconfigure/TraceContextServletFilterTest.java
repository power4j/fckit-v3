package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.boot.autoconfigure.servlet.TraceContextServletFilter;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TraceContextServletFilter} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextServletFilterTest {

	private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TraceContextAutoConfiguration.class))
		.withPropertyValues("fist.trace-context.enabled=true", "spring.application.name=bank-web");

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		MDC.clear();
	}

	@Test
	void shouldReadHeaderWriteMdcAndCleanup() {
		this.runner.run((context) -> {
			TraceContextServletFilter filter = context.getBean(TraceContextServletFilter.class);
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.addHeader("X-REQ-UID", "REQ-1");
			MockHttpServletResponse response = new MockHttpServletResponse();

			filter.doFilter(request, response, assertInsideChain("REQ-1", "bank-web"));

			assertThat(TraceContexts.current()).isEmpty();
			assertThat(MDC.get("requestId")).isNull();
			assertThat(MDC.get("systemCode")).isNull();
			assertThat(MDC.get("spanId")).isNull();
		});
	}

	@Test
	void shouldGenerateMissingRequestId() {
		this.runner.run((context) -> {
			TraceContextServletFilter filter = context.getBean(TraceContextServletFilter.class);
			MockHttpServletRequest request = new MockHttpServletRequest();

			filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) -> {
				assertThat(TraceContexts.requireCurrent().getValue("requestId")).isPresent();
				assertThat(MDC.get("requestId")).isNotBlank();
			});
		});
	}

	@Test
	void shouldRegisterServletFilterWhenEnabled() {
		this.runner.run((context) -> assertThat(context).hasSingleBean(TraceContextServletFilter.class)
			.hasSingleBean(TraceContextRuntime.class));
	}

	private static FilterChain assertInsideChain(String requestId, String systemCode) {
		return (request, response) -> {
			assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains(requestId);
			assertThat(TraceContexts.requireCurrent().getValue("systemCode")).contains(systemCode);
			assertThat(MDC.get("requestId")).isEqualTo(requestId);
			assertThat(MDC.get("systemCode")).isEqualTo(systemCode);
		};
	}

}
