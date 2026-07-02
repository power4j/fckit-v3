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

import com.power4j.fist.trace.context.InboundTraceContext;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 {@link ServerHttpRequest} 的入口追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class ServerHttpRequestInboundTraceContext implements InboundTraceContext {

	private final ServerHttpRequest request;

	private final Map<String, String> values = new LinkedHashMap<>();

	public ServerHttpRequestInboundTraceContext(ServerHttpRequest request) {
		this.request = request;
	}

	@Override
	public Optional<String> readHeader(String name) {
		return Optional.ofNullable(this.request.getHeaders().getFirst(name));
	}

	@Override
	public Optional<String> getValue(String name) {
		return Optional.ofNullable(this.values.get(name));
	}

	@Override
	public void putValue(String name, String value) {
		this.values.put(name, value);
	}

	@Override
	public Map<String, String> values() {
		return Map.copyOf(this.values);
	}

}
