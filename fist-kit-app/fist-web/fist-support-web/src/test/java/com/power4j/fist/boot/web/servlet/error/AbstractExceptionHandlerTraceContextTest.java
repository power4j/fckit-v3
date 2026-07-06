package com.power4j.fist.boot.web.servlet.error;

import com.power4j.fist.trace.context.item.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.item.CorrelationTraceContextItem;
import com.power4j.fist.trace.context.runtime.DefaultTraceContextRegistry;
import com.power4j.fist.trace.context.runtime.DefaultTraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import com.power4j.fist.boot.web.event.error.HandlerErrorEvent;
import com.power4j.fist.support.spring.util.ApplicationContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AbstractExceptionHandler} 追踪上下文测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class AbstractExceptionHandlerTraceContextTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
		RequestContextHolder.resetRequestAttributes();
		new ApplicationContextHolder().destroy();
	}

	@Test
	void shouldCreateTraceInfoFromTraceContext() {
		TestExceptionHandler handler = new TestExceptionHandler();
		bindRequest();

		try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
			HandlerErrorEvent event = handler.create(new IllegalStateException("failed"));

			assertThat(event.getTraceInfo().getRequestId()).isEqualTo("REQ-1");
		}
	}

	@Test
	void shouldCreateTraceInfoFromConfiguredCorrelationContextName() {
		TestExceptionHandler handler = new TestExceptionHandler();
		bindRequest();
		bindRuntime(new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(new TestCorrelationItem("correlationId")))));

		try (var ignored = TraceContexts.restore(TraceContexts.create(Map.of("correlationId", "REQ-1")).snapshot())) {
			HandlerErrorEvent event = handler.create(new IllegalStateException("failed"));

			assertThat(event.getTraceInfo().getRequestId()).isEqualTo("REQ-1");
		}
	}

	@Test
	void shouldCreateEmptyTraceInfoWithoutTraceContext() {
		TestExceptionHandler handler = new TestExceptionHandler();
		bindRequest();

		HandlerErrorEvent event = handler.create(new IllegalStateException("failed"));

		assertThat(event.getTraceInfo().getRequestId()).isNull();
	}

	private static void bindRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	private static void bindRuntime(DefaultTraceContextRuntime runtime) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(DefaultTraceContextRuntime.class, () -> runtime);
		context.refresh();
		new ApplicationContextHolder().setApplicationContext(context);
	}

	private static class TestExceptionHandler extends AbstractExceptionHandler {

		HandlerErrorEvent create(Throwable throwable) {
			return createErrorEvent(throwable);
		}

	}

	private static class TestCorrelationItem extends AbstractSingleValueTraceContextItem
			implements CorrelationTraceContextItem {

		TestCorrelationItem(String contextName) {
			super("test", contextName, Optional.empty(), Optional.empty(), Optional.empty());
		}

	}

}
