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

import com.power4j.fist.trace.context.OutboundTraceContext;
import com.power4j.fist.trace.context.TraceContexts;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * 基于 Feign {@link RequestTemplate} 的出口追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@RequiredArgsConstructor
public class FeignRequestTemplateOutboundTraceContext implements OutboundTraceContext {

	private final RequestTemplate template;

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void writeHeader(String name, String value) {
		this.template.header(name, value);
	}

	@Override
	public boolean hasHeader(String name) {
		return this.template.headers().containsKey(name);
	}

}
