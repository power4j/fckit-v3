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

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class HeaderRelayHandler implements RelayHandler {

	private final Set<String> headers;

	@Override
	public void handle(@Nullable HttpServletRequest request, RequestTemplate template) {
		if (request == null) {

			if (log.isTraceEnabled()) {
				log.trace("no request,skip relay headers");
			}

			return;
		}
		if (ObjectUtils.isEmpty(headers)) {
			if (log.isTraceEnabled()) {
				log.trace("no relay headers configured,skip relay headers");
			}

			return;
		}
		headers.forEach(k -> {
			String val = request.getHeader(k);
			if (val != null) {
				if (log.isTraceEnabled()) {
					log.trace("relay header:{}", k);
				}
				template.header(k, val);
			}
		});
	}

}
