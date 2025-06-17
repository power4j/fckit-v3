/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.cloud.gateway.filter;

import com.power4j.fist.boot.common.logging.LogConstant;
import com.power4j.fist.boot.web.reactive.log.MdcContextLifter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 * @see MdcContextLifter
 */
@RequiredArgsConstructor
public class RequestIdGlobalFilter implements GlobalFilter {

	private final static String CONTEXT_REQUEST_ID_KEY = LogConstant.MDC_REQUEST_ID;

	private final String headerKey;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String requestId = request.getHeaders().getFirst(headerKey);

		if (requestId == null || requestId.isEmpty()) {
			requestId = UUID.randomUUID().toString();
			ServerHttpRequest mutatedRequest = request.mutate().header(headerKey, requestId).build();
			exchange = exchange.mutate().request(mutatedRequest).build();
		}

		final String finalRequestId = requestId;
		return chain.filter(exchange).contextWrite(ctx -> ctx.put(CONTEXT_REQUEST_ID_KEY, finalRequestId));
	}

}
