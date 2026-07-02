package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.OutboundTraceContext;
import com.power4j.fist.trace.context.TraceContexts;
import org.springframework.http.HttpRequest;

import java.util.Optional;

/**
 * Spring HTTP 客户端出口载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
class ClientHttpRequestOutboundTraceContext implements OutboundTraceContext {

	private final HttpRequest request;

	ClientHttpRequestOutboundTraceContext(HttpRequest request) {
		this.request = request;
	}

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void writeHeader(String name, String value) {
		this.request.getHeaders().set(name, value);
	}

	@Override
	public boolean hasHeader(String name) {
		return this.request.getHeaders().containsKey(name);
	}

}
