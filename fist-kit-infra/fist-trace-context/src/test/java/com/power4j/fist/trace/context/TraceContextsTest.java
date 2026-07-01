package com.power4j.fist.trace.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TraceContexts} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextsTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void currentShouldExposeReadOnlyValues() {
		TraceContext context = TraceContexts.create(Map.of("requestId", "REQ-1", "systemCode", "bank"));

		try (TraceScope ignored = TraceContexts.restore(context.snapshot())) {
			assertThat(TraceContexts.current()).isPresent();
			assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains("REQ-1");
			assertThat(TraceContexts.requireCurrent().asMap()).containsEntry("systemCode", "bank");
			assertThatThrownBy(() -> TraceContexts.requireCurrent().asMap().put("requestId", "REQ-2"))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}

	@Test
	void restoreShouldRecoverPreviousContextWhenScopeClosed() {
		TraceContext outer = TraceContexts.create(Map.of("requestId", "REQ-1"));
		TraceContext inner = TraceContexts.create(Map.of("requestId", "REQ-2"));

		try (TraceScope ignored = TraceContexts.restore(outer.snapshot())) {
			assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains("REQ-1");
			try (TraceScope ignored2 = TraceContexts.restore(inner.snapshot())) {
				assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains("REQ-2");
			}
			assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains("REQ-1");
		}

		assertThat(TraceContexts.current()).isEmpty();
	}

	@Test
	void captureShouldKeepValuesAndCurrentSpan() {
		TraceContext context = TraceContexts.create(Map.of("requestId", "REQ-1"));

		TraceContextSnapshot snapshot;
		try (TraceScope ignored = TraceContexts.restore(context.snapshot())) {
			try (TraceScope ignored2 = TraceContexts.pushSpan("remote.query")) {
				snapshot = TraceContexts.capture();
			}
		}

		try (TraceScope ignored = TraceContexts.restore(snapshot)) {
			assertThat(TraceContexts.requireCurrent().getValue("requestId")).contains("REQ-1");
			assertThat(TraceContexts.requireCurrent().getSpanId()).contains("remote.query");
		}
	}

	@Test
	void pushSpanShouldRestorePreviousSpan() {
		TraceContext context = TraceContexts.create(Map.of("requestId", "REQ-1"), Optional.of("root"));

		try (TraceScope ignored = TraceContexts.restore(context.snapshot())) {
			assertThat(TraceContexts.requireCurrent().getSpanId()).contains("root");
			try (TraceScope ignored2 = TraceContexts.pushSpan("service.call")) {
				assertThat(TraceContexts.requireCurrent().getSpanId()).contains("service.call");
				try (TraceScope ignored3 = TraceContexts.pushSpan("db.select")) {
					assertThat(TraceContexts.requireCurrent().getSpanId()).contains("db.select");
				}
				assertThat(TraceContexts.requireCurrent().getSpanId()).contains("service.call");
			}
			assertThat(TraceContexts.requireCurrent().getSpanId()).contains("root");
		}
	}

	@Test
	void withSpanShouldReturnSupplierValueAndRestoreSpan() {
		TraceContext context = TraceContexts.create(Map.of("requestId", "REQ-1"), Optional.of("root"));

		try (TraceScope ignored = TraceContexts.restore(context.snapshot())) {
			String value = TraceContexts.withSpan("compute", () -> TraceContexts.requireCurrent()
				.getSpanId()
				.orElseThrow());

			assertThat(value).isEqualTo("compute");
			assertThat(TraceContexts.requireCurrent().getSpanId()).contains("root");
		}
	}

}
