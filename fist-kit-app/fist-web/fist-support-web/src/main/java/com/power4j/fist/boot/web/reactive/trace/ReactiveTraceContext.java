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

package com.power4j.fist.boot.web.reactive.trace;

import com.power4j.fist.trace.context.TraceContextSnapshot;
import org.springframework.web.server.ServerWebExchange;
import reactor.util.context.ContextView;

import java.util.Optional;

/**
 * Reactive 追踪上下文协议。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public final class ReactiveTraceContext {

	public static final String KEY_TRACE_CONTEXT = "fist_trace_context_snapshot";

	private static final String EXCHANGE_ATTRIBUTE_TRACE_CONTEXT = ReactiveTraceContext.class.getName() + ".snapshot";

	private ReactiveTraceContext() {
	}

	public static Optional<TraceContextSnapshot> getSnapshot(ContextView context) {
		Object value = context.getOrDefault(KEY_TRACE_CONTEXT, null);
		return value instanceof TraceContextSnapshot snapshot ? Optional.of(snapshot) : Optional.empty();
	}

	public static void putSnapshot(ServerWebExchange exchange, TraceContextSnapshot snapshot) {
		exchange.getAttributes().put(EXCHANGE_ATTRIBUTE_TRACE_CONTEXT, snapshot);
	}

	public static Optional<TraceContextSnapshot> getSnapshot(ServerWebExchange exchange) {
		Object value = exchange.getAttribute(EXCHANGE_ATTRIBUTE_TRACE_CONTEXT);
		return value instanceof TraceContextSnapshot snapshot ? Optional.of(snapshot) : Optional.empty();
	}

}
