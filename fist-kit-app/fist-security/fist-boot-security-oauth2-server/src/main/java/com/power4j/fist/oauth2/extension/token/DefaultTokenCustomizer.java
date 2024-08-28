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

package com.power4j.fist.oauth2.extension.token;

import com.power4j.fist.oauth2.core.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@RequiredArgsConstructor
public class DefaultTokenCustomizer implements OAuth2TokenCustomizer<OAuth2TokenClaimsContext> {

	private final OauthUserPropExporter oauthUserPropExporter;

	@Override
	public void customize(OAuth2TokenClaimsContext context) {
		OAuth2TokenClaimsSet.Builder claims = context.getClaims();
		final String clientId = context.getAuthorizationGrant().getName();
		final String grantType = context.getAuthorizationGrantType().getValue();
		claims.claim(AuthConstants.DETAILS_LICENSE, "LC-7616f11");
		claims.claim(AuthConstants.CLIENT_ID, clientId);
		claims.claim(AuthConstants.ACTIVE, Boolean.TRUE);

		if (AuthConstants.CLIENT_CREDENTIALS.equals(grantType)) {
			return;
		}

		ClientSettings clientSettings = context.getRegisteredClient().getClientSettings();
		String propKey = Objects.toString(clientSettings.getSetting(AuthConstants.ClientOpts.KEY_USER_DETAILS_FIELD),
				AuthConstants.ClientOpts.VAL_USER_DETAILS_FIELD_DEFAULT);
		Object principal = context.getPrincipal().getPrincipal();
		ClaimsContext claimsContext = ClaimsContext.builder()
			.clientId(clientId)
			.grantType(grantType)
			.clientSettings(clientSettings)
			.principal(principal)
			.build();
		claims.claim(propKey, oauthUserPropExporter.export(claimsContext));

	}

}
