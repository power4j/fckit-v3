/*
 * Copyright 2026. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.oauth2.extension.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Map;
import java.util.Set;

/**
 * @author Codex
 * @since 3.13
 */
class OAuth2ResourceOwnerBaseAuthenticationProviderTest {

	@Test
	void shouldPreserveBadCredentialsMessageWhenWrappedAsOAuth2Error() {
		AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
		OAuth2AuthorizationService authorizationService = Mockito.mock(OAuth2AuthorizationService.class);
		OAuth2TokenGenerator<?> tokenGenerator = Mockito.mock(OAuth2TokenGenerator.class);
		RegisteredClient registeredClient = Mockito.mock(RegisteredClient.class);
		OAuth2ClientAuthenticationToken clientPrincipal = Mockito.mock(OAuth2ClientAuthenticationToken.class);

		Mockito.when(clientPrincipal.isAuthenticated()).thenReturn(true);
		Mockito.when(clientPrincipal.getRegisteredClient()).thenReturn(registeredClient);
		Mockito.when(authenticationManager.authenticate(Mockito.any()))
			.thenThrow(new BadCredentialsException("认证模式已禁用"));

		OAuth2ResourceOwnerPasswordAuthenticationProviderStub provider = new OAuth2ResourceOwnerPasswordAuthenticationProviderStub(
				authenticationManager, authorizationService, tokenGenerator);
		OAuth2ResourceOwnerPasswordAuthenticationTokenStub authentication = new OAuth2ResourceOwnerPasswordAuthenticationTokenStub(
				AuthorizationGrantType.PASSWORD, clientPrincipal, Map.of("username", "alice", "password", "pwd"));

		OAuth2AuthenticationException ex = Assertions.assertThrows(OAuth2AuthenticationException.class,
				() -> provider.authenticate(authentication));

		Assertions.assertEquals("认证模式已禁用", ex.getError().getDescription());
	}

	private static class OAuth2ResourceOwnerPasswordAuthenticationProviderStub
			extends OAuth2ResourceOwnerBaseAuthenticationProvider<OAuth2ResourceOwnerPasswordAuthenticationTokenStub> {

		OAuth2ResourceOwnerPasswordAuthenticationProviderStub(AuthenticationManager authenticationManager,
				OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<?> tokenGenerator) {
			super(authenticationManager, authorizationService, tokenGenerator);
		}

		@Override
		public UsernamePasswordAuthenticationToken buildToken(Map<String, Object> reqParameters) {
			return new UsernamePasswordAuthenticationToken(reqParameters.get("username"),
					reqParameters.get("password"));
		}

		@Override
		public boolean supports(Class<?> authentication) {
			return OAuth2ResourceOwnerPasswordAuthenticationTokenStub.class.isAssignableFrom(authentication);
		}

		@Override
		public void checkClient(RegisteredClient registeredClient) {
			// 测试中不关注客户端校验逻辑
		}

	}

	private static class OAuth2ResourceOwnerPasswordAuthenticationTokenStub
			extends OAuth2ResourceOwnerBaseAuthenticationToken {

		OAuth2ResourceOwnerPasswordAuthenticationTokenStub(AuthorizationGrantType authorizationGrantType,
				Authentication clientPrincipal, Map<String, Object> additionalParameters) {
			super(authorizationGrantType, clientPrincipal, Set.of(), additionalParameters);
		}

	}

}
