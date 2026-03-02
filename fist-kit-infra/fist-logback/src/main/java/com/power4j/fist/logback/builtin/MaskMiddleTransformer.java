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
import com.power4j.fist.logback.util.MaskingUtil;

import java.util.Map;

/**
 * 中间打码 {@link Transformer}：保留头部 keepFirst 位和尾部 keepLast 位，中间替换为掩码字符。
 * <p>
 * 配置参数：{@code keepFirst}（默认 0）、{@code keepLast}（默认 0）、{@code maskChar}（默认 *）。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public class MaskMiddleTransformer implements Transformer {

	private int keepFirst = 0;

	private int keepLast = 0;

	private char maskChar = '*';

	@Override
	public void init(Map<String, String> props) {
		keepFirst = Integer.parseInt(props.getOrDefault("keepFirst", "0"));
		keepLast = Integer.parseInt(props.getOrDefault("keepLast", "0"));
		String mc = props.getOrDefault("maskChar", "*");
		maskChar = mc.isEmpty() ? '*' : mc.charAt(0);
	}

	@Override
	public String transform(String value, LogMessageContext context) {
		return MaskingUtil.maskMiddle(value, keepFirst, keepLast, maskChar);
	}

}
