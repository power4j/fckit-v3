package com.power4j.fist.autoconfigure.gateway;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.power4j.fist.boot.common.matcher.FastPathMatcher;
import com.power4j.fist.boot.common.matcher.PathMatcher;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.DefaultGatewayAuthFilterChain;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilterChain;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.AuthEndFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.InternalAccessFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.LoadPermissionDefinitionFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.LoginAccessFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.Oauth2IntrospectFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.PrepareAuthFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.PublicAccessFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.ReadRouteFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.SafeModeFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.SkipAuthorizationFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.TenantFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.UserIpAccessFilter;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl.UserPermissionFilter;
import com.power4j.fist.cloud.security.oauth2.client.UserIntrospectClient;
import com.power4j.fist.security.core.authorization.config.GlobalAuthorizationProperties;
import com.power4j.fist.security.core.authorization.domain.PermissionDefinition;
import com.power4j.fist.security.core.authorization.service.reactive.ReactivePermissionDefinitionService;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GlobalAuthorizationProperties.class)
@ConditionalOnProperty(prefix = GlobalAuthorizationProperties.PROP_PREFIX, name = "enabled", matchIfMissing = true)
public class GatewayAuthFilterConfiguration {

	static final int BASE_ORDER = 10_000;

	private final GlobalAuthorizationProperties authorizationProperties;

	private final ReactivePermissionDefinitionService<? extends PermissionDefinition> permissionDefinitionService;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(Caffeine.class)
	public FastPathMatcher fastPathMatcher() {
		return new FastPathMatcher(2048);
	}

	@Bean
	@Order(BASE_ORDER + 100)
	public PrepareAuthFilter prepareAuthFilter() {
		return new PrepareAuthFilter();
	}

	@Bean
	@Order(BASE_ORDER + 110)
	@ConditionalOnProperty(prefix = GlobalAuthorizationProperties.PROP_PREFIX, name = "safe-mode.enabled",
			havingValue = "true")
	public SafeModeFilter safeModeFilter() {
		List<String> whitelist = authorizationProperties.getSafeMode().getWhitelist();
		log.info("{} init with whitelist :{}", SafeModeFilter.class.getSimpleName(), StringUtils.join(whitelist, ","));
		return new SafeModeFilter(whitelist);
	}

	@Bean
	@Order(BASE_ORDER + 200)
	public SkipAuthorizationFilter skipAuthorizationFilter(PathMatcher pathMatcher) {
		Set<String> skip = authorizationProperties.getSkip();
		log.info("{} init with skip list :{}", SkipAuthorizationFilter.class.getSimpleName(),
				StringUtils.join(skip, ","));
		return new SkipAuthorizationFilter(authorizationProperties.getSkip(), pathMatcher);
	}

	@Bean
	@Order(BASE_ORDER + 300)
	public ReadRouteFilter readRouteFilter() {
		return new ReadRouteFilter();
	}

	@Bean
	@Order(BASE_ORDER + 400)
	public LoadPermissionDefinitionFilter loadPermissionDefinitionFilter(PathMatcher pathMatcher) {
		return new LoadPermissionDefinitionFilter(pathMatcher, permissionDefinitionService);
	}

	@Bean
	@Order(BASE_ORDER + 500)
	public InternalAccessFilter internalAccessFilter() {
		return new InternalAccessFilter();
	}

	@Bean
	@Order(BASE_ORDER + 600)
	public PublicAccessFilter publicAccessFilter() {
		return new PublicAccessFilter();
	}

	@Bean
	@Order(BASE_ORDER + 700)
	public Oauth2IntrospectFilter oauth2IntrospectFilter(UserIntrospectClient client) {
		return new Oauth2IntrospectFilter(client);
	}

	@Bean
	@Order(BASE_ORDER + 800)
	public UserIpAccessFilter userIpAccessFilter() {
		final GlobalAuthorizationProperties.AccessIpConfig config = authorizationProperties.getAccessIp();
		Map<String, List<IPAddress>> rules = new HashMap<>(8);
		List<IPAddress> global = config.getGlobal()
			.stream()
			.map(o -> new IPAddressString(o).getAddress())
			.collect(Collectors.toList());
		rules.put(UserIpAccessFilter.ANY_USER, global);
		config.getRules().forEach((k, v) -> {
			List<IPAddress> rule = v.stream()
				.map(o -> new IPAddressString(o).getAddress())
				.collect(Collectors.toList());
			rules.put(k, rule);
		});
		return new UserIpAccessFilter(rules, config.getMaxTrustResolves());
	}

	@Bean
	@Order(BASE_ORDER + 1_100)
	public LoginAccessFilter loginAccessFilter() {
		return new LoginAccessFilter();
	}

	@Bean
	@Order(BASE_ORDER + 1_190)
	@ConditionalOnProperty(prefix = GlobalAuthorizationProperties.PROP_FILTERS + ".tenant", name = "enabled",
			matchIfMissing = true)
	public TenantFilter tenantFilter() {
		final String paramKey = authorizationProperties.getAuth().getTenantParameter();
		final String headerKey = authorizationProperties.getAuth().getTenantHeader();
		return new TenantFilter(paramKey, headerKey);
	}

	@Bean
	@Order(BASE_ORDER + 1_200)
	public UserPermissionFilter userPermissionFilter() {
		return new UserPermissionFilter();
	}

	@Bean
	@Order
	public AuthEndFilter authEndFilter() {
		return new AuthEndFilter();
	}

	@Bean
	@ConditionalOnMissingBean
	public GatewayAuthFilterChain gatewayAuthFilterChain(List<GatewayAuthFilter> authFilters) {
		return new DefaultGatewayAuthFilterChain(authFilters);
	}

}
