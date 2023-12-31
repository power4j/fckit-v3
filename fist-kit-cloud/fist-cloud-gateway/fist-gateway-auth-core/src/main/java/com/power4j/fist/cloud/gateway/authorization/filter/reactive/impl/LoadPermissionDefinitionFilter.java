/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.cloud.gateway.authorization.filter.reactive.impl;

import com.power4j.fist.boot.common.matcher.PathMatcher;
import com.power4j.fist.cloud.gateway.authorization.domain.ApiProxy;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthContext;
import com.power4j.fist.cloud.gateway.authorization.domain.AuthProblem;
import com.power4j.fist.cloud.gateway.authorization.filter.reactive.GatewayAuthFilter;
import com.power4j.fist.security.core.authorization.domain.PermissionDefinition;
import com.power4j.fist.security.core.authorization.filter.reactive.ServerAuthFilterChain;
import com.power4j.fist.security.core.authorization.service.reactive.ReactivePermissionDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/26
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LoadPermissionDefinitionFilter implements GatewayAuthFilter {

	private final PathMatcher pathMatcher;

	private final ReactivePermissionDefinitionService<? extends PermissionDefinition> reactivePermissionDefinitionService;

	@Override
	public Mono<Void> filter(AuthContext ctx, ServerAuthFilterChain<AuthContext> chain) {
		final ApiProxy upstream = ctx.getUpstream();
		if (null == upstream) {
			if (log.isDebugEnabled()) {
				log.debug("No upstream for this request,block access by default. => {}",
						ctx.getInbound().shortDescription());
			}
			return exitChain(ctx,
					AuthProblem.AUTH_EXCEPTION.moreInfo("No upstream for:" + ctx.getInbound().shortDescription()));
		}
		final String api = upstream.getPath();
		// @formatter:off
		return reactivePermissionDefinitionService
				.getPermissionDefinition(upstream.getServiceName(), upstream.getMethod())
				.switchIfEmpty(Mono.defer(() -> {
					if (null == ctx.getPermissionDefinition()) {
						log.debug("No permission definition for {}", api);
					}
					return Mono.just(Collections.emptyList());
				}))
				.onErrorResume(ex -> {
					log.error("Load permission definition error",ex);
					return Mono.just(Collections.emptyList());
				})
				.flatMap(list -> {
					PermissionDefinition matched = pathMatcher.bestMatch(list, api, PermissionDefinition::getPath)
							.orElse(null);
					if(Objects.isNull(matched)){
						log.debug("No permission definition matched for {} {}",upstream.getServiceName(), api);
					}else{
						log.trace("Request matches : {} => {}",ctx.getInbound().shortDescription(),matched.getPath());
					}
					ctx.setPermissionDefinition(matched);
					return doNext(ctx, chain);
				});
		// @formatter:on
	}

}
