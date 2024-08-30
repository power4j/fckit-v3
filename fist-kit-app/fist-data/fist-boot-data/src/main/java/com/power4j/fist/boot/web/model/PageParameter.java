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

package com.power4j.fist.boot.web.model;

import com.power4j.fist.data.domain.PageRequest;
import com.power4j.fist.data.domain.Sort;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/15
 * @since 1.0
 */
@Data
public class PageParameter implements Serializable {

	@Serial
	private final static long serialVersionUID = 1L;

	public final static int DEFAULT_PAGE_SIZE = 20;

	public final static int FIRST_PAGE = 1;

	public final static String ORDER_VALUE_ASC = "ascend";

	public final static String ORDER_VALUE_DESC = "descend";

	@Schema(description = "页码", defaultValue = "1")
	private Integer pageNumber;

	@Schema(description = "页大小", defaultValue = "20")
	private Integer pageSize;

	@Nullable
	@Schema(description = "排序字段")
	private String field;

	@Nullable
	@Schema(description = "排序方式 ascend | descend")
	private String order;

	public static PageParameter of(int pageNumber, int pageSize) {
		return of(pageNumber, pageSize, null, null);
	}

	public static PageParameter of(int pageNumber, int pageSize, @Nullable String sortField, @Nullable String order) {
		PageParameter page = new PageParameter();
		page.setPageNumber(Math.max(pageNumber, FIRST_PAGE));
		page.setPageSize(pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE);
		page.setField(sortField);
		page.setOrder(order);
		return page;
	}

	public PageParameter applyDefaultPage() {
		this.pageSize = getPageSizeOrDefault(DEFAULT_PAGE_SIZE);
		this.pageNumber = getPageNumberOrDefault(FIRST_PAGE);
		return this;
	}

	public int getPageNumberOrDefault(int value) {
		int pn = ObjectUtils.defaultIfNull(this.pageNumber, value);
		if (pn < FIRST_PAGE) {
			pn = FIRST_PAGE;
		}
		return pn;
	}

	public int getPageSizeOrDefault(int value) {
		int ps = ObjectUtils.defaultIfNull(this.pageSize, value);
		if (ps < 0) {
			ps = DEFAULT_PAGE_SIZE;
		}
		return ps;
	}

	public PageRequest toPageRequest() {
		if (StringUtils.isNotEmpty(field)) {
			Sort.Direction direction = StringUtils.equalsIgnoreCase(order, ORDER_VALUE_DESC) ? Sort.Direction.DESC
					: Sort.Direction.ASC;
			return PageRequest.of(pageNumber, pageSize, Sort.of(direction, field));
		}
		return PageRequest.of(pageNumber, pageSize);
	}

}
