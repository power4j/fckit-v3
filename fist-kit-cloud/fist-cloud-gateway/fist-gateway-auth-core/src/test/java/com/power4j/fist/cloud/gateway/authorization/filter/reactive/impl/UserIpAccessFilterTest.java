package com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl;

import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthUser;
import com.power4j.fist.security.core.authorization.filter.reactive.ServerAuthFilterChain;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserIpAccessFilterTest {

	private MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
		.remoteAddress(InetSocketAddress.createUnresolved("1.2.3.4", 80))
		.build();

	private ServerWebExchange exchange = MockServerWebExchange.from(request);

	@Spy
	private AuthContext authContext = new AuthContext(exchange, null);

	@Mock
	private ServerAuthFilterChain<AuthContext> authFilterChain;

	@Test
	void shouldCallNextIfNoRules() {
		AuthContext authContext = Mockito.mock(AuthContext.class);
		UserIpAccessFilter filter = new UserIpAccessFilter(Collections.emptyMap(), 1);
		when(authFilterChain.filter(any())).thenReturn(Mono.empty());
		Mono<Void> result = filter.filter(authContext, authFilterChain);
		StepVerifier.create(result).verifyComplete();
		verify(authFilterChain, times(1)).filter(any());
	}

	@Test
	void shouldDenyIfNoAuthState() {
		Map<String, List<IPAddress>> rules = Map.of("admin", List.of(new IPAddressString("0.0.0.0/32").getAddress()));
		UserIpAccessFilter filter = new UserIpAccessFilter(rules, 1);
		when(authContext.getUserInfo()).thenReturn(null);
		Mono<Void> result = filter.filter(authContext, authFilterChain);
		StepVerifier.create(result).verifyComplete();
		assertThat(authContext.getAuthState()).isNotNull();
		assertThat(authContext.getAuthState().getProblem()).isNotNull();
		assertThat(authContext.getAuthState().getProblem().getCode()).isEqualTo(AuthProblem.USER_IP_DENIED.getCode());
		verify(authFilterChain, never()).filter(any());
	}

	@Test
	void shouldCallNextWhenUserRuleMatch() {
		AuthUser user = AuthUser.builder().username("admin").build();
		Map<String, List<IPAddress>> rules = Map.of("admin", List.of(new IPAddressString("1.2.0.0/16").getAddress()));
		UserIpAccessFilter filter = new UserIpAccessFilter(rules, 1);
		when(authContext.getUserInfo()).thenReturn(user);
		when(authFilterChain.filter(any())).thenReturn(Mono.empty());
		Mono<Void> result = filter.filter(authContext, authFilterChain);
		StepVerifier.create(result).verifyComplete();
		verify(authFilterChain, times(1)).filter(any());
	}

	@Test
	void shouldCallNextWhenGlobalRuleMatch() {
		AuthUser user = AuthUser.builder().username("admin").build();
		Map<String, List<IPAddress>> rules = Map.of("*", List.of(new IPAddressString("1.2.3.0/24").getAddress()));
		UserIpAccessFilter filter = new UserIpAccessFilter(rules, 1);
		when(authContext.getUserInfo()).thenReturn(user);
		when(authFilterChain.filter(any())).thenReturn(Mono.empty());
		Mono<Void> result = filter.filter(authContext, authFilterChain);
		StepVerifier.create(result).verifyComplete();
		verify(authFilterChain, times(1)).filter(any());
	}

	@Test
	void shouldDenyIfNotMatch() {
		Map<String, List<IPAddress>> rules = Map.of("xx", List.of(new IPAddressString("0.0.0.0/32").getAddress()));
		UserIpAccessFilter filter = new UserIpAccessFilter(rules, 1);
		when(authContext.getUserInfo()).thenReturn(null);
		Mono<Void> result = filter.filter(authContext, authFilterChain);
		StepVerifier.create(result).verifyComplete();
		assertThat(authContext.getAuthState()).isNotNull();
		assertThat(authContext.getAuthState().getProblem()).isNotNull();
		assertThat(authContext.getAuthState().getProblem().getCode()).isEqualTo(AuthProblem.USER_IP_DENIED.getCode());
		verify(authFilterChain, never()).filter(any());
	}

}
