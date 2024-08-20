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

package com.power4j.fist.oauth2.core;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface AuthConstants {

	/**
	 * 客户端是否允许同时在线
	 */
	String ONLINE_UNLIMITED = "online_unlimited";

	/**
	 * 客户端编号
	 */
	String CLIENT_ID = "client_id";

	/**
	 * 客户端唯一令牌
	 */
	String CLIENT_RECREATE = "recreate_flag";

	/**
	 * 用户ID字段
	 */
	String DETAILS_USER_ID = "user_id";

	/**
	 * 用户名
	 */
	String DETAILS_USERNAME = "username";

	/**
	 * 姓名
	 */
	String NAME = "name";

	/**
	 * 协议字段
	 */
	String DETAILS_LICENSE = "license";

	/**
	 * 激活字段 兼容外围系统接入
	 */
	String ACTIVE = "active";

	/**
	 * 客户端模式
	 */
	String CLIENT_CREDENTIALS = "client_credentials";

	/**
	 * 短信登录 参数名称
	 */
	String SMS_PARAMETER_NAME = "mobile";

	/**
	 * 手机号登录
	 */
	String APP = "mobile";

}
