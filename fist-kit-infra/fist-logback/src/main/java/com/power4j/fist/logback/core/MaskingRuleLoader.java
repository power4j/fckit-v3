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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 从 properties 文件加载脱敏规则。
 * <p>
 * 内置规则（phone/email/idcard/bankcard）默认全部启用，可通过配置文件关闭： <pre>{@code
 * builtin.phone.enabled=false
 * }</pre>
 * <p>
 * 自定义规则示例： <pre>{@code
 * rule.apikey.pattern=(?i)(api[_-]?key\s*[=:]\s*)\S+
 * rule.apikey.strategy=REPLACE
 * rule.apikey.replacement=$1***
 * }</pre>
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.11
 */
public class MaskingRuleLoader {

	private static final Map<String, MaskRule> BUILT_IN;

	static {
		Map<String, MaskRule> m = new LinkedHashMap<>();
		// 手机号：保留前3位+后4位，中间4位打码
		m.put("phone", new MaskRule("phone", Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})"), MaskStrategy.REPLACE,
				"$1****$2", 0, 0, '*'));
		// 邮箱：保留首字符+域名
		m.put("email",
				new MaskRule("email",
						Pattern.compile("([a-zA-Z0-9])[a-zA-Z0-9._%+\\-]*(@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})"),
						MaskStrategy.REPLACE, "$1***$2", 0, 0, '*'));
		// 身份证：保留前6位+后4位
		m.put("idcard", new MaskRule("idcard", Pattern.compile("(\\d{6})\\d{8}(\\d{4})"), MaskStrategy.REPLACE,
				"$1********$2", 0, 0, '*'));
		// 银行卡：保留前4位+后4位
		m.put("bankcard", new MaskRule("bankcard", Pattern.compile("(\\d{4})\\d{5,11}(\\d{4})"), MaskStrategy.REPLACE,
				"$1****$2", 0, 0, '*'));
		BUILT_IN = Collections.unmodifiableMap(m);
	}

	private final String configFile;

	public MaskingRuleLoader(String configFile) {
		this.configFile = configFile;
	}

	public List<MaskRule> load() {
		Properties props = loadProperties();
		List<MaskRule> rules = new ArrayList<>();

		for (Map.Entry<String, MaskRule> entry : BUILT_IN.entrySet()) {
			if (!"false".equalsIgnoreCase(props.getProperty("builtin." + entry.getKey() + ".enabled", "true"))) {
				rules.add(entry.getValue());
			}
		}

		Set<String> names = new LinkedHashSet<>();
		for (String key : props.stringPropertyNames()) {
			if (key.startsWith("rule.") && key.endsWith(".pattern")) {
				names.add(key.substring(5, key.length() - 8));
			}
		}
		for (String name : names) {
			MaskRule rule = buildCustomRule(name, props);
			if (rule != null) {
				rules.add(rule);
			}
		}

		return rules;
	}

	private MaskRule buildCustomRule(String name, Properties props) {
		String patternStr = props.getProperty("rule." + name + ".pattern");
		if (patternStr == null) {
			return null;
		}
		try {
			MaskStrategy strategy = MaskStrategy
				.valueOf(props.getProperty("rule." + name + ".strategy", "REPLACE").toUpperCase());
			String replacement = props.getProperty("rule." + name + ".replacement", "***");
			int keepHead = Integer.parseInt(props.getProperty("rule." + name + ".keepFirst", "0"));
			int keepTail = Integer.parseInt(props.getProperty("rule." + name + ".keepLast", "0"));
			char maskChar = props.getProperty("rule." + name + ".maskChar", "*").charAt(0);
			return new MaskRule(name, Pattern.compile(patternStr), strategy, replacement, keepHead, keepTail, maskChar);
		}
		catch (Exception e) {
			return null;
		}
	}

	private Properties loadProperties() {
		Properties props = new Properties();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = cl != null ? cl.getResourceAsStream(configFile) : null;
		if (is == null) {
			is = MaskingRuleLoader.class.getClassLoader().getResourceAsStream(configFile);
		}
		if (is != null) {
			try (InputStream stream = is) {
				props.load(stream);
			}
			catch (IOException ignored) {
			}
		}
		return props;
	}

}
