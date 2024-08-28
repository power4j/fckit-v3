
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

import com.power4j.fist.auth.event.AuthFailureEvent;
import com.power4j.fist.support.spring.util.SpringEventUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.nio.charset.StandardCharsets;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
@Slf4j
public class DefaultFormAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final URLCodec urlCodec = new URLCodec();

	/**
	 * Called when an authentication attempt fails.
	 * @param request the request during which the authentication attempt occurred.
	 * @param response the response.
	 * @param exception the exception which was thrown to reject the authentication
	 */
	@Override
	@SneakyThrows
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		log.debug("Form Authentication Failure:{}", exception.getLocalizedMessage());
		AuthFailureEvent event = AuthFailureEvent.builder()
			.request(request)
			.username(request.getParameter("username"))
			.exception(exception)
			.build();
		SpringEventUtil.publishEvent(event);
		String url = "/token/login?error=" + exception.getMessage();
		response.sendRedirect(urlCodec.encode(url, StandardCharsets.UTF_8.name()));
	}

}
