package com.power4j.fist.autoconfigure.gateway;

import com.power4j.fist.boot.security.inner.DefaultUserCodec;
import com.power4j.fist.boot.web.constant.HttpConstant;
import com.power4j.fist.cloud.gateway.ApiGuardFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilterChain;
import com.power4j.fist.cloud.gateway.filter.RequestIdGlobalFilter;
import com.power4j.fist.cloud.security.AccessDeniedHandler;
import com.power4j.fist.cloud.security.AccessPermittedHandler;
import com.power4j.fist.cloud.security.DefaultAccessDeniedHandler;
import com.power4j.fist.cloud.security.DefaultAccessPermittedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@Import({ Oauth2Configuration.class, GatewayAuthFilterConfiguration.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class RouteGuardConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AccessPermittedHandler accessPermittedHandler() {
		return new DefaultAccessPermittedHandler(new DefaultUserCodec());
	}

	@Bean
	@ConditionalOnMissingBean
	public AccessDeniedHandler accessDeniedHandler() {
		return new DefaultAccessDeniedHandler();
	}

	@Bean
	@Order
	@ConditionalOnMissingBean
	public ApiGuardFilter apiGuardFilter(GatewayAuthFilterChain authFilterChain,
			AccessPermittedHandler permittedHandler, AccessDeniedHandler deniedHandler) {
		return new ApiGuardFilter(authFilterChain, permittedHandler, deniedHandler);
	}

	@Bean
	@ConditionalOnMissingBean(name = "remoteAddressKeyResolver")
	public KeyResolver remoteAddressKeyResolver() {
		return exchange -> Mono
			.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
	}

	/**
	 * TODO: Maybe move to another configuration like GatewayConfiguration
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
	@ConditionalOnMissingBean
	RequestIdGlobalFilter requestIdGlobalFilter() {
		return new RequestIdGlobalFilter(HttpConstant.Header.KEY_REQUEST_ID);
	}

}
