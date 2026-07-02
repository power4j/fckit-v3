package com.power4j.fist.boot.autoconfigure.web;

import com.power4j.fist.boot.mon.info.TraceInfoResolver;
import com.power4j.fist.boot.web.servlet.trace.logback.HeaderMdcFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FistWebAutoConfiguration} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class FistWebAutoConfigurationTest {

	private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(FistWebAutoConfiguration.class));

	@Test
	void shouldNotRegisterLegacyHeaderMdcFilter() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean("headerMdcFilter")
			.doesNotHaveBean(HeaderMdcFilter.class)
			.doesNotHaveBean(FilterRegistrationBean.class));
	}

	@Test
	void shouldNotRegisterDefaultTraceInfoResolver() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean(TraceInfoResolver.class));
	}

}
