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

import com.power4j.fist.trace.context.TraceContexts;
import com.power4j.fist.trace.context.TraceMdcContext;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 SLF4J MDC 的追踪上下文载体。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class Slf4jTraceMdcContext implements TraceMdcContext, AutoCloseable {

	private final Map<String, String> previousValues = new LinkedHashMap<>();

	@Override
	public Optional<String> getValue(String name) {
		return TraceContexts.current().flatMap(context -> context.getValue(name));
	}

	@Override
	public void putMdc(String name, String value) {
		if (!this.previousValues.containsKey(name)) {
			this.previousValues.put(name, MDC.get(name));
		}
		MDC.put(name, value);
	}

	@Override
	public void close() {
		this.previousValues.forEach((name, value) -> {
			if (value == null) {
				MDC.remove(name);
			}
			else {
				MDC.put(name, value);
			}
		});
	}

}
