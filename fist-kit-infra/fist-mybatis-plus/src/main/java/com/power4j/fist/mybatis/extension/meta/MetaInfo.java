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

package com.power4j.fist.mybatis.extension.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
@Getter
@AllArgsConstructor
public class MetaInfo {

	public final static String DEFAULT_SEPARATOR = ":";

	private final String mainType;

	private final String subType;

	private final String value;

	public static MetaInfo of(String mainType, String subType, String value) {
		if (emptyStr(mainType) || emptyStr(subType) || emptyStr(value)) {
			throw new IllegalArgumentException("mainType, subType and value must not be null or empty");
		}
		return new MetaInfo(mainType, subType, value);
	}

	public static Optional<MetaInfo> decode(@Nullable String raw, String separator) {
		if (emptyStr(raw)) {
			return Optional.empty();
		}
		String[] parts = raw.split(separator);
		if (parts.length != 3) {
			return Optional.empty();
		}
		if (emptyStr(parts[0]) || emptyStr(parts[1]) || emptyStr(parts[2])) {
			return Optional.empty();
		}
		return Optional.of(new MetaInfo(parts[0], parts[1], parts[2]));
	}

	public static Optional<MetaInfo> decode(@Nullable String raw) {
		return decode(raw, DEFAULT_SEPARATOR);
	}

	public String encode(String separator) {
		return String.join(separator, mainType, subType, value);
	}

	public String encode() {
		return encode(DEFAULT_SEPARATOR);
	}

	static boolean emptyStr(String value) {
		return value == null || value.isEmpty();
	}

}
