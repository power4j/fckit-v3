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

package com.power4j.fist.boot.web.reactive.error;

import com.power4j.fist.boot.web.constant.HttpConstant;
import com.power4j.fist.boot.web.reactive.trace.ReactiveTraceContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextSnapshot;
import com.power4j.fist.trace.context.TraceCorrelation;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/16
 * @since 1.0
 */
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	private final static String ATTRIBUTE_KEY_REQUEST_ID = "requestId";

	@Nullable
	private final TraceContextRuntime runtime;

	public GlobalErrorAttributes() {
		this(null);
	}

	public GlobalErrorAttributes(@Nullable TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		return processErrorAttributes(super.getErrorAttributes(request, options), request);
	}

	protected Map<String, Object> processErrorAttributes(Map<String, Object> attributes, ServerRequest request) {
		ReactiveTraceContext.getSnapshot(request.exchange())
			.flatMap(snapshot -> requestId(snapshot))
			.ifPresent(requestId -> attributes.put(ATTRIBUTE_KEY_REQUEST_ID, requestId));
		if (attributes.containsKey(ATTRIBUTE_KEY_REQUEST_ID)) {
			return attributes;
		}
		final String customRequestId = request.headers().firstHeader(HttpConstant.Header.KEY_REQUEST_ID);
		if (Objects.nonNull(customRequestId)) {
			attributes.put(ATTRIBUTE_KEY_REQUEST_ID, customRequestId);
		}
		return attributes;
	}

	private Optional<String> requestId(TraceContextSnapshot snapshot) {
		if (this.runtime != null) {
			return TraceCorrelation.resolve(this.runtime, snapshot)
				.or(() -> Optional.ofNullable(snapshot.values().get(ATTRIBUTE_KEY_REQUEST_ID)));
		}
		return Optional.ofNullable(snapshot.values().get(ATTRIBUTE_KEY_REQUEST_ID));
	}

}
