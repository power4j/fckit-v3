/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.autoconfigure.apidoc;

import com.power4j.fist.boot.apidoc.springdoc.extension.ApiTraitOperationCustomizer;
import com.power4j.fist.boot.security.core.SecurityConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/30
 * @since 1.0 FIXME 可配置化
 */
@AutoConfiguration
@ConditionalOnClass(SpringDocConfiguration.class)
public class SpringDocAutoConfiguration {

	private final static String SCHEME_INNER_AUTH = "inner_auth";

	@Bean
	public OperationCustomizer apiTraitOperationCustomizer() {
		return new ApiTraitOperationCustomizer();
	}

	@Bean
	public OpenAPI openApi() {
		// @formatter:off
		return new OpenAPI()
				.components(new Components()
						//API Key, see: https://swagger.io/docs/specification/authentication/api-keys/
						.addSecuritySchemes(SCHEME_INNER_AUTH,apiKeyInnerAuthScheme())
				)
				.addSecurityItem(new SecurityRequirement().addList(SCHEME_INNER_AUTH));
		// @formatter:on
	}

	private SecurityScheme oAuthScheme() {
		// TODO: DEMO
		// @formatter:off
		return new SecurityScheme()
				.type(SecurityScheme.Type.OAUTH2)
				.description("This API uses OAuth 2 with the implicit grant flow. [More info](https://api.example.com/docs/auth)")
				.flows(new OAuthFlows()
						.implicit(new OAuthFlow()
								.authorizationUrl("https://api.example.com/oauth2/authorize")
								.scopes(new Scopes()
										.addString("read_pets", "read your pets")
										.addString("write_pets", "modify pets in your account")
								)
						)
				);
		// @formatter:on
	}

	private SecurityScheme apiKeyInnerAuthScheme() {
		// @formatter:off
		return new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name(SecurityConstant.HEADER_USER_TOKEN_INNER);
		// @formatter:on
	}

	private SecurityScheme basicScheme() {
		// @formatter:off
		return new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("basic");
		// @formatter:on
	}

}
