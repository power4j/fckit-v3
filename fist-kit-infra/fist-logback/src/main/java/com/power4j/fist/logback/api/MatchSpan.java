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

package com.power4j.fist.logback.api;

/**
 * 检测结果区间，语义为 {@code [start, end)}，索引单位为 UTF-16 char，与 {@link String#substring(int, int)}
 * 及 {@code Matcher.start()}/{@code Matcher.end()} 一致。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public record MatchSpan(int start, int end) {

	/**
	 * 从源字符串中提取本区间对应的文本。调用方须保证区间合法，否则抛出 {@link StringIndexOutOfBoundsException}。
	 * @param source 源字符串
	 * @return 区间内文本
	 */
	public String extract(String source) {
		return source.substring(start, end);
	}

}
