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

package com.power4j.fist.logback.builtin;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MatchSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于正则表达式的 {@link Detector} 实现。
 * <p>
 * 配置参数：{@code pattern}（必填）。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class RegexDetector implements Detector {

	private Pattern pattern;

	@Override
	public void init(Map<String, String> props) {
		String p = props.get("pattern");
		if (p == null || p.isBlank()) {
			throw new IllegalArgumentException("RegexDetector: 'pattern' is required");
		}
		this.pattern = Pattern.compile(p);
	}

	@Override
	public List<MatchSpan> detect(String message, LogMessageContext context) {
		List<MatchSpan> spans = new ArrayList<>();
		Matcher m = pattern.matcher(message);
		while (m.find()) {
			spans.add(new MatchSpan(m.start(), m.end()));
		}
		return spans;
	}

}
