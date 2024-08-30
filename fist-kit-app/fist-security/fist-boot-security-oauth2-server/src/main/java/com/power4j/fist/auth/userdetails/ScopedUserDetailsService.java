/*
 * Copyright 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.auth.userdetails;

import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
public interface ScopedUserDetailsService extends UserDetailsService, Ordered {

	int DEFAULT_ORDER = 0;

	/**
	 * 是否支持此客户端校验
	 * @param clientId 请求客户端
	 * @param grantType 授权类型
	 * @return true/false
	 */
	default boolean supported(String clientId, String grantType) {
		return true;
	}

	@Override
	default int getOrder() {
		return DEFAULT_ORDER;
	}

}
