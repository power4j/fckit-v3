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

import com.power4j.fist.boot.common.utils.Snowflake;
import com.power4j.fist.boot.web.reactive.constant.ContextConstant;
import com.power4j.fist.boot.web.reactive.log.MdcContextLifter;
import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import com.power4j.fist.boot.web.reactive.trace.ServerHttpRequestInboundTraceContext;
import com.power4j.fist.trace.context.MapOutboundTraceContext;
import com.power4j.fist.trace.context.TraceContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextSnapshot;
import com.power4j.fist.trace.context.TraceContexts;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 * @see MdcContextLifter
 */
@RequiredArgsConstructor
public class RequestIdGlobalFilter implements GlobalFilter {

	private final static Snowflake ID_GEN = new Snowflake();

	private final String headerKey;

	@Nullable
	private final TraceContextRuntime runtime;

	public RequestIdGlobalFilter(String headerKey) {
		this(headerKey, null);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (this.runtime != null) {
			return filterWithTraceRuntime(exchange, chain);
		}
		ServerHttpRequest request = exchange.getRequest();
		String requestId = request.getHeaders().getFirst(headerKey);

		if (requestId == null || requestId.isEmpty()) {
			requestId = Long.toHexString(ID_GEN.nextId());
			ServerHttpRequest mutatedRequest = request.mutate().header(headerKey, requestId).build();
			exchange = exchange.mutate().request(mutatedRequest).build();
		}

		final String finalRequestId = requestId;
		return chain.filter(exchange).contextWrite(ctx -> ctx.put(ContextConstant.KEY_MDC, finalRequestId));
	}

	private Mono<Void> filterWithTraceRuntime(ServerWebExchange exchange, GatewayFilterChain chain) {
		try {
			TraceContext traceContext = this.runtime
				.inbound(new ServerHttpRequestInboundTraceContext(exchange.getRequest()));
			MapOutboundTraceContext outbound = new MapOutboundTraceContext();
			this.runtime.outbound(outbound);
			ServerWebExchange tracedExchange = mutateHeaders(exchange, outbound);
			TraceContextSnapshot snapshot = traceContext.snapshot();
			ReactiveTraceContext.putSnapshot(tracedExchange, snapshot);
			return chain.filter(tracedExchange)
				.contextWrite(ctx -> ctx.put(ReactiveTraceContext.KEY_TRACE_CONTEXT, snapshot));
		}
		finally {
			TraceContexts.clear();
		}
	}

	private ServerWebExchange mutateHeaders(ServerWebExchange exchange, MapOutboundTraceContext outbound) {
		if (outbound.headers().isEmpty()) {
			return exchange;
		}
		ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
		outbound.headers().forEach(builder::header);
		return exchange.mutate().request(builder.build()).build();
	}

}
