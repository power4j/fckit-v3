package com.power4j.fist.oauth2.extension.sms;

import com.power4j.fist.oauth2.core.AuthConstants;
import com.power4j.fist.oauth2.extension.support.OAuth2ResourceOwnerBaseAuthenticationConverter;
import com.power4j.fist.oauth2.extension.utils.OAuth2EndpointUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author lengleng
 * @since 3.1
 */
public class OAuth2ResourceOwnerSmsAuthenticationConverter
		extends OAuth2ResourceOwnerBaseAuthenticationConverter<OAuth2ResourceOwnerSmsAuthenticationToken> {

	/**
	 * 是否支持此convert
	 * @param grantType 授权类型
	 * @return
	 */
	@Override
	public boolean support(String grantType) {
		return AuthConstants.APP.equals(grantType);
	}

	@Override
	public OAuth2ResourceOwnerSmsAuthenticationToken buildToken(Authentication clientPrincipal,
			Set<String> requestedScopes, Map<String, Object> additionalParameters) {
		return new OAuth2ResourceOwnerSmsAuthenticationToken(new AuthorizationGrantType(AuthConstants.APP),
				clientPrincipal, requestedScopes, additionalParameters);
	}

	/**
	 * 校验扩展参数 密码模式密码必须不为空
	 * @param request 参数列表
	 */
	@Override
	public void checkParams(HttpServletRequest request) {
		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);
		// PHONE (REQUIRED)
		String phone = parameters.getFirst(AuthConstants.SMS_PARAMETER_NAME);
		if (!StringUtils.hasText(phone) || parameters.get(AuthConstants.SMS_PARAMETER_NAME).size() != 1) {
			OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, AuthConstants.SMS_PARAMETER_NAME,
					OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}
	}

}
