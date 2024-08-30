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

package com.power4j.fist.boot.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 表示业务处理被终止,此异常及其子类会被全局异常处理器捕获
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/13
 * @since 1.0
 * @see MsgBundleRejectedException
 */
public class RejectedException extends RuntimeException {

	private final String code;

	@Nullable
	private final String hint;

	private HttpStatus status;

	/**
	 * 构造函数，不会进行国际化处理
	 * @param code 错误代码
	 * @param message 错误消息
	 * @param hint 错误提示
	 */
	public RejectedException(String code, String message, @Nullable String hint) {
		this(HttpStatus.OK, code, message, hint);
	}

	/**
	 * 构造函数，不会进行国际化处理
	 * @param status HTTP 响应码
	 * @param code 错误代码
	 * @param message 错误消息
	 * @param hint 错误提示
	 */
	public RejectedException(HttpStatus status, String code, String message, @Nullable String hint) {
		super(message);
		this.code = code;
		this.hint = hint;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	@Nullable
	public String getHint() {
		return hint;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = Objects.requireNonNull(status);
	}

}
