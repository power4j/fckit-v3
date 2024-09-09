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

package com.power4j.fist.boot.security.inner;

import com.power4j.coca.kit.common.exception.WrappedException;
import com.power4j.fist.boot.security.context.UserContextHolder;
import com.power4j.fist.boot.security.core.SecurityConstant;
import com.power4j.fist.boot.security.core.UserInfo;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/20
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class TrustedUserFilter extends OncePerRequestFilter {

	private final static String USER_ATTR_KEY = "fiamc.trusted-user-info-value";

	private final UserDecoder userDecoder;

	@Setter
	private boolean strictMode = true;

	private final Collection<IPAddress> whitelist = new ArrayList<>(4);

	public void setWhitelist(Collection<String> list) {
		whitelist.clear();
		if (ObjectUtils.isNotEmpty(list)) {
			for (String p : list) {
				try {
					IPAddressString ip = new IPAddressString(p);
					ip.validate();
					whitelist.add(ip.getAddress());
				}
				catch (AddressStringException e) {
					String msg = "白名单配置错误,非法IP地址:" + p;
					throw new IllegalArgumentException(msg, e);
				}
			}
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String userValue = request.getHeader(SecurityConstant.HEADER_USER_TOKEN_INNER);
		UserContextHolder.setOriginalValue(userValue);
		request.setAttribute(USER_ATTR_KEY, userValue);
		UserInfo info = null;
		if (StringUtils.isNotEmpty(userValue) && isTrusted(request)) {
			try {
				info = userDecoder.decode(userValue);
			}
			catch (UserCodecException e) {
				log.warn(e.getMessage(), e);
			}
		}
		try {
			SecurityUtil.act(info, () -> filterChain.doFilter(request, response));
		}
		catch (WrappedException e) {
			if (e.getCause() instanceof ServletException) {
				throw e.getOriginal(ServletException.class);
			}
			else if (e.getCause() instanceof IOException) {
				throw e.getOriginal(IOException.class);
			}
		}
	}

	private boolean isTrusted(HttpServletRequest request) {
		if (strictMode) {
			final String ip = request.getRemoteAddr();
			final IPAddress reqAddr = new IPAddressString(ip).getAddress();
			if (ObjectUtils.isNotEmpty(whitelist)) {
				Optional<IPAddress> matched = whitelist.stream().filter(addr -> addr.contains(reqAddr)).findFirst();
				if (matched.isPresent()) {
					if (log.isTraceEnabled()) {
						log.trace("认证信息可信(白名单: {}),来源:{}", matched.get(), ip);
					}
				}
				else {
					log.warn("认证信息不可信(白名单),来源:{}", ip);
				}
				return matched.isPresent();
			}
			else {
				if (reqAddr.isAnyLocal() || reqAddr.isLoopback()) {
					return true;
				}
				log.warn("认证信息不可信,来源:{}", ip);
				return false;
			}
		}
		return true;
	}

}
