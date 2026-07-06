package com.power4j.fist.trace.boot.autoconfigure.servlet;

import com.power4j.fist.trace.context.carrier.Slf4jTraceMdcContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 入口追踪上下文 Filter。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceContextServletFilter extends OncePerRequestFilter {

	private final TraceContextRuntime runtime;

	public TraceContextServletFilter(TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		this.runtime.inbound(new ServletInboundTraceContext(request));
		try (Slf4jTraceMdcContext mdc = new Slf4jTraceMdcContext()) {
			this.runtime.syncMdc(mdc);
			filterChain.doFilter(request, response);
		}
		finally {
			TraceContexts.clear();
		}
	}

}
