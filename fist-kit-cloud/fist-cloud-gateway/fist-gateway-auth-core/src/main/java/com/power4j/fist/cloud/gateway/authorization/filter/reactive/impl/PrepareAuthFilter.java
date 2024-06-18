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

import com.power4j.fist.boot.security.core.SecurityConstant;
import com.power4j.fist.cloud.gateway.AuthUtils;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.domain.RequestInfo;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilter;
import com.power4j.fist.security.core.authorization.filter.reactive.ServerAuthFilterChain;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/26
 * @since 1.0
 */
public class PrepareAuthFilter implements GatewayAuthFilter {

	@Override
	public Mono<Void> filter(AuthContext ctx, ServerAuthFilterChain<AuthContext> chain) {
		final ServerWebExchange exchange = AuthUtils.eraseHeader(ctx.getExchange(),
				SecurityConstant.HEADER_USER_TOKEN_INNER);
		LinkedHashSet<URI> uris = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
		final ServerHttpRequest request = exchange.getRequest();
		ctx.setExchange(exchange);
		URI originUri;
		if (ObjectUtils.isNotEmpty(uris)) {
			originUri = uris.iterator().next();
		}
		else {
			originUri = request.getURI();
		}
		RequestInfo info = new RequestInfo(request.getHeaders(), request.getMethod(), originUri);
		ctx.setInbound(info);
		return doNext(ctx, chain);
	}

}
