package com.power4j.fist.trace.context;

import com.power4j.fist.trace.context.carrier.InboundTraceContext;
import com.power4j.fist.trace.context.carrier.MapInboundTraceContext;
import com.power4j.fist.trace.context.carrier.MapOutboundTraceContext;
import com.power4j.fist.trace.context.carrier.MapTraceMdcContext;
import com.power4j.fist.trace.context.exception.TraceContextConfigurationException;
import com.power4j.fist.trace.context.item.AbstractPropDrivenTraceContextItem;
import com.power4j.fist.trace.context.item.AbstractSingleValueTraceContextItem;
import com.power4j.fist.trace.context.item.DefaultTraceContextItemSpec;
import com.power4j.fist.trace.context.item.TraceContextItemSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 追踪上下文项支撑类型测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextItemSupportTest {

	@AfterEach
	void tearDown() {
		TraceContexts.clear();
	}

	@Test
	void specShouldExposeOptionalProps() {
		TraceContextItemSpec spec = new DefaultTraceContextItemSpec("request", "correlation-id", 10,
				Map.of("context-name", "requestId"));

		assertThat(spec.id()).isEqualTo("request");
		assertThat(spec.processor()).isEqualTo("correlation-id");
		assertThat(spec.order()).isEqualTo(10);
		assertThat(spec.optionalProp("context-name")).contains("requestId");
		assertThat(spec.optionalProp("missing")).isEmpty();
	}

	@Test
	void singleValueItemShouldReadHeaderGenerateValueAndWriteOutboundAndMdc() {
		AbstractSingleValueTraceContextItem item = new AbstractSingleValueTraceContextItem("test", "requestId",
				Optional.of("X-REQ-UID"), Optional.of("X-REQ-UID"), Optional.of("requestId")) {
			@Override
			protected Optional<String> generateValue(InboundTraceContext context) {
				return Optional.of("REQ-GEN");
			}
		};
		MapInboundTraceContext inbound = new MapInboundTraceContext(Map.of());

		item.onInbound(inbound);

		try (TraceScope ignored = TraceContexts.restore(TraceContexts.create(inbound.values()).snapshot())) {
			MapOutboundTraceContext outbound = new MapOutboundTraceContext();
			MapTraceMdcContext mdc = new MapTraceMdcContext();
			item.onOutbound(outbound);
			item.onMdc(mdc);

			assertThat(outbound.headers()).containsEntry("X-REQ-UID", "REQ-GEN");
			assertThat(mdc.values()).containsEntry("requestId", "REQ-GEN");
		}
	}

	@Test
	void singleValueItemShouldPreferInboundHeader() {
		AbstractSingleValueTraceContextItem item = new AbstractSingleValueTraceContextItem("test", "requestId",
				Optional.of("X-REQ-UID"), Optional.empty(), Optional.empty()) {
		};
		MapInboundTraceContext inbound = new MapInboundTraceContext(Map.of("X-REQ-UID", "REQ-1"));

		item.onInbound(inbound);

		assertThat(inbound.getValue("requestId")).contains("REQ-1");
	}

	@Test
	void propDrivenItemShouldValidateRequiredContextName() {
		TraceContextItemSpec spec = new DefaultTraceContextItemSpec("request", "correlation-id", 0, Map.of());

		assertThatThrownBy(() -> new AbstractPropDrivenTraceContextItem(spec) {
		}).isInstanceOf(TraceContextConfigurationException.class).hasMessageContaining("context-name");
	}

}
