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

package com.power4j.fist.logback.util;

import lombok.experimental.UtilityClass;

/**
 * 常见字符串打码工具。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
@UtilityClass
public class MaskingUtil {

	private static final char DEFAULT_MASK_CHAR = '*';

	/**
	 * 首位打码（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param maskLength 从头部开始打码的长度；当值大于输入长度时，按输入长度全量打码
	 * @return 打码后的文本
	 */
	public String maskHead(String input, int maskLength) {
		return maskHead(input, maskLength, DEFAULT_MASK_CHAR);
	}

	/**
	 * 首位打码（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param maskLength 从头部开始打码的长度；当值大于输入长度时，按输入长度全量打码
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskHead(String input, int maskLength, char maskChar) {
		if (input == null || input.isEmpty() || maskLength == 0) {
			return input;
		}
		if (maskLength < 0) {
			throw new IllegalArgumentException("maskLength must be >= 0");
		}
		int actualMaskLength = Math.min(maskLength, input.length());
		return repeat(maskChar, actualMaskLength) + input.substring(actualMaskLength);
	}

	/**
	 * 尾部打码（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param maskLength 从尾部开始打码的长度；当值大于输入长度时，按输入长度全量打码
	 * @return 打码后的文本
	 */
	public String maskTail(String input, int maskLength) {
		return maskTail(input, maskLength, DEFAULT_MASK_CHAR);
	}

	/**
	 * 尾部打码（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param maskLength 从尾部开始打码的长度；当值大于输入长度时，按输入长度全量打码
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskTail(String input, int maskLength, char maskChar) {
		if (input == null || input.isEmpty() || maskLength == 0) {
			return input;
		}
		if (maskLength < 0) {
			throw new IllegalArgumentException("maskLength must be >= 0");
		}
		int actualMaskLength = Math.min(maskLength, input.length());
		int keepLength = input.length() - actualMaskLength;
		return input.substring(0, keepLength) + repeat(maskChar, actualMaskLength);
	}

	/**
	 * 头部打码，仅显示最多后 N 位（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param visibleTail 允许保留的尾部明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @return 打码后的文本
	 */
	public String maskHeadKeepLast(String input, int visibleTail) {
		return maskHeadKeepLast(input, visibleTail, DEFAULT_MASK_CHAR);
	}

	/**
	 * 头部打码，仅显示最多后 N 位（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param visibleTail 允许保留的尾部明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskHeadKeepLast(String input, int visibleTail, char maskChar) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		if (visibleTail < 0) {
			throw new IllegalArgumentException("visibleTail must be >= 0");
		}
		if (visibleTail >= input.length()) {
			return input;
		}
		return maskHead(input, input.length() - visibleTail, maskChar);
	}

	/**
	 * 尾部打码，仅显示最多前 N 位（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param visibleHead 允许保留的头部明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @return 打码后的文本
	 */
	public String maskTailKeepFirst(String input, int visibleHead) {
		return maskTailKeepFirst(input, visibleHead, DEFAULT_MASK_CHAR);
	}

	/**
	 * 尾部打码，仅显示最多前 N 位（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param visibleHead 允许保留的头部明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskTailKeepFirst(String input, int visibleHead, char maskChar) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		if (visibleHead < 0) {
			throw new IllegalArgumentException("visibleHead must be >= 0");
		}
		if (visibleHead >= input.length()) {
			return input;
		}
		return maskTail(input, input.length() - visibleHead, maskChar);
	}

	/**
	 * 中间打码，仅显示最多中间 N 位（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param visibleMiddle 允许保留的中间明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @return 打码后的文本
	 */
	public String maskMiddleKeepCenter(String input, int visibleMiddle) {
		return maskMiddleKeepCenter(input, visibleMiddle, DEFAULT_MASK_CHAR);
	}

	/**
	 * 中间打码，仅显示最多中间 N 位（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param visibleMiddle 允许保留的中间明文长度；当值大于等于输入长度时返回原文，值为 0 时全量打码
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskMiddleKeepCenter(String input, int visibleMiddle, char maskChar) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		if (visibleMiddle < 0) {
			throw new IllegalArgumentException("visibleMiddle must be >= 0");
		}
		if (visibleMiddle >= input.length()) {
			return input;
		}
		int start = (input.length() - visibleMiddle) / 2;
		int end = start + visibleMiddle;
		return repeat(maskChar, start) + input.substring(start, end) + repeat(maskChar, input.length() - end);
	}

	/**
	 * 中间打码（默认掩码字符为 *）。
	 * @param input 输入文本
	 * @param keepHead 保留头部明文长度；当输入长度小于保留长度总和时，不做打码并返回原文
	 * @param keepTail 保留尾部明文长度；当输入长度小于保留长度总和时，不做打码并返回原文
	 * @return 打码后的文本
	 */
	public String maskMiddle(String input, int keepHead, int keepTail) {
		return maskMiddle(input, keepHead, keepTail, DEFAULT_MASK_CHAR);
	}

	/**
	 * 中间打码（支持自定义掩码字符）。
	 * @param input 输入文本
	 * @param keepHead 保留头部明文长度；当输入长度小于保留长度总和时，不做打码并返回原文
	 * @param keepTail 保留尾部明文长度；当输入长度小于保留长度总和时，不做打码并返回原文
	 * @param maskChar 掩码字符
	 * @return 打码后的文本
	 */
	public String maskMiddle(String input, int keepHead, int keepTail, char maskChar) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		if (keepHead < 0 || keepTail < 0) {
			throw new IllegalArgumentException("keepHead and keepTail must be >= 0");
		}
		if (keepHead + keepTail >= input.length()) {
			return input;
		}
		int middleLength = input.length() - keepHead - keepTail;
		String head = input.substring(0, keepHead);
		String tail = input.substring(input.length() - keepTail);
		return head + repeat(maskChar, middleLength) + tail;
	}

	private String repeat(char ch, int count) {
		if (count <= 0) {
			return "";
		}
		char[] chars = new char[count];
		for (int i = 0; i < count; i++) {
			chars[i] = ch;
		}
		return new String(chars);
	}

}
