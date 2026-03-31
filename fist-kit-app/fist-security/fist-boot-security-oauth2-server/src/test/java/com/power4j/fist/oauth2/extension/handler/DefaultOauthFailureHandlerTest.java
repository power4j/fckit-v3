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

package com.power4j.fist.oauth2.extension.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Codex
 * @since 3.13
 */
class DefaultOauthFailureHandlerTest {

	@Test
	void shouldFallbackToExceptionMessageWhenOAuth2ErrorDescriptionIsBlank() throws Exception {
		DefaultOauthFailureHandler handler = new DefaultOauthFailureHandler();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OAuth2AuthenticationException exception = new OAuth2AuthenticationException(new OAuth2Error("bad_credentials"),
				"认证模式已禁用");

		Assertions.assertDoesNotThrow(
				() -> ReflectionTestUtils.invokeMethod(handler, "writeErrorResponse", request, response, exception));
		Assertions.assertTrue(response.getContentAsString().contains("认证模式已禁用"));
	}

}
