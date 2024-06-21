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

package com.power4j.fist.boot.security.core;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/7/2
 * @since 1.0
 */
public interface SecurityConstant {

	String HEADER_JWT_INNER = "X-JWT-IN";

	String HEADER_USER_TOKEN_INNER = "X-USER-IN";

	interface Auth2 {

		String INFO_KEY_USER_INFO = "userInfo";

	}

	/**
	 * 匿名用户的用户名
	 */
	String ANONYMOUS_USER = "AnonymousUser";

	/**
	 * 零号租户的ID
	 */
	String TENANT_ZERO = "f06c2084821b4daf96abd68ce88e0ec9";

	/**
	 * 租户ID
	 */
	String PARAMETER_KEY_TENANT_ID = "tenantId";

	/**
	 * 能够被自动识别为租户ID的参数名称,按顺序级解析
	 */
	String[] TENANT_ID_PARAMETER_KEYS = { PARAMETER_KEY_TENANT_ID };

	interface Res {

		String LEVEL_SITE = "0";

		String LEVEL_TENANT = "1";

	}

	interface Crypto {

		String NO_OP_PREFIX = "{noop}";

	}

	interface UserProp {

		String KEY_USER_SOURCE = "userSource";

		String KEY_USERNAME = "username";

		String KEY_USER_ID = "userId";

		String KEY_NICK_NAME = "nickName";

		String KEY_AVATAR_URL = "avatarUrl";

		String KEY_TENANT_ID = "tenantId";

		String KEY_CLIENT_ID = "clientId";

		String KEY_DEPT = "dept";

		String KEY_ROLE_LIST = "roles";

		String KEY_PERMISSION_LIST = "permissions";

		String VAL_DEFAULT_USER_SOURCE = "local";

	}

}
