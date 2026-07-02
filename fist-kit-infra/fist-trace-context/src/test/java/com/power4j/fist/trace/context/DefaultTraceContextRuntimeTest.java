package com.power4j.fist.trace.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultTraceContextRuntime} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class DefaultTraceContextRuntimeTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void inboundShouldRunItemsAndFreezeValues() {
		TraceContextItem first = new TestItem("first") {
			@Override
			public void onInbound(InboundTraceContext context) {
				context.putValue("requestId", "REQ-1");
			}
		};
		TraceContextItem second = new TestItem("second") {
			@Override
			public void onInbound(InboundTraceContext context) {
				context.putValue("derived", context.getValue("requestId").orElseThrow() + "-D");
			}
		};
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(
				new DefaultTraceContextRegistry(List.of(first, second)));
		MapInboundTraceContext inbound = new MapInboundTraceContext(Map.of());

		TraceContext context = runtime.inbound(inbound);
		inbound.putValue("requestId", "REQ-2");

		assertThat(context.getValue("requestId")).contains("REQ-1");
		assertThat(context.getValue("derived")).contains("REQ-1-D");
	}

	@Test
	void outboundShouldSkipWhenCurrentContextMissing() {
		TraceContextItem item = new TestItem("outbound") {
			@Override
			public void onOutbound(OutboundTraceContext context) {
				context.writeHeader("X-REQ-UID", "REQ-1");
			}
		};
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(new DefaultTraceContextRegistry(List.of(item)));
		MapOutboundTraceContext outbound = new MapOutboundTraceContext();

		runtime.outbound(outbound);

		assertThat(outbound.headers()).isEmpty();
	}

	@Test
	void outboundShouldReadCurrentContextValues() {
		TraceContextItem item = new TestItem("outbound") {
			@Override
			public void onOutbound(OutboundTraceContext context) {
				context.writeHeader("X-REQ-UID", context.getValue("requestId").orElseThrow());
			}
		};
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(new DefaultTraceContextRegistry(List.of(item)));
		MapOutboundTraceContext outbound = new MapOutboundTraceContext();

		try (TraceScope ignored = TraceContexts
			.restore(TraceContexts.create(Map.of("requestId", "REQ-1")).snapshot())) {
			runtime.outbound(outbound);
		}

		assertThat(outbound.headers()).containsEntry("X-REQ-UID", "REQ-1");
	}

	@Test
	void syncMdcShouldRunItemsAndWriteCurrentSpan() {
		TraceContextItem item = new TestItem("mdc") {
			@Override
			public void onMdc(TraceMdcContext context) {
				context.putMdc("requestId", context.getValue("requestId").orElseThrow());
			}
		};
		TraceContextRuntime runtime = new DefaultTraceContextRuntime(new DefaultTraceContextRegistry(List.of(item)));
		MapTraceMdcContext mdc = new MapTraceMdcContext();

		try (TraceScope ignored = TraceContexts
			.restore(TraceContexts.create(Map.of("requestId", "REQ-1"), Optional.of("span-1")).snapshot())) {
			runtime.syncMdc(mdc);
		}

		assertThat(mdc.values()).containsEntry("requestId", "REQ-1").containsEntry("spanId", "span-1");
	}

	private abstract static class TestItem implements TraceContextItem {

		private final String processor;

		TestItem(String processor) {
			this.processor = processor;
		}

		@Override
		public String processor() {
			return this.processor;
		}

	}

}
