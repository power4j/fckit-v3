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

package com.power4j.fist.oauth2.extension.event;

import com.power4j.fist.auth.event.AbstractAuthEvent;
import lombok.experimental.SuperBuilder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@SuperBuilder
public class OauthSuccessEvent extends AbstractAuthEvent {

	private final String username;

	private final OAuth2AccessTokenAuthenticationToken authentication;

}