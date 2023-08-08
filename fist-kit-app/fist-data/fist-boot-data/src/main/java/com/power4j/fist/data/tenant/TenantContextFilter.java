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

package com.power4j.fist.data.tenant;

import com.power4j.coca.kit.common.exception.WrappedException;
import com.power4j.fist.data.tenant.isolation.TenantBroker;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/22
 * @since 1.0
 */
public class TenantContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String value = TenantUtil.resolveTenantId(request).orElse(null);
		if (Objects.isNull(value)) {
			filterChain.doFilter(request, response);
			return;
		}
		try {
			TenantBroker.runAs(value, () -> filterChain.doFilter(request, response), null);
		}
		catch (WrappedException e) {
			if (e.getCause() instanceof ServletException) {
				throw e.getOriginal(ServletException.class);
			}
			else if (e.getCause() instanceof IOException) {
				throw e.getOriginal(IOException.class);
			}
		}
	}

}
