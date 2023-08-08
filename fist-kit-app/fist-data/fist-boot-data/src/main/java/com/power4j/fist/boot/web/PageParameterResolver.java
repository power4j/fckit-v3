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

package com.power4j.fist.boot.web;

import com.power4j.fist.boot.web.model.PageParameter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/15
 * @since 1.0
 */
@Slf4j
public class PageParameterResolver extends AbstractPageRequestResolver {

	@Setter
	private String orderFieldKey = "field";

	@Setter
	private String orderKey = "order";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(PageParameter.class);
	}

	@Nullable
	@Override
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (Objects.isNull(request)) {
			return null;
		}
		int page = parseInt(request.getParameter(getPageNumberKey()), PageParameter.FIRST_PAGE);
		int size = parseInt(request.getParameter(getPageSizeKey()), PageParameter.DEFAULT_PAGE_SIZE);
		String orderField = filterFieldName(request.getParameter(orderFieldKey));
		String order = request.getParameter(orderKey);
		return PageParameter.of(page, size, orderField, order);
	}

}
