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

package com.power4j.fist.autoconfigure.oas;

import com.power4j.fist.auth.handler.DefaultFormAuthenticationFailureHandler;
import com.power4j.fist.auth.handler.DefaultSsoLogoutSuccessHandler;
import com.power4j.fist.oauth2.extension.password.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.power4j.fist.oauth2.extension.sms.OAuth2ResourceOwnerSmsAuthenticationConverter;
import com.power4j.fist.oauth2.extension.token.DefaultOAuth2AccessTokenGenerator;
import com.power4j.fist.oauth2.extension.token.DefaultTokenCustomizer;
import com.power4j.fist.oauth2.extension.token.OAuth2RefreshTokenAuthenticationConverter;
import com.power4j.fist.oauth2.extension.token.OauthUserPropExporter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeRequestAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Arrays;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@AutoConfiguration
public class AuthorizationServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OauthUserPropExporter userInfoExporter() {
		return OauthUserPropExporter.NO_OP;
	}

	@Bean
	@ConditionalOnMissingBean
	public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(OauthUserPropExporter userInfoExporter) {
		DefaultOAuth2AccessTokenGenerator accessTokenGenerator = new DefaultOAuth2AccessTokenGenerator();
		accessTokenGenerator.setAccessTokenCustomizer(new DefaultTokenCustomizer(userInfoExporter));
		return new DelegatingOAuth2TokenGenerator(accessTokenGenerator, new OAuth2RefreshTokenGenerator());
	}

	@Bean
	@ConditionalOnMissingBean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new DefaultFormAuthenticationFailureHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return new DefaultSsoLogoutSuccessHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public AuthenticationConverter accessTokenRequestConverter() {
		return new DelegatingAuthenticationConverter(Arrays.asList(
				new OAuth2ResourceOwnerPasswordAuthenticationConverter(),
				new OAuth2ResourceOwnerSmsAuthenticationConverter(), new OAuth2RefreshTokenAuthenticationConverter(),
				new OAuth2ClientCredentialsAuthenticationConverter(),
				new OAuth2AuthorizationCodeAuthenticationConverter(),
				new OAuth2AuthorizationCodeRequestAuthenticationConverter()));
	}

}
