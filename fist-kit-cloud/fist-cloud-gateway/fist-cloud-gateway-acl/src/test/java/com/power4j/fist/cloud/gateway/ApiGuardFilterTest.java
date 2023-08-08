package com.power4j.fist.cloud.gateway;

import com.power4j.fist.boot.security.core.UserInfo;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.domain.RequestInfo;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilterChain;
import com.power4j.fist.cloud.security.AccessDeniedHandler;
import com.power4j.fist.cloud.security.AccessPermittedHandler;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.lang.Nullable;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
class ApiGuardFilterTest {

	private GatewayAuthFilterChain authFilterChain = (c) -> Mono.empty();

	private GatewayFilterChain gatewayFilterChain = mock(GatewayFilterChain.class);

	private AccessPermittedHandler permittedHandler = mock(AccessPermittedHandler.class);

	private AccessDeniedHandler deniedHandler = mock(AccessDeniedHandler.class);

	@Test
	public void shouldCallAccessDenied() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost").build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);
		ApiGuardFilter filter = spy(new ApiGuardFilter(authFilterChain, permittedHandler, deniedHandler));

		given(deniedHandler.handleAccessDenied(any(), any())).willReturn(Mono.empty());
		given(filter.makeAuthContext(any()))
			.willReturn(makeAuthContext(exchange, null, AuthProblem.PERMISSION_CHECK_DENIED));

		Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
		StepVerifier.create(result).verifyComplete();

		verify(deniedHandler, times(1)).handleAccessDenied(any(), any());
		verify(permittedHandler, times(0)).handleAccessPermitted(any());

	}

	@Test
	public void shouldCallAccessPermitted() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost").build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);
		ApiGuardFilter filter = spy(new ApiGuardFilter(authFilterChain, permittedHandler, deniedHandler));

		given(permittedHandler.handleAccessPermitted(any())).willReturn(Mono.just(exchange));
		given(filter.makeAuthContext(any()))
			.willReturn(makeAuthContext(exchange, new UserInfo(), AuthProblem.PUB_ACCESS));
		given(gatewayFilterChain.filter(any())).willReturn(Mono.empty());

		Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
		StepVerifier.create(result).verifyComplete();

		verify(deniedHandler, times(0)).handleAccessDenied(any(), any());
		verify(permittedHandler, times(1)).handleAccessPermitted(any());

	}

	static AuthContext makeAuthContext(ServerWebExchange exchange, @Nullable Object user, AuthProblem problem) {
		AuthContext context = new AuthContext(exchange, user);
		RequestInfo info = new RequestInfo(exchange.getRequest().getHeaders(), exchange.getRequest().getMethod(),
				exchange.getRequest().getURI());
		context.setInbound(info);
		context.getAuthState().setProblem(problem);
		return context;
	}

}
