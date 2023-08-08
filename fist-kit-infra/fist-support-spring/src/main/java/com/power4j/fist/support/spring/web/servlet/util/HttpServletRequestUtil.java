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

package com.power4j.fist.support.spring.web.servlet.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * HttpServletRequest Util
 * <p>
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020-11-17
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class HttpServletRequestUtil {

	public final static String UNKNOWN = "unknown";

	public final static String X_FORWARD_FOR = "x-forwarded-for";

	public final static String PROXY_CLIENT_IP = "Proxy-Client-IP";

	public final static String WL_Proxy_Client_IP = "WL-Proxy-Client-IP";

	public final static String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";

	public final static String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

	/**
	 * 获取输入流
	 * @param request ServletRequest
	 * @return Optional
	 */
	public Optional<ServletInputStream> getInputStreamIfAvailable(ServletRequest request) {
		try {
			return Optional.of(request.getInputStream());
		}
		catch (IOException e) {
			log.warn(e.getMessage(), e);
			return Optional.empty();
		}
	}

	/**
	 * 获取当前请求
	 * @see RequestContextListener
	 * @return Optional
	 */
	public Optional<HttpServletRequest> getCurrentRequestIfAvailable() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
			.map(x -> (ServletRequestAttributes) x)
			.map(ServletRequestAttributes::getRequest);
	}

	/**
	 * 获取当前请求
	 * @see RequestContextListener
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getCurrentRequest() {
		return getCurrentRequestIfAvailable()
			.orElseThrow(() -> new IllegalStateException("Can not offer HttpServletRequest"));
	}

	/**
	 * 获取IP地址
	 * @see <a href=
	 * "https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For</a>
	 * @param request
	 * @return
	 */
	public static Optional<String> getRemoteIp(HttpServletRequest request) {
		String address = request.getHeader(X_FORWARD_FOR);
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = request.getHeader(PROXY_CLIENT_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = request.getHeader(WL_Proxy_Client_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = request.getHeader(HTTP_CLIENT_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = request.getHeader(HTTP_X_FORWARDED_FOR);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = request.getRemoteAddr();
		}
		return Optional.ofNullable(address).map(s -> StringUtils.split(s, ',')[0]);
	}

	/**
	 * 获取IP地址
	 * @see <a href=
	 * "https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For</a>
	 * @param headers
	 * @return
	 */
	public static Optional<String> getRemoteIp(HttpHeaders headers) {
		String address = headers.getFirst(X_FORWARD_FOR);
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = headers.getFirst(PROXY_CLIENT_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = headers.getFirst(WL_Proxy_Client_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = headers.getFirst(HTTP_CLIENT_IP);
		}
		if (StringUtils.isBlank(address) || UNKNOWN.equalsIgnoreCase(address)) {
			address = headers.getFirst(HTTP_X_FORWARDED_FOR);
		}
		return Optional.ofNullable(address).map(s -> StringUtils.split(s, ',')[0]);
	}

	/**
	 * 获取IP地址
	 * @see #getRemoteIp(HttpServletRequest)
	 * @param request
	 * @param defaultValue
	 * @return
	 */
	@Nullable
	public static String getRemoteIp(HttpServletRequest request, @Nullable String defaultValue) {
		return getRemoteIp(request).orElse(defaultValue);
	}

	/**
	 * 提取http头
	 * @param request
	 * @param names
	 * @return
	 */
	public HttpHeaders pickupHeaders(HttpServletRequest request, Collection<String> names) {
		return pickupHeaders(request, name -> names.contains(name));
	}

	/**
	 * 提取http头
	 * @param request
	 * @param predicate
	 * @return
	 */
	public HttpHeaders pickupHeaders(HttpServletRequest request, Predicate<String> predicate) {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames == null) {
			return headers;
		}
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			if (predicate.test(name)) {
				headers.add(name, request.getHeader(name));
			}
		}
		return headers;
	}

	/**
	 * 获取Header
	 * @param request
	 * @param name
	 * @return
	 */
	public Optional<String> getHeader(HttpServletRequest request, String name) {
		return Optional.ofNullable(request.getHeader(name));
	}

}
