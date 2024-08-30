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

package com.power4j.fist.oauth2.extension.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
public interface ManagedOAuth2AuthorizationService extends OAuth2AuthorizationService {

	/**
	 * 根据 username 删除
	 * @param registeredClient 客户端信息
	 * @param username 用户名
	 * @return 返回记录影响条数
	 */
	int removeByUsername(RegisteredClient registeredClient, String username);

	/**
	 * 根据 username 删除
	 * @param registeredClient 客户端信息
	 * @param authentication 认证信息
	 * @return 返回记录影响条数
	 */
	default int removeByUsername(RegisteredClient registeredClient, Authentication authentication) {
		return removeByUsername(registeredClient, authentication.getName());
	}

}
