package com.power4j.fist.boot.web.reactive.trace;

import com.power4j.fist.trace.context.MapOutboundTraceContext;
import com.power4j.fist.trace.context.TraceContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextSnapshot;
import com.power4j.fist.trace.context.TraceContexts;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 基于 WebFlux 的入口追踪上下文过滤器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceContextWebFilter implements WebFilter {

	private final TraceContextRuntime runtime;

	public TraceContextWebFilter(TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		try {
			// 同步段完成上下文构建、header 透传和 snapshot 捕获。
			// finally 只清理当前入口线程，不影响后续 Reactor Context。
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

	private static ServerWebExchange mutateHeaders(ServerWebExchange exchange, MapOutboundTraceContext outbound) {
		if (outbound.headers().isEmpty()) {
			return exchange;
		}
		ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
		outbound.headers().forEach(builder::header);
		return exchange.mutate().request(builder.build()).build();
	}

}
