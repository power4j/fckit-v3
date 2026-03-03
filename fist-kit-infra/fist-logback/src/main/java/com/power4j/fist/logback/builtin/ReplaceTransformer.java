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

import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.Transformer;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 正则替换 {@link Transformer}：对 span 内文本执行正则替换。
 * <p>
 * 配置参数：{@code pattern}（必填）、{@code replacement}（必填）。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class ReplaceTransformer implements Transformer {

	private Pattern pattern;

	private String replacement;

	@Override
	public void init(Map<String, String> props) {
		String p = props.get("pattern");
		replacement = props.get("replacement");
		if (p == null || p.isBlank()) {
			throw new IllegalArgumentException("ReplaceTransformer: 'pattern' is required");
		}
		if (replacement == null) {
			throw new IllegalArgumentException("ReplaceTransformer: 'replacement' is required");
		}
		this.pattern = Pattern.compile(p);
	}

	@Override
	public String transform(String value, LogMessageContext context) {
		return pattern.matcher(value).replaceAll(replacement);
	}

}
