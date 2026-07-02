/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.fist.cloud.rpc.feign;

import com.power4j.fist.trace.context.TraceContextRuntime;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 * 基于 {@link TraceContextRuntime} 的 Feign 追踪信息传递处理器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TraceRelayHandler implements RelayHandler {

	private final TraceContextRuntime runtime;

	@Override
	public void handle(@Nullable HttpServletRequest request, RequestTemplate template) {
		this.runtime.outbound(new FeignRequestTemplateOutboundTraceContext(template));
	}

}
