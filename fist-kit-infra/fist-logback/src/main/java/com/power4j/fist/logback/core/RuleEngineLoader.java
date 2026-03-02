/*
 * Copyright 2026. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.fist.logback.core;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.api.Transformer;
import com.power4j.fist.logback.spi.DetectorProvider;
import com.power4j.fist.logback.spi.TransformerProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 规则引擎加载器：负责 SPI 加载、配置文件解析与规则组装。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class RuleEngineLoader {

	private final String configFile;

	private final Consumer<String> infoLogger;

	private final Consumer<String> warnLogger;

	public RuleEngineLoader(String configFile, Consumer<String> infoLogger, Consumer<String> warnLogger) {
		this.configFile = configFile;
		this.infoLogger = infoLogger;
		this.warnLogger = warnLogger;
	}

	/**
	 * 加载规则列表。配置文件不存在时返回空列表；SPI 名称冲突时抛出 {@link IllegalStateException}。
	 * @return 按 order 升序、同 order 按名称字典序排列的规则列表
	 */
	public List<ProcessingRule> load() {
		Map<String, DetectorProvider> detectorRegistry = loadDetectorProviders();
		Map<String, TransformerProvider> transformerRegistry = loadTransformerProviders();

		Properties props = loadProperties();
		if (props == null) {
			return Collections.emptyList();
		}

		Set<String> ruleNames = extractRuleNames(props);
		List<ProcessingRule> rules = new ArrayList<>();
		for (String ruleName : ruleNames) {
			ProcessingRule rule = buildRule(ruleName, props, detectorRegistry, transformerRegistry);
			if (rule != null) {
				rules.add(rule);
			}
		}

		rules.sort(Comparator.comparingInt(ProcessingRule::getOrder).thenComparing(ProcessingRule::getName));
		infoLogger.accept("[RuleEngineLoader] loaded " + rules.size() + " rule(s) from " + configFile);
		return Collections.unmodifiableList(rules);
	}

	private Map<String, DetectorProvider> loadDetectorProviders() {
		Map<String, DetectorProvider> registry = new LinkedHashMap<>();
		Iterator<DetectorProvider> it = ServiceLoader.load(DetectorProvider.class).iterator();
		while (it.hasNext()) {
			try {
				DetectorProvider p = it.next();
				if (registry.containsKey(p.name())) {
					throw new IllegalStateException(
							"Duplicate DetectorProvider name: '" + p.name() + "' from " + p.getClass().getName());
				}
				registry.put(p.name(), p);
				infoLogger.accept("[SPI] DetectorProvider: " + p.name() + " -> " + p.getClass().getName());
			}
			catch (ServiceConfigurationError e) {
				warnLogger.accept("[SPI] Failed to load DetectorProvider: " + e.getMessage());
			}
		}
		return registry;
	}

	private Map<String, TransformerProvider> loadTransformerProviders() {
		Map<String, TransformerProvider> registry = new LinkedHashMap<>();
		Iterator<TransformerProvider> it = ServiceLoader.load(TransformerProvider.class).iterator();
		while (it.hasNext()) {
			try {
				TransformerProvider p = it.next();
				if (registry.containsKey(p.name())) {
					throw new IllegalStateException(
							"Duplicate TransformerProvider name: '" + p.name() + "' from " + p.getClass().getName());
				}
				registry.put(p.name(), p);
				infoLogger.accept("[SPI] TransformerProvider: " + p.name() + " -> " + p.getClass().getName());
			}
			catch (ServiceConfigurationError e) {
				warnLogger.accept("[SPI] Failed to load TransformerProvider: " + e.getMessage());
			}
		}
		return registry;
	}

	private Properties loadProperties() {
		try (InputStream is = openConfigStream(configFile)) {
			Properties props = new Properties();
			props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
			return props;
		}
		catch (IOException e) {
			warnLogger.accept("[RuleEngineLoader] config not found or unreadable: " + configFile + " (" + e.getMessage()
					+ "), running with 0 rules");
			return null;
		}
	}

	private InputStream openConfigStream(String path) throws IOException {
		String trimmed = path.trim();
		if (trimmed.startsWith("classpath:")) {
			String resource = trimmed.substring("classpath:".length()).replaceFirst("^/+", "");
			InputStream is = tryClasspath(resource);
			if (is == null) {
				throw new IOException("classpath resource not found: " + resource);
			}
			return is;
		}
		if (trimmed.startsWith("file:")) {
			String filePath = decodeUri(trimmed.substring("file:".length()).trim());
			return new FileInputStream(filePath);
		}
		InputStream is = tryClasspath(trimmed);
		if (is == null) {
			throw new IOException("classpath resource not found: " + trimmed);
		}
		return is;
	}

	private InputStream tryClasspath(String resource) {
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		InputStream is = (tccl != null) ? tccl.getResourceAsStream(resource) : null;
		return (is != null) ? is : RuleEngineLoader.class.getClassLoader().getResourceAsStream(resource);
	}

	private String decodeUri(String raw) {
		try {
			return URLDecoder.decode(raw, StandardCharsets.UTF_8);
		}
		catch (Exception e) {
			return raw;
		}
	}

	private Set<String> extractRuleNames(Properties props) {
		Set<String> names = new LinkedHashSet<>();
		for (String key : props.stringPropertyNames()) {
			if (key.startsWith("rule.")) {
				String[] parts = key.split("\\.", 3);
				if (parts.length >= 3 && parts[1].matches("[A-Za-z0-9_\\-]+")) {
					names.add(parts[1]);
				}
			}
		}
		return names;
	}

	private ProcessingRule buildRule(String ruleName, Properties props, Map<String, DetectorProvider> detectorRegistry,
			Map<String, TransformerProvider> transformerRegistry) {
		String prefix = "rule." + ruleName + ".";
		String detectorType = props.getProperty(prefix + "detector");
		String transformerType = props.getProperty(prefix + "transformer");
		int order = 0;
		String orderStr = props.getProperty(prefix + "order");
		if (orderStr != null) {
			try {
				order = Integer.parseInt(orderStr.trim());
			}
			catch (NumberFormatException e) {
				warnLogger.accept(
						"[RuleEngineLoader] rule '" + ruleName + "' invalid order value: " + orderStr + ", using 0");
			}
		}

		if (detectorType == null || detectorType.isBlank()) {
			warnLogger.accept("[RuleEngineLoader] rule '" + ruleName + "' missing 'detector', skipped");
			return null;
		}
		if (transformerType == null || transformerType.isBlank()) {
			warnLogger.accept("[RuleEngineLoader] rule '" + ruleName + "' missing 'transformer', skipped");
			return null;
		}

		DetectorProvider dp = detectorRegistry.get(detectorType.trim());
		if (dp == null) {
			warnLogger.accept(
					"[RuleEngineLoader] rule '" + ruleName + "' unknown detector type: " + detectorType + ", skipped");
			return null;
		}
		TransformerProvider tp = transformerRegistry.get(transformerType.trim());
		if (tp == null) {
			warnLogger.accept("[RuleEngineLoader] rule '" + ruleName + "' unknown transformer type: " + transformerType
					+ ", skipped");
			return null;
		}

		Map<String, String> ruleProps = extractRuleProps(ruleName, props);
		Detector detector = dp.create();
		Transformer transformer = tp.create();
		try {
			detector.init(ruleProps);
		}
		catch (Exception e) {
			warnLogger.accept(
					"[RuleEngineLoader] rule '" + ruleName + "' detector init failed: " + e.getMessage() + ", skipped");
			return null;
		}
		try {
			transformer.init(ruleProps);
		}
		catch (Exception e) {
			warnLogger.accept("[RuleEngineLoader] rule '" + ruleName + "' transformer init failed: " + e.getMessage()
					+ ", skipped");
			return null;
		}

		infoLogger.accept("[RuleEngineLoader] rule '" + ruleName + "' loaded (order=" + order + ", detector="
				+ detectorType + ", transformer=" + transformerType + ")");
		return new ProcessingRule(ruleName, detector, transformer, order);
	}

	private Map<String, String> extractRuleProps(String ruleName, Properties props) {
		String prefix = "rule." + ruleName + ".";
		Map<String, String> result = new LinkedHashMap<>();
		for (String key : props.stringPropertyNames()) {
			if (key.startsWith(prefix)) {
				result.put(key.substring(prefix.length()), props.getProperty(key));
			}
		}
		return Collections.unmodifiableMap(result);
	}

}
