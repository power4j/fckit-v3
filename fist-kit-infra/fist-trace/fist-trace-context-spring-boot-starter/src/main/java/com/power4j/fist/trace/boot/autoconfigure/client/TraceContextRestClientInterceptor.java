package com.power4j.fist.trace.boot.autoconfigure.client;

import com.power4j.fist.trace.context.TraceContextRuntime;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestClient 追踪上下文透传拦截器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextRestClientInterceptor implements ClientHttpRequestInterceptor {

	private final TraceContextRuntime runtime;

	public TraceContextRestClientInterceptor(TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		this.runtime.outbound(new ClientHttpRequestOutboundTraceContext(request));
		return execution.execute(request, body);
	}

}
