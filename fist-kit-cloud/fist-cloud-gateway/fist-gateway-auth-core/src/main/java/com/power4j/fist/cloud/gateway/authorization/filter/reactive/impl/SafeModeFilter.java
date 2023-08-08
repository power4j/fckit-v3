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

package com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl;

import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilter;
import com.power4j.fist.security.core.authorization.filter.reactive.ServerAuthFilterChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/15
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SafeModeFilter implements GatewayAuthFilter {

	private final Collection<String> whitelist;

	static boolean matchAny(String input, Collection<String> patterns) {
		return patterns.stream().anyMatch(input::matches);
	}

	@Override
	public Mono<Void> filter(AuthContext ctx, ServerAuthFilterChain<AuthContext> chain) {
		Optional<String> address = Optional.ofNullable(ctx.getExchange().getRequest().getRemoteAddress())
			.map(InetSocketAddress::getAddress)
			.map(InetAddress::getHostAddress);
		boolean isSafe = address.map(s -> matchAny(s, whitelist)).orElse(false);

		if (log.isDebugEnabled()) {
			log.debug("safe check = {},address = {}, whitelist = {}", isSafe, address.orElse("null"),
					StringUtils.join(whitelist, ","));
		}
		return exitChain(ctx, isSafe ? AuthProblem.SAFE_MODE_PASS : AuthProblem.SAFE_MODE_DENIED);
	}

	@PostConstruct
	void postCheck() {
		for (String p : whitelist) {
			try {
				Pattern.compile(p);
			}
			catch (PatternSyntaxException e) {
				log.error("表达式非法:{}", p);
				throw e;
			}
		}
	}

}
