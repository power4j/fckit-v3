package com.power4j.fist.oauth2.extension.utils;

import com.power4j.fist.oauth2.core.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Optional;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author jumuning
 * @since 3.1
 */
@UtilityClass
public class OAuth2EndpointUtils {

	public final String ACCESS_TOKEN_REQUEST_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	public MultiValueMap<String, String> getParameters(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
		parameterMap.forEach((key, values) -> {
			for (String value : values) {
				parameters.add(key, value);
			}
		});
		return parameters;
	}

	public boolean matchesPkceTokenRequest(HttpServletRequest request) {
		return AuthorizationGrantType.AUTHORIZATION_CODE.getValue()
			.equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
				&& request.getParameter(OAuth2ParameterNames.CODE) != null
				&& request.getParameter(PkceParameterNames.CODE_VERIFIER) != null;
	}

	public void throwError(String errorCode, String parameterName, String errorUri) {
		OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
		throw new OAuth2AuthenticationException(error);
	}

	public Optional<String> resolveParameter(HttpServletRequest request, String... names) {
		MultiValueMap<String, String> parameters = getParameters(request);
		for (String name : names) {
			final String value = parameters.getFirst(name);
			if (StringUtils.isNotEmpty(value)) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	public Optional<String> resolveUsername(HttpServletRequest request) {
		return resolveParameter(request, OAuth2ParameterNames.USERNAME, AuthConstants.SSO_PARAMETER_NAME,
				AuthConstants.SMS_PARAMETER_NAME);
	}

}
