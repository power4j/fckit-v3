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

package com.power4j.fist.cloud.gateway.authorization.domain;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.StringJoiner;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/23
 * @since 1.0
 */
@Getter
public class AuthProblem {

	public final static int CODE_AUTH_PASS = 0;

	private final int code;

	private final String msg;

	@Nullable
	private String moreInfo;

	@Nullable
	private Advise advise;

	public AuthProblem(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public AuthProblem moreInfo(String info) {
		moreInfo = info;
		return this;
	}

	public AuthProblem advise(Advise advise) {
		this.advise = advise;
		return this;
	}

	public boolean codeEquals(int value) {
		return code == value;
	}

	public boolean isAuthPass() {
		return CODE_AUTH_PASS == code;
	}

	public String passStr() {
		return isAuthPass() ? "Pass" : "Denied";
	}

	public String description() {
		return String.format("%s(%d),details: %s", msg, code, moreInfo);
	}

	/**
	 * 公共接口放行
	 */
	public final static AuthProblem PUB_ACCESS = new AuthProblem(CODE_AUTH_PASS, "PUB_ACCESS");

	/**
	 * 认证用户放行
	 */
	public final static AuthProblem LOGIN_ACCESS = new AuthProblem(CODE_AUTH_PASS, "LOGIN_ACCESS");

	/**
	 * 需跳过鉴权
	 */
	public final static AuthProblem SKIP_AUTH = new AuthProblem(CODE_AUTH_PASS, "SKIP_AUTH");

	/**
	 * 安全模式放行
	 */
	public final static AuthProblem SAFE_MODE_PASS = new AuthProblem(CODE_AUTH_PASS, "SAFE_MODE_PASS");

	/**
	 * 有API访问权限
	 */
	public final static AuthProblem PERMISSION_CHECK_PASS = new AuthProblem(CODE_AUTH_PASS, "PERMISSION_CHECK_PASS");

	/**
	 * 鉴权正常结束
	 */
	public final static AuthProblem AUTH_END_PASS = new AuthProblem(CODE_AUTH_PASS, "AUTH_END_PASS");

	/**
	 * 鉴权正常结束
	 */
	public final static AuthProblem AUTH_END_DENIED = new AuthProblem(1, "AUTH_END_DENIED");

	/**
	 * HTTP 协议不支持
	 */
	public final static AuthProblem HTTP_PROTOCOL = new AuthProblem(10, "HTTP_PROTOCOL");

	/**
	 * 传入token无效
	 */
	public final static AuthProblem TOKEN_INVALID = new AuthProblem(20, "TOKEN_INVALID");

	/**
	 * 用户不存在
	 */
	public final static AuthProblem USER_NOT_EXISTS = new AuthProblem(30, "USER_NOT_EXISTS");

	/**
	 * 内部接口拒绝访问
	 */
	public final static AuthProblem INTERNAL_ACCESS_DENIED = new AuthProblem(40, "INTERNAL_ACCESS_DENIED");

	/**
	 * 未登录不能访问
	 */
	public final static AuthProblem USER_ACCESS_DENIED = new AuthProblem(50, "USER_ACCESS_DENIED");

	/**
	 * 用户IP不在白名单
	 */
	public final static AuthProblem USER_IP_DENIED = new AuthProblem(51, "USER_IP_DENIED");

	/**
	 * 无API访问权限
	 */
	public final static AuthProblem PERMISSION_CHECK_DENIED = new AuthProblem(60, "PERMISSION_CHECK_DENIED");

	/**
	 * 租户级API必须传入租户ID
	 */
	public final static AuthProblem TENANT_ID_REQUIRED = new AuthProblem(70, "TENANT_ID_REQUIRED");

	/**
	 * 无租户访问权限
	 */
	public final static AuthProblem TENANT_CHECK_DENIED = new AuthProblem(80, "TENANT_CHECK_DENIED");

	/**
	 * 调用外部服务异常
	 */
	public final static AuthProblem RPC_EXCEPTION = new AuthProblem(100, "RPC_EXCEPTION");

	/**
	 * 安全模式放行
	 */
	public final static AuthProblem SAFE_MODE_DENIED = new AuthProblem(999, "SAFE_MODE_DENIED");

	/**
	 * 认证插件异常
	 */
	public final static AuthProblem AUTH_EXCEPTION = new AuthProblem(1001, "AUTH_EXCEPTION");

	@Override
	public String toString() {
		return new StringJoiner(", ", AuthProblem.class.getSimpleName() + "[", "]").add("code=" + code)
			.add("advise=" + advise)
			.add("msg='" + msg + "'")
			.toString();
	}

	public enum Advise {

		/** need authentication */
		AUTH,
		/** need permission */
		PERM

	}

}
