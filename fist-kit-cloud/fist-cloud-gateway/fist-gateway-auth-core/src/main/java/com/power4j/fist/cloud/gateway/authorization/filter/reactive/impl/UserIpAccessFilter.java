package com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl;

import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilter;
import com.power4j.fist.security.core.authorization.domain.AuthenticatedUser;
import com.power4j.fist.security.core.authorization.filter.reactive.ServerAuthFilterChain;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
public class UserIpAccessFilter implements GatewayAuthFilter {

	public final static String ANY_USER = "*";

	// key: username, value: list of CIDR rules
	private final Map<String, List<IPAddress>> rules;

	private final XForwardedRemoteAddressResolver resolver;

	public UserIpAccessFilter(Map<String, List<IPAddress>> rules, int maxTrustResolves) {
		this.rules = rules;
		this.resolver = XForwardedRemoteAddressResolver.maxTrustedIndex(maxTrustResolves);
	}

	@Override
	public Mono<Void> filter(AuthContext ctx, ServerAuthFilterChain<AuthContext> chain) {
		if (rules.isEmpty()) {
			return doNext(ctx, chain);
		}
		return applyRules(ctx, chain);
	}

	private Mono<Void> applyRules(AuthContext ctx, ServerAuthFilterChain<AuthContext> chain) {
		final AuthenticatedUser userInfo = ctx.getUserInfo();
		if (Objects.isNull(userInfo)) {
			return exitChain(ctx, AuthProblem.USER_IP_DENIED.moreInfo("No user info"));
		}
		final IPAddress accessIp = new IPAddressString(resolver.resolve(ctx.getExchange()).getHostString())
			.getAddress();
		// apply user based rules
		final String username = userInfo.getUsername();
		if (rules.containsKey(username)) {
			List<IPAddress> ipList = rules.get(username);
			for (IPAddress rule : ipList) {
				if (rule.contains(accessIp)) {
					if (log.isDebugEnabled()) {
						log.debug("user [{}] allowed access from [{}] with rule [{}]", username, accessIp, rule);
					}
					return doNext(ctx, chain);
				}
			}
		}
		else {
			// no user based rules, apply global rules
			if (rules.containsKey(ANY_USER)) {
				List<IPAddress> ipList = rules.get(ANY_USER);
				for (IPAddress rule : ipList) {
					if (rule.contains(accessIp)) {
						if (log.isDebugEnabled()) {
							log.debug("user [{}] allowed access from [{}] with global rule [{}]", username, accessIp,
									rule);
						}
						return doNext(ctx, chain);
					}
				}
			}
		}

		return exitChain(ctx, AuthProblem.USER_IP_DENIED
			.moreInfo(String.format("user [%s] not allowed access from [%s]", username, accessIp)));
	}

}
