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

package com.power4j.fist.oauth2.extension.support;

import com.power4j.fist.oauth2.core.AuthConstants;
import com.power4j.fist.oauth2.core.OAuth2ErrorCodesExpand;
import com.power4j.fist.oauth2.extension.exceptions.ScopeException;
import com.power4j.fist.oauth2.extension.service.ManagedOAuth2AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author jumuning
 * @since 3.1
 */
@Slf4j
public abstract class OAuth2ResourceOwnerBaseAuthenticationProvider<T extends OAuth2ResourceOwnerBaseAuthenticationToken>
		implements AuthenticationProvider {

	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

	private final OAuth2AuthorizationService authorizationService;

	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	private final AuthenticationManager authenticationManager;

	@Deprecated
	private Supplier<String> refreshTokenGenerator;

	/**
	 * Constructs an {@code OAuth2AuthorizationCodeAuthenticationProvider} using the
	 * provided parameters.
	 * @param authorizationService the authorization service
	 * @param tokenGenerator the token generator
	 * @since 0.2.3
	 */
	public OAuth2ResourceOwnerBaseAuthenticationProvider(AuthenticationManager authenticationManager,
			OAuth2AuthorizationService authorizationService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Deprecated
	public void setRefreshTokenGenerator(Supplier<String> refreshTokenGenerator) {
		Assert.notNull(refreshTokenGenerator, "refreshTokenGenerator cannot be null");
		this.refreshTokenGenerator = refreshTokenGenerator;
	}

	public abstract UsernamePasswordAuthenticationToken buildToken(Map<String, Object> reqParameters);

	/**
	 * 当前provider是否支持此令牌类型
	 * @param authentication
	 * @return
	 */
	@Override
	public abstract boolean supports(Class<?> authentication);

	/**
	 * 当前的请求客户端是否支持此模式
	 * @param registeredClient
	 */
	public abstract void checkClient(RegisteredClient registeredClient);

	/**
	 * Performs authentication with the same contract as
	 * {@link AuthenticationManager#authenticate(Authentication)} .
	 * @param authentication the authentication request object.
	 * @return a fully authenticated object including credentials. May return
	 * <code>null</code> if the <code>AuthenticationProvider</code> is unable to support
	 * authentication of the passed <code>Authentication</code> object. In such a case,
	 * the next <code>AuthenticationProvider</code> that supports the presented
	 * <code>Authentication</code> class will be tried.
	 * @throws AuthenticationException if authentication fails.
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		T resourceOwnerBaseAuthentication = (T) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(
				resourceOwnerBaseAuthentication);

		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
		checkClient(registeredClient);

		Set<String> scopeSet;
		// Default to configured scopes
		if (!CollectionUtils.isEmpty(resourceOwnerBaseAuthentication.getScopes())) {
			for (String requestedScope : resourceOwnerBaseAuthentication.getScopes()) {
				if (!registeredClient.getScopes().contains(requestedScope)) {
					throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
				}
			}
			scopeSet = new LinkedHashSet<>(resourceOwnerBaseAuthentication.getScopes());
		}
		else {
			scopeSet = new LinkedHashSet<>();
		}

		Map<String, Object> reqParameters = resourceOwnerBaseAuthentication.getAdditionalParameters();
		try {

			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = buildToken(reqParameters);

			log.debug("got usernamePasswordAuthenticationToken: {}", usernamePasswordAuthenticationToken);

			Authentication usernamePasswordAuthentication = authenticationManager
				.authenticate(usernamePasswordAuthenticationToken);

			prepareGenerateAuthenticationToken(registeredClient, usernamePasswordAuthenticationToken);
			return generateAuthenticationToken(resourceOwnerBaseAuthentication, clientPrincipal, registeredClient,
					scopeSet, usernamePasswordAuthentication);

		}
		catch (AuthenticationException ex) {
			throw oAuth2AuthenticationException(authentication, ex);
		}
		catch (Exception e) {
			log.error("发放Token异常:", e);
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR));
		}
	}

	/**
	 * callback invoked before generate authentication
	 * @param registeredClient the RegisteredClient
	 * @param authentication the principal
	 */
	protected void prepareGenerateAuthenticationToken(RegisteredClient registeredClient,
			Authentication authentication) {
		Object unlimitedSetting = registeredClient.getClientSettings()
			.getSettings()
			.get(AuthConstants.ClientOpts.KEY_ONLINE_UNLIMITED);
		boolean unlimited = false;
		if (unlimitedSetting != null) {
			unlimited = ObjectUtils.defaultIfNull(BooleanUtils.toBooleanObject(unlimitedSetting.toString()), false);
		}
		if (unlimited) {
			return;
		}

		if (authorizationService instanceof ManagedOAuth2AuthorizationService) {
			ManagedOAuth2AuthorizationService managedAuthorizationService = (ManagedOAuth2AuthorizationService) this.authorizationService;
			int count = managedAuthorizationService.removeByUsername(registeredClient, authentication);
			if (log.isDebugEnabled()) {
				log.debug("remove user token due to online limit ,client:{}, user: {},removed token: {}",
						registeredClient.getClientId(), authentication.getName(), count);
			}
		}
		else {
			log.warn("Limit user online is not supported,please use ManagedOAuth2AuthorizationService");
		}
	}

	/**
	 * 生成新的令牌
	 * @param authenticationToken
	 * @param clientPrincipal
	 * @param registeredClient
	 * @param authorizedScopes
	 * @param usernamePasswordAuthentication
	 * @return OAuth2AccessTokenAuthenticationToken
	 */
	private OAuth2AccessTokenAuthenticationToken generateAuthenticationToken(T authenticationToken,
			OAuth2ClientAuthenticationToken clientPrincipal, RegisteredClient registeredClient,
			Set<String> authorizedScopes, Authentication usernamePasswordAuthentication) {
		// @formatter:off
		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
				.registeredClient(registeredClient)
				.principal(usernamePasswordAuthentication)
				.authorizationServerContext(AuthorizationServerContextHolder.getContext())
				.authorizedScopes(authorizedScopes)
				.authorizationGrantType(authenticationToken.getAuthorizationGrantType())
				.authorizationGrant(authenticationToken);
		// @formatter:on

		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
			.principalName(usernamePasswordAuthentication.getName())
			.authorizationGrantType(authenticationToken.getAuthorizationGrantType())
			// 0.4.0 新增的方法
			.authorizedScopes(authorizedScopes);

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the access token.", ERROR_URI);
			throw new OAuth2AuthenticationException(error);
		}
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
				generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
				generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.id(accessToken.getTokenValue())
				.token(accessToken,
						(metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
								((ClaimAccessor) generatedAccessToken).getClaims()))
				// 0.4.0 新增的方法
				.authorizedScopes(authorizedScopes)
				.attribute(Principal.class.getName(), usernamePasswordAuthentication);
		}
		else {
			authorizationBuilder.id(accessToken.getTokenValue()).accessToken(accessToken);
		}

		// ----- Refresh token -----
		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN) &&
		// Do not issue refresh token to public client
				!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			if (ObjectUtils.isNotEmpty(this.refreshTokenGenerator)) {
				Instant issuedAt = Instant.now();
				Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
				refreshToken = new OAuth2RefreshToken(this.refreshTokenGenerator.get(), issuedAt, expiresAt);
			}
			else {
				tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
				OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
				if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
					OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
							"The token generator failed to generate the refresh token.", ERROR_URI);
					throw new OAuth2AuthenticationException(error);
				}
				refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
			}
			authorizationBuilder.refreshToken(refreshToken);
		}

		OAuth2Authorization authorization = authorizationBuilder.build();

		this.authorizationService.save(authorization);

		log.debug("returning OAuth2AccessTokenAuthenticationToken");

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken,
				Objects.requireNonNull(authorization.getAccessToken().getClaims()));
	}

	/**
	 * 登录异常转换为oauth2异常
	 * @param authentication 身份验证
	 * @param authenticationException 身份验证异常
	 * @return {@link OAuth2AuthenticationException}
	 */
	private OAuth2AuthenticationException oAuth2AuthenticationException(Authentication authentication,
			AuthenticationException authenticationException) {

		if (authenticationException instanceof ScopeException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE, "invalid_scope", ""));
		}

		// OAuth2AuthenticationException 直接返回
		if (authenticationException instanceof OAuth2AuthenticationException) {
			return (OAuth2AuthenticationException) authenticationException;
		}

		if (authenticationException instanceof UsernameNotFoundException) {
			String msg = String.format("Username %s not found", authentication.getName());
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.USERNAME_NOT_FOUND, msg, ""));
		}
		if (authenticationException instanceof BadCredentialsException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.BAD_CREDENTIALS, "Bad credentials", ""));
		}
		if (authenticationException instanceof LockedException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.USER_LOCKED, "User account is locked", ""));
		}
		if (authenticationException instanceof DisabledException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.USER_DISABLE, "User is disabled", ""));
		}
		if (authenticationException instanceof AccountExpiredException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.USER_EXPIRED, "User account has expired", ""));
		}
		if (authenticationException instanceof CredentialsExpiredException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.CREDENTIALS_EXPIRED, "User credentials have expired", ""));
		}

		log.error(authenticationException.getLocalizedMessage());
		return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR),
				authenticationException.getLocalizedMessage(), authenticationException);
	}

	private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
			Authentication authentication) {

		OAuth2ClientAuthenticationToken clientPrincipal = null;

		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}

		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}

		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}

}
