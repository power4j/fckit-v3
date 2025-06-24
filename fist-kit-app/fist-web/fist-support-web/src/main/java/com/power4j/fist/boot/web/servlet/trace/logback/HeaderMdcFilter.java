/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.boot.web.servlet.trace.logback;

import com.power4j.fist.boot.common.logging.LogConstant;
import com.power4j.fist.boot.web.constant.HttpConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
@RequiredArgsConstructor
public class HeaderMdcFilter extends OncePerRequestFilter {

	private final static String NATIVE_REQ_ID = "REQ-ID";

	private final boolean enableNativeReqId;

	// Key: http header name,Value: MDC key name
	private final Map<String, String> mdcKeyMapper;

	public static HeaderMdcFilter useDefault() {
		Map<String, String> mdcKeyMapper = new HashMap<>(4);
		mdcKeyMapper.put(HttpConstant.Header.KEY_REQUEST_ID, LogConstant.MDC_REQUEST_ID);
		return new HeaderMdcFilter(true, mdcKeyMapper);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Map<String, String> mdcContext = null;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			mdcContext = extractMdc(httpRequest);
		}

		if (mdcContext == null || mdcContext.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}
		mdcContext.forEach(MDC::put);
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			mdcContext.keySet().forEach(MDC::remove);
		}
	}

	protected Map<String, String> extractMdc(HttpServletRequest request) {
		if (mdcKeyMapper == null || mdcKeyMapper.isEmpty()) {
			return Map.of();
		}
		final Map<String, String> mdc = new HashMap<>();
		if (enableNativeReqId) {
			final String reqId = request.getRequestId();
			mdc.put(NATIVE_REQ_ID, reqId == null ? "null" : reqId);
		}
		mdcKeyMapper.forEach((headKey, mdcKey) -> {
			String val = request.getHeader(headKey);
			if (val != null) {
				mdc.put(mdcKey, val);
			}
		});
		return mdc;
	}

}
