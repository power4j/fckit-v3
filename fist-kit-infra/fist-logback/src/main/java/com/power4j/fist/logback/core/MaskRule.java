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

import com.power4j.fist.logback.util.MaskingUtil;

import java.util.regex.Pattern;

/**
 * 脱敏规则：正则匹配 + 处理策略。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.11
 */
public final class MaskRule {

	private final String name;

	private final Pattern pattern;

	private final MaskStrategy strategy;

	/** REPLACE 策略的替换字符串，支持 $1/$2 反向引用。 */
	private final String replacement;

	/** MASK_MIDDLE 策略保留的头部长度。 */
	private final int keepHead;

	/** MASK_MIDDLE 策略保留的尾部长度。 */
	private final int keepTail;

	private final char maskChar;

	public MaskRule(String name, Pattern pattern, MaskStrategy strategy, String replacement, int keepHead, int keepTail,
			char maskChar) {
		this.name = name;
		this.pattern = pattern;
		this.strategy = strategy;
		this.replacement = replacement;
		this.keepHead = keepHead;
		this.keepTail = keepTail;
		this.maskChar = maskChar;
	}

	public String getName() {
		return name;
	}

	public String apply(String message) {
		var matcher = pattern.matcher(message);
		return switch (strategy) {
			case REPLACE -> matcher.replaceAll(replacement);
			case MASK_ALL -> matcher.replaceAll(mr -> String.valueOf(maskChar).repeat(mr.group().length()));
			case MASK_MIDDLE ->
				matcher.replaceAll(mr -> MaskingUtil.maskMiddle(mr.group(), keepHead, keepTail, maskChar));
		};
	}

}
