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

package com.power4j.fist.oauth2.extension.exceptions;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author jumuning
 * @since 3.1
 */
public class ScopeException extends OAuth2AuthenticationException {

	/**
	 * Constructs a <code>ScopeException</code> with the specified message.
	 * @param msg the detail message.
	 */
	public ScopeException(String msg) {
		super(new OAuth2Error(msg), msg);
	}

	/**
	 * Constructs a {@code ScopeException} with the specified message and root cause.
	 * @param msg the detail message.
	 * @param cause root cause
	 */
	public ScopeException(String msg, Throwable cause) {
		super(new OAuth2Error(msg), cause);
	}

}
