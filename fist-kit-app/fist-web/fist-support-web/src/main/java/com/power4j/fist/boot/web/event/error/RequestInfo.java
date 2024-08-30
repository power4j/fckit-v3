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

package com.power4j.fist.boot.web.event.error;

import lombok.Data;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/14
 * @since 1.0
 */
@Data
public class RequestInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private HttpMethod method;

	private String uri;

	@Nullable
	private String query;

	public static RequestInfo from(HttpServletRequest request) {
		RequestInfo info = new RequestInfo();
		info.setMethod(HttpMethod.valueOf(request.getMethod()));
		info.setUri(request.getRequestURI());
		info.setQuery(request.getQueryString());
		return info;
	}

}
