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

import java.util.Arrays;
import java.util.Map;

/**
 * 全量打码 {@link Transformer}：将整个 span 替换为掩码字符。
 * <p>
 * 配置参数：{@code maskChar}（默认 *）。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class MaskAllTransformer implements Transformer {

	private char maskChar = '*';

	@Override
	public void init(Map<String, String> props) {
		String mc = props.getOrDefault("maskChar", "*");
		maskChar = mc.isEmpty() ? '*' : mc.charAt(0);
	}

	@Override
	public String transform(String value, LogMessageContext context) {
		char[] chars = new char[value.length()];
		Arrays.fill(chars, maskChar);
		return new String(chars);
	}

}
