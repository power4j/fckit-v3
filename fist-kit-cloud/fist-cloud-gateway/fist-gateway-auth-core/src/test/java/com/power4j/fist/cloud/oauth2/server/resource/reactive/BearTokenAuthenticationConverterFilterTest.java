package com.power4j.fist.cloud.oauth2.server.resource.reactive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class BearTokenAuthenticationConverterFilterTest {

	@Mock
	private WebFilterChain chain;

	@Mock
	AuthenticationProcessor authenticationProcessor;

	@Mock
	BearerTokenAuthentication bearerTokenAuthentication;

	PublisherProbe<Void> chainResult = PublisherProbe.empty();

	@Test
	public void authenticationProcessorNotCalled() {
		Context context = ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl()));
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		given(this.chain.filter(exchange)).willReturn(this.chainResult.mono());
		BearTokenAuthenticationConverterFilter filter = new BearTokenAuthenticationConverterFilter(
				authenticationProcessor);
		Mono<Void> result = filter.filter(exchange, chain).contextWrite(context);
		StepVerifier.create(result).verifyComplete();
		verify(authenticationProcessor, times(0)).process(any(), any());
	}

	@Test
	public void authenticationProcessorCalled() {
		Context context = ReactiveSecurityContextHolder
			.withSecurityContext(Mono.just(new SecurityContextImpl(bearerTokenAuthentication)));
		MockServerWebExchange serverWebExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
		given(authenticationProcessor.process(any(), any())).willReturn(Mono.just(serverWebExchange));
		given(this.chain.filter(serverWebExchange)).willReturn(this.chainResult.mono());
		BearTokenAuthenticationConverterFilter filter = new BearTokenAuthenticationConverterFilter(
				authenticationProcessor);
		Mono<Void> result = filter.filter(serverWebExchange, chain).contextWrite(context);
		StepVerifier.create(result).verifyComplete();
		verify(authenticationProcessor, times(1)).process(any(), any());
	}

}
