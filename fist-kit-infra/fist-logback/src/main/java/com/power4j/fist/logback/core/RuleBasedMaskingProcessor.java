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

import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;

import java.util.List;
import java.util.Map;

/**
 * 基于规则的脱敏处理器，通过 SPI 自动注册。
 * <p>
 * 规则来源（优先级递增）：
 * <ol>
 * <li>内置规则（phone/email/idcard/bankcard），默认全部启用。</li>
 * <li>classpath 根目录下的 {@code logback-masking.properties}（默认）。</li>
 * <li>通过 {@code %mask{configFile=path}} 指定的自定义配置文件。</li>
 * </ol>
 * <p>
 * logback.xml 配置示例： <pre>{@code
 * <!-- 使用默认配置文件 -->
 * <pattern>%d - %mask%n</pattern>
 *
 * <!-- 指定自定义配置文件 -->
 * <pattern>%d - %mask{configFile=custom-masking.properties}%n</pattern>
 * }</pre>
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.11
 */
public class RuleBasedMaskingProcessor implements MessageProcessor {

	static final String CONFIG_FILE_KEY = "configFile";

	static final String DEFAULT_CONFIG = "logback-masking.properties";

	private volatile List<MaskRule> rules;

	@Override
	public void configure(Map<String, String> options) {
		String configFile = options.getOrDefault(CONFIG_FILE_KEY, DEFAULT_CONFIG);
		this.rules = new MaskingRuleLoader(configFile).load();
	}

	@Override
	public String process(String message, LogMessageContext context) {
		String result = message;
		for (MaskRule rule : getRules()) {
			result = rule.apply(result);
		}
		return result;
	}

	private List<MaskRule> getRules() {
		if (rules == null) {
			synchronized (this) {
				if (rules == null) {
					rules = new MaskingRuleLoader(DEFAULT_CONFIG).load();
				}
			}
		}
		return rules;
	}

}
