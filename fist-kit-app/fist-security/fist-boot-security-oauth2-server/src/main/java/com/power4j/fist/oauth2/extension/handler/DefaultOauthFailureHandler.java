
package com.power4j.fist.oauth2.extension.handler;

import com.power4j.coca.kit.common.lang.Result;
import com.power4j.fist.boot.common.api.Results;
import com.power4j.fist.oauth2.extension.event.OauthFailureEvent;
import com.power4j.fist.oauth2.extension.utils.OAuth2EndpointUtils;
import com.power4j.fist.support.spring.util.SpringEventUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
@Slf4j
public class DefaultOauthFailureHandler implements AuthenticationFailureHandler {

	private static final MappingJackson2HttpMessageConverter ERROR_RESPONSE_CONVERTER = new MappingJackson2HttpMessageConverter();

	/**
	 * Called when an authentication attempt fails.
	 * @param request the request during which the authentication attempt occurred.
	 * @param response the response.
	 * @param exception the exception which was thrown to reject the authentication
	 * request.
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (log.isInfoEnabled()) {
			log.info("OAuth authentication failed: {}", exception.getLocalizedMessage());
		}
		OauthFailureEvent event = OauthFailureEvent.builder()
			.grantType(grantType)
			.username(OAuth2EndpointUtils.resolveUsername(request).orElse(""))
			.exception(exception)
			.request(request)
			.build();
		SpringEventUtil.publishEvent(event);
		try {
			writeErrorResponse(request, response, exception);
		}
		catch (IOException e) {
			throw new IllegalStateException(e.getLocalizedMessage(), e);
		}
	}

	private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
		httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
		String errorMessage;
		if (exception instanceof OAuth2AuthenticationException authorizationException) {
			errorMessage = StringUtils.isBlank(authorizationException.getError().getDescription())
					? authorizationException.getError().getErrorCode()
					: authorizationException.getError().getDescription();

			ERROR_RESPONSE_CONVERTER.write(
					Result.create(authorizationException.getError().getErrorCode(), errorMessage, null),
					MediaType.APPLICATION_JSON, httpResponse);

		}
		else {
			errorMessage = exception.getLocalizedMessage();
			ERROR_RESPONSE_CONVERTER.write(Results.clientError(errorMessage, null), MediaType.APPLICATION_JSON,
					httpResponse);
		}
	}

}
