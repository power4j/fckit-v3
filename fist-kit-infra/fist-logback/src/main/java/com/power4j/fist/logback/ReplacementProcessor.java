/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.logback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface ReplacementProcessor {

	/**
	 * 匹配文本的Pattern。
	 * @return Pattern
	 */
	Pattern getPattern();

	/**
	 * 对原始文本中匹配到的特定部分进行替换处理。
	 * @param originalMatch 原始文本中匹配到的字符串片段。
	 * @return 替换后的字符串片段。
	 */
	String replaceMatch(String originalMatch);

	/**
	 * 替换处理
	 * @param logMessage 原始的日志消息。
	 * @return 经过处理（替换）后的日志消息。
	 */
	default String process(String logMessage) {
		if (logMessage == null || logMessage.isEmpty()) {
			return logMessage;
		}
		Pattern pattern = getPattern();
		Matcher matcher = pattern.matcher(logMessage);
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		while (matcher.find()) {
			found = true;
			String originalMatch = matcher.group();
			String replacedText = replaceMatch(originalMatch);
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacedText));
		}
		if (found) {
			matcher.appendTail(sb);
			return sb.toString();
		}
		return logMessage;
	}

}
