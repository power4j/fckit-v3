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

import ch.qos.logback.core.spi.ContextAwareBase;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MatchSpan;
import com.power4j.fist.logback.api.MessageProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎：实现 {@link MessageProcessor}，按规则列表串行执行脱敏。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class RuleEngine extends ContextAwareBase implements MessageProcessor {

	private List<ProcessingRule> rules = Collections.emptyList();

	/** 仅供测试使用，直接注入规则列表。 */
	void setRules(List<ProcessingRule> rules) {
		this.rules = rules;
	}

	@Override
	public String name() {
		return "RuleEngine";
	}

	@Override
	public void init(Map<String, String> options) {
		String configFile = options.getOrDefault("configFile", "logback-masking.properties");
		RuleEngineLoader loader = new RuleEngineLoader(configFile, this::addInfo, this::addWarn);
		try {
			this.rules = loader.load();
			addInfo("RuleEngine loaded " + rules.size() + " rule(s)");
		}
		catch (IllegalStateException e) {
			addError("RuleEngine init failed: " + e.getMessage(), e);
			throw e;
		}
		catch (Exception e) {
			addWarn("RuleEngine failed to load config, running with 0 rules: " + e.getMessage());
		}
	}

	@Override
	public String process(String message, LogMessageContext context) {
		String result = message;
		for (ProcessingRule rule : rules) {
			result = applyRule(rule, result, context);
		}
		return result;
	}

	@Override
	public void destroy() {
		for (ProcessingRule rule : rules) {
			try {
				rule.getDetector().destroy();
			}
			catch (Throwable ignored) {
			}
			try {
				rule.getTransformer().destroy();
			}
			catch (Throwable ignored) {
			}
		}
	}

	private String applyRule(ProcessingRule rule, String message, LogMessageContext context) {
		List<MatchSpan> rawSpans;
		try {
			rawSpans = rule.getDetector().detect(message, context);
			if (rawSpans == null) {
				addWarn("[" + rule.getName() + "] detector returned null, skipping");
				return message;
			}
		}
		catch (Throwable t) {
			addWarn("[" + rule.getName() + "] detector threw exception, skipping: " + t.getMessage());
			return message;
		}

		int len = message.length();
		List<MatchSpan> valid = new ArrayList<>();
		for (MatchSpan s : rawSpans) {
			if (s.start() < 0 || s.end() > len) {
				addWarn("[" + rule.getName() + "] invalid span [" + s.start() + "," + s.end() + "), discarded");
			}
			else if (s.start() < s.end()) {
				valid.add(s);
			}
		}

		if (valid.isEmpty()) {
			return message;
		}

		// start ASC, end DESC
		valid.sort(Comparator.comparingInt(MatchSpan::start).thenComparingInt((MatchSpan s) -> -s.end()));
		List<MatchSpan> deduped = greedyDedup(valid);

		List<String> replacements = new ArrayList<>(deduped.size());
		for (MatchSpan span : deduped) {
			try {
				String rep = rule.getTransformer().transform(span.extract(message), context);
				if (rep == null) {
					addWarn("[" + rule.getName() + "] transformer returned null, keeping original");
					replacements.add(span.extract(message));
				}
				else {
					replacements.add(rep);
				}
			}
			catch (Throwable t) {
				replacements.add(span.extract(message));
				addWarn("[" + rule.getName() + "] transformer threw exception, keeping original: " + t.getMessage());
			}
		}

		StringBuilder sb = new StringBuilder(message);
		for (int i = deduped.size() - 1; i >= 0; i--) {
			MatchSpan span = deduped.get(i);
			sb.replace(span.start(), span.end(), replacements.get(i));
		}
		return sb.toString();
	}

	private List<MatchSpan> greedyDedup(List<MatchSpan> sorted) {
		List<MatchSpan> result = new ArrayList<>();
		int lastEnd = -1;
		for (MatchSpan s : sorted) {
			if (s.start() >= lastEnd) {
				result.add(s);
				lastEnd = s.end();
			}
		}
		return result;
	}

}
