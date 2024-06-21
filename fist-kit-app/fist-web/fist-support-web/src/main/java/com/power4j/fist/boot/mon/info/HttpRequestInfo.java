/*
 * Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.boot.mon.info;

import com.power4j.fist.support.spring.web.servlet.util.HttpServletRequestUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/3/31
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 方法,小写字符
	 */
	private String method;

	private String path;

	@Nullable
	private String queryParam;

	private String from;

	public static HttpRequestInfo from(HttpServletRequest request) {
		final String method = Optional.ofNullable(request.getMethod()).map(String::toLowerCase).orElse("unknown");
		HttpRequestInfo info = new HttpRequestInfo();
		info.setMethod(method);
		info.setPath(request.getRequestURI());
		info.setQueryParam(request.getQueryString());
		info.setFrom(HttpServletRequestUtil.getRemoteIp(request, "unknown"));
		return info;
	}

}
