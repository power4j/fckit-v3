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

package com.power4j.fist.support.spring.web.reactive.util;

import com.power4j.fist.support.spring.web.servlet.util.HttpServletRequestUtil;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/16
 * @since 1.0
 */
@UtilityClass
public class ServerHttpRequestUtil {

	/**
	 * 取IP
	 * @param request ServerHttpRequest
	 * @return IP
	 */
	public Optional<String> getRemoteIp(ServerHttpRequest request) {
		Optional<String> ip = HttpServletRequestUtil.getRemoteIp(request.getHeaders());
		if (ip.isEmpty()) {
			ip = Optional.ofNullable(request.getRemoteAddress())
				.map(InetSocketAddress::getAddress)
				.map(InetAddress::getHostAddress);
		}
		return ip;
	}

	/**
	 * 请求行
	 * @param request the request
	 * @return 请求行,比如 {@code GET /users/john}
	 */
	public String simpleRequestLine(ServerHttpRequest request) {
		final HttpMethod method = request.getMethod();
		return (method == null ? "NULL" : method.name()) + " " + request.getURI().getPath();
	}

	/**
	 * 修改请求头
	 * @param origin 原始请求
	 * @param headersConsumer 消费函数
	 * @return 返回新的ServerHttpRequest
	 */
	public ServerHttpRequest updateHeaders(ServerHttpRequest origin, Consumer<HttpHeaders> headersConsumer) {
		// @formatter:off
		return origin
				.mutate()
				.headers(headersConsumer)
				.build();
		// @formatter:on
	}

	/**
	 * 添加或者覆盖HTTP头
	 * @param origin 原始请求
	 * @param key 头名称
	 * @param value 值
	 * @return 返回新的ServerHttpRequest
	 */
	public ServerHttpRequest putHeader(ServerHttpRequest origin, String key, String value) {
		return updateHeaders(origin, h -> h.add(key, value));
	}

	/**
	 * 添加或者覆盖HTTP头
	 * @param origin 原始请求
	 * @param key 头名称
	 * @param values 值
	 * @return 返回新的ServerHttpRequest
	 */
	public ServerHttpRequest putHeader(ServerHttpRequest origin, String key, List<? extends String> values) {
		return updateHeaders(origin, h -> h.addAll(key, values));
	}

}
