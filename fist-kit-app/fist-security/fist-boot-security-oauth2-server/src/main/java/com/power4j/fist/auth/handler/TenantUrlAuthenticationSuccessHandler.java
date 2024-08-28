
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

package com.power4j.fist.auth.handler;

import com.power4j.fist.auth.event.AuthSuccessEvent;
import com.power4j.fist.data.tenant.TenantConstant;
import com.power4j.fist.data.tenant.TenantUtil;
import com.power4j.fist.support.spring.util.SpringEventUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author jumuning
 * @since 3.1
 */
@Slf4j
public class TenantUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final RequestCache requestCache = new HttpSessionRequestCache();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		SavedRequest savedRequest = this.requestCache.getRequest(request, response);
		if (savedRequest == null) {
			super.onAuthenticationSuccess(request, response, authentication);
		}
		AuthSuccessEvent event = AuthSuccessEvent.builder()
			.request(request)
			.username(request.getParameter("username"))
			.authentication(authentication)
			.build();
		SpringEventUtil.publishEvent(event);

		if (isAlwaysUseDefaultTargetUrl()) {
			this.requestCache.removeRequest(request, response);
			super.onAuthenticationSuccess(request, response, authentication);
		}
		else {
			this.clearAuthenticationAttributes(request);
			assert savedRequest != null;
			String targetUrl = savedRequest.getRedirectUrl() + "&" + TenantConstant.TENANT_ID_PARAMETER + "="
					+ TenantUtil.resolveTenantId(request).orElse("null");

			this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
		}
	}

}
