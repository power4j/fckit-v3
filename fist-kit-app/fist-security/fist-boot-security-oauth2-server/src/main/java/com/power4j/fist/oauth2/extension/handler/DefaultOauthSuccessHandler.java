
package com.power4j.fist.oauth2.extension.handler;

import com.power4j.fist.oauth2.extension.event.OauthSuccessEvent;
import com.power4j.fist.oauth2.extension.utils.OAuth2EndpointUtils;
import com.power4j.fist.support.spring.util.SpringEventUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimNames;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
@Slf4j
public class DefaultOauthSuccessHandler implements AuthenticationSuccessHandler {

	private static final HttpMessageConverter<OAuth2AccessTokenResponse> ACCESS_TOKEN_CONVERTER = new OAuth2AccessTokenResponseHttpMessageConverter();

	/**
	 * Called when a user has been successfully authenticated.
	 * @param request the request which caused the successful authentication
	 * @param response the response
	 * @param authentication the <tt>Authentication</tt> object which was created during
	 * the authentication process.
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		if (log.isDebugEnabled()) {
			log.debug("OAuth2 Authentication Success: {}", authentication.getName());
		}
		OAuth2AccessTokenAuthenticationToken accessTokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		String username = OAuth2EndpointUtils.resolveUsername(request).orElse(null);
		if (StringUtils.isEmpty(username)) {
			username = Objects
				.toString(accessTokenAuthentication.getAdditionalParameters().get(OAuth2TokenClaimNames.SUB));
		}
		OauthSuccessEvent event = OauthSuccessEvent.builder()
			.request(request)
			.grantType(grantType)
			.authentication(accessTokenAuthentication)
			.username(username)
			.build();
		SpringEventUtil.publishEvent(event);
		try {
			writeAccessTokenResponse(response, authentication);
		}
		catch (IOException e) {
			throw new IllegalStateException(e.getLocalizedMessage(), e);
		}
	}

	private void writeAccessTokenResponse(HttpServletResponse response, Authentication authentication)
			throws IOException {

		OAuth2AccessTokenAuthenticationToken accessTokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;

		OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
		OAuth2RefreshToken refreshToken = accessTokenAuthentication.getRefreshToken();
		Map<String, Object> additionalParameters = accessTokenAuthentication.getAdditionalParameters();

		OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
			.tokenType(accessToken.getTokenType())
			.scopes(accessToken.getScopes());
		if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
			Duration duration = Duration.between(accessToken.getIssuedAt(), accessToken.getExpiresAt());
			builder.expiresIn(duration.getSeconds());
		}
		if (refreshToken != null) {
			builder.refreshToken(refreshToken.getTokenValue());
		}
		if (!CollectionUtils.isEmpty(additionalParameters)) {
			builder.additionalParameters(additionalParameters);
		}
		OAuth2AccessTokenResponse accessTokenResponse = builder.build();
		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);

		SecurityContextHolder.clearContext();
		ACCESS_TOKEN_CONVERTER.write(accessTokenResponse, null, httpResponse);
	}

}
