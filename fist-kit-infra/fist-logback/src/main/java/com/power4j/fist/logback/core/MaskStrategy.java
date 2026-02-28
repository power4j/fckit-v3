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

/**
 * 脱敏策略。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.11
 */
public enum MaskStrategy {

	/** 整体替换为固定字符串，支持正则反向引用（$1/$2）。 */
	REPLACE,

	/** 将匹配内容全部替换为掩码字符。 */
	MASK_ALL,

	/** 保留首尾各 N 位，中间替换为掩码字符。 */
	MASK_MIDDLE

}
