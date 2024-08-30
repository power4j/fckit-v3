/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.security.oauth2;

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 授权类型 see <a href=https://datatracker.ietf.org/doc/html/rfc6749#section-1.3>rfc6749
 * section-1.3</a>
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/24
 * @since 1.0
 */
public enum Oauth2GrantType {

	/**
	 * AUTHORIZATION_CODE
	 */
	AUTHORIZATION_CODE("authorization_code"),
	/**
	 * IMPLICIT
	 */
	IMPLICIT("implicit"),
	/**
	 * REFRESH_TOKEN
	 */
	REFRESH_TOKEN("refresh_token"),
	/**
	 * CLIENT_CREDENTIALS
	 */
	CLIENT_CREDENTIALS("client_credentials"),
	/**
	 * PASSWORD
	 */
	PASSWORD("password");

	private final String value;

	Oauth2GrantType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Set<String> valueSet() {
		return Arrays.stream(Oauth2GrantType.values()).map(Oauth2GrantType::getValue).collect(Collectors.toSet());
	}

	/**
	 * 解析
	 * @param value 被解析的数据,可以是null
	 * @param defValue 默认值
	 * @return 如果解析失败返回默认值
	 */
	@Nullable
	public static Oauth2GrantType parseOrDefault(@Nullable final String value,
			@Nullable final Oauth2GrantType defValue) {
		if (value == null) {
			return defValue;
		}
		for (Oauth2GrantType o : Oauth2GrantType.values()) {
			if (o.getValue().equals(value)) {
				return o;
			}
		}
		return defValue;
	}

	/**
	 * 解析
	 * @param value 被解析的数据
	 * @return 如果解析失败返回 null
	 */
	@Nullable
	public static Oauth2GrantType parseOrNull(final String value) {
		return parseOrDefault(value, null);
	}

	/**
	 * 解析
	 * @param value 被解析的数据
	 * @param thrower 异常抛出器
	 * @return 如果解析失败抛出异常
	 */
	public static Oauth2GrantType parseOrThrow(final String value, Function<String, RuntimeException> thrower) {
		Oauth2GrantType o = parseOrDefault(value, null);
		if (o == null) {
			throw thrower.apply(value);
		}
		return o;
	}

	/**
	 * 解析
	 * @param value 被解析的数据
	 * @return 如果解析失败抛出 IllegalArgumentException
	 */
	public static Oauth2GrantType parse(final String value) throws IllegalArgumentException {
		return parseOrThrow(value, (v) -> new IllegalArgumentException("Invalid value : " + v));
	}

}
