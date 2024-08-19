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

package com.power4j.fist.security.core.authorization.config;

import com.power4j.fist.boot.common.prop.PropConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/26
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = GlobalAuthorizationProperties.PROP_PREFIX)
public class GlobalAuthorizationProperties {

	public static final String PROP_ENTRY = "authorization.global";

	public static final String PROP_PREFIX = PropConstant.PROP_ROOT_PREFIX + PROP_ENTRY;

	public static final String PROP_FILTERS = PROP_PREFIX + ".filters";

	private boolean enabled = true;

	/**
	 * 安全模式
	 */
	private SafeModeConfig safeMode = new SafeModeConfig();

	/**
	 * 跳过鉴权的入站地址,支持ANT,比如 {@code '/api/**'}
	 *
	 */
	private Set<String> skip = new HashSet<>();

	private Auth auth = new Auth();

	private AccessIpConfig accessIp = new AccessIpConfig();

	@Data
	public static class Auth {

		private String tenantHeader = "x-tenant-id";

		private String tenantParameter = "tenantId";

		private String apiTokenHeader = "x-api-token";

		private String apiTokenParameter = "apiToken";

	}

	@Data
	public static class SafeModeConfig {

		private boolean enabled = false;

		/**
		 * 安全模式允许的IP,比如 {@code '192.168.*'}
		 *
		 */
		private List<String> whitelist = Collections.singletonList("127.*");

	}

	@Data
	public static class AccessIpConfig {

		/** 用户访问IP的最大解析索引,用于防止IP欺骗 */
		private int maxTrustResolves = 64;

		/** 所有用户的IP白名单(CIDR) */
		private List<String> global = Collections.singletonList("0.0.0.0/0");

		/**
		 * 针对特定用户的IP白名单(CIDR)
		 */
		private Map<String, List<String>> rules = Collections.emptyMap();

	}

}
