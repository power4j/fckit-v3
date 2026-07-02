package com.power4j.fist.trace.context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 追踪上下文项注册表构建器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextRegistryBuilder {

	private final Map<String, TraceContextItemFactory> factories;

	private final TraceContextItemFactoryContext context;

	public TraceContextRegistryBuilder(List<TraceContextItemFactory> factories,
			TraceContextItemFactoryContext context) {
		this.factories = factories.stream()
			.collect(Collectors.toMap(TraceContextItemFactory::processor, Function.identity(), (left, right) -> {
				throw new TraceContextConfigurationException(
						"duplicate trace context item factory processor: " + left.processor());
			}, LinkedHashMap::new));
		this.context = context;
	}

	public TraceContextRegistry build(List<TraceContextItemSpec> specs) {
		List<TraceContextItemSpec> sortedSpecs = specs.stream()
			.sorted(Comparator.comparingInt(TraceContextItemSpec::order).thenComparing(TraceContextItemSpec::id))
			.toList();
		List<TraceContextItem> items = new ArrayList<>(sortedSpecs.size());
		for (TraceContextItemSpec spec : sortedSpecs) {
			TraceContextItemFactory factory = this.factories.get(spec.processor());
			if (factory == null) {
				throw new TraceContextConfigurationException(
						"Missing trace context item factory for processor: " + spec.processor());
			}
			items.add(factory.create(spec, this.context));
		}
		checkSingleValueConflicts(items);
		return new DefaultTraceContextRegistry(items);
	}

	private static void checkSingleValueConflicts(List<TraceContextItem> items) {
		Map<String, String> contextNames = new LinkedHashMap<>();
		Map<String, String> mdcNames = new LinkedHashMap<>();
		Map<String, String> outboundHeaders = new LinkedHashMap<>();
		for (TraceContextItem item : items) {
			if (item instanceof AbstractSingleValueTraceContextItem singleValue) {
				checkUnique(contextNames, "context-name", singleValue.contextName(), item.processor());
				singleValue.mdcName().ifPresent(name -> checkUnique(mdcNames, "mdc-name", name, item.processor()));
				singleValue.outboundHeader()
					.ifPresent(name -> checkUnique(outboundHeaders, "outbound-header", name, item.processor()));
			}
		}
	}

	private static void checkUnique(Map<String, String> names, String kind, String name, String processor) {
		String existing = names.putIfAbsent(name, processor);
		if (existing != null) {
			throw new TraceContextConfigurationException("Duplicate trace context " + kind + " '" + name
					+ "' used by processors '" + existing + "' and '" + processor + "'");
		}
	}

}
