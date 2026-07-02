package com.power4j.fist.trace.context;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TraceContextRegistryBuilder} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class TraceContextRegistryBuilderTest {

	private final TraceContextItemFactoryContext context = new TraceContextItemFactoryContext(() -> "ID-1",
			TraceContextBeanLocator.empty());

	@Test
	void buildShouldSortByOrderThenId() {
		TraceContextItemFactory factory = new TestFactory();
		List<TraceContextItemSpec> specs = List.of(spec("b", 0, "b"), spec("a", 0, "a"), spec("c", -1, "c"));

		TraceContextRegistry registry = new TraceContextRegistryBuilder(List.of(factory), this.context).build(specs);

		assertThat(registry.items()).extracting(item -> ((AbstractSingleValueTraceContextItem) item).contextName())
			.containsExactly("c", "a", "b");
	}

	@Test
	void buildShouldRejectMissingFactory() {
		TraceContextRegistryBuilder builder = new TraceContextRegistryBuilder(List.of(), this.context);

		assertThatThrownBy(() -> builder.build(List.of(spec("a", 0, "a"))))
			.isInstanceOf(TraceContextConfigurationException.class)
			.hasMessageContaining("processor");
	}

	@Test
	void buildShouldRejectDuplicateFactoryProcessor() {
		TraceContextItemFactory factory = new TestFactory();

		assertThatThrownBy(() -> new TraceContextRegistryBuilder(List.of(factory, factory), this.context))
			.isInstanceOf(TraceContextConfigurationException.class)
			.hasMessageContaining("duplicate");
	}

	@Test
	void buildShouldRejectDuplicateContextName() {
		TraceContextRegistryBuilder builder = new TraceContextRegistryBuilder(List.of(new TestFactory()), this.context);

		assertThatThrownBy(() -> builder.build(List.of(spec("a", 0, "same"), spec("b", 1, "same"))))
			.isInstanceOf(TraceContextConfigurationException.class)
			.hasMessageContaining("context-name");
	}

	@Test
	void buildShouldRejectDuplicateMdcName() {
		TraceContextRegistryBuilder builder = new TraceContextRegistryBuilder(List.of(new TestFactory()), this.context);

		assertThatThrownBy(
				() -> builder.build(List.of(spec("a", 0, "a", "sameMdc", "A"), spec("b", 1, "b", "sameMdc", "B"))))
			.isInstanceOf(TraceContextConfigurationException.class)
			.hasMessageContaining("mdc-name");
	}

	@Test
	void buildShouldRejectDuplicateOutboundHeader() {
		TraceContextRegistryBuilder builder = new TraceContextRegistryBuilder(List.of(new TestFactory()), this.context);

		assertThatThrownBy(
				() -> builder.build(List.of(spec("a", 0, "a", "mdcA", "X-ID"), spec("b", 1, "b", "mdcB", "X-ID"))))
			.isInstanceOf(TraceContextConfigurationException.class)
			.hasMessageContaining("outbound-header");
	}

	private static TraceContextItemSpec spec(String id, int order, String contextName) {
		return spec(id, order, contextName, id + "Mdc", id + "Header");
	}

	private static TraceContextItemSpec spec(String id, int order, String contextName, String mdcName,
			String outboundHeader) {
		return new DefaultTraceContextItemSpec(id, "test", order,
				Map.of("context-name", contextName, "mdc-name", mdcName, "outbound-header", outboundHeader));
	}

	private static class TestFactory implements TraceContextItemFactory {

		@Override
		public String processor() {
			return "test";
		}

		@Override
		public TraceContextItem create(TraceContextItemSpec spec, TraceContextItemFactoryContext context) {
			return new AbstractSingleValueTraceContextItem(spec.processor(),
					spec.optionalProp("context-name").orElseThrow(), Optional.empty(),
					spec.optionalProp("outbound-header"), spec.optionalProp("mdc-name")) {
			};
		}

	}

}
