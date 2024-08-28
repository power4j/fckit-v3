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

package com.power4j.fist.oauth2.extension.filter;

import com.power4j.fist.boot.web.servlet.CachedRequestWrapper;
import com.power4j.fist.oauth2.core.AuthConstants;
import com.power4j.fist.oauth2.extension.utils.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public abstract class AbstractSecureParameterDecoderFilter extends OncePerRequestFilter {

	protected static final String GRANT_TYPE = "grant_type";

	protected static final String REFRESH_TOKEN = "refresh_token";

	private final RegisteredClientRepository registeredClientRepository;

	public AbstractSecureParameterDecoderFilter(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		Set<String> secureParams = findSecureParam(request);
		if (ObjectUtils.isEmpty(secureParams)) {
			chain.doFilter(request, response);
			return;
		}

		String grantType = request.getParameter(GRANT_TYPE);
		if (StringUtils.equals(REFRESH_TOKEN, grantType)) {
			chain.doFilter(request, response);
			return;
		}

		String encFlag = resolveEncFlag(request).orElse(null);
		if (encFlag == null) {
			chain.doFilter(request, response);
			return;
		}

		CachedRequestWrapper requestWrapper = new CachedRequestWrapper(request);
		Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
		decodeParams(encFlag, secureParams, parameterMap);
		chain.doFilter(requestWrapper, response);
	}

	/**
	 * Find secure param in request
	 * @param request the request
	 * @return true if this filter should be skipped
	 */
	protected abstract Set<String> findSecureParam(HttpServletRequest request);

	/**
	 * Decode parameters
	 * @param cryptFlag crypt flag
	 * @param keys the parameter keys need to be decoded
	 * @param parameterMap parameter map,see {@link HttpServletRequest#getParameterMap()}
	 */
	protected abstract void decodeParams(String cryptFlag, Set<String> keys, Map<String, String[]> parameterMap);

	protected Optional<String> resolveEncFlag(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		String clientId = WebUtil.parseBasicClientId(header).orElse(null);
		if (StringUtils.isEmpty(clientId)) {
			return Optional.empty();
		}
		return retrieveEncryptFlag(clientId);
	}

	protected Optional<String> retrieveEncryptFlag(String clientId) {
		RegisteredClient client = registeredClientRepository.findByClientId(clientId);
		if (client == null) {
			return Optional.empty();
		}
		return Optional
			.ofNullable(client.getClientSettings().getSetting(AuthConstants.ClientOpts.KEY_CREDENTIAL_ENC_ALGO));
	}

}
