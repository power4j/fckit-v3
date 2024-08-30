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

import com.power4j.fist.data.domain.Paged;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class PageData<T> implements Serializable {

	@Serial
	private final static long serialVersionUID = 1L;

	@Schema(description = "数据")
	private final List<T> content;

	@Schema(description = "总行数")
	private final int total;

	@Schema(description = "是否有下一页")
	private final boolean hasNext;

	@Schema(description = "页码")
	private final Integer pageNumber;

	@Schema(description = "页大小")
	private final Integer pageSize;

	public static <T> PageData<T> empty() {
		return new PageData<>(Collections.emptyList(), 0, false, 0, 0);
	}

	public static <T> PageData<T> of(List<T> content, int total, boolean hasNext, Integer pageNumber,
			Integer pageSize) {
		return new PageData<>(content, total, hasNext, pageNumber, pageSize);
	}

	public static <T> PageData<T> of(Paged<T> paged) {
		long total = paged.getTotalElements();
		if (total < Integer.MIN_VALUE || total > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(total + " cannot be cast to int value.");
		}
		return new PageData<>(paged.getContent(), (int) total, paged.hasNext(), paged.getPageNumber(),
				paged.getPageSize());
	}

	public static <T> PageData<T> of(PageDTO<T> dto) {
		return new PageData(dto.getContent(), dto.getTotal(), dto.getHasNext(), dto.getPageNumber(), dto.getPageSize());
	}

	public static <T> PageDTO<T> toPageDTO(PageData<T> src) {
		PageDTO<T> dto = new PageDTO<>();
		dto.setContent(src.getContent());
		dto.setTotal(src.getTotal());
		dto.setHasNext(src.isHasNext());
		dto.setPageNumber(src.getPageNumber());
		dto.setPageSize(src.getPageSize());
		return dto;
	}

	public PageData(List<T> content, int total, boolean hasNext, Integer pageNumber, Integer pageSize) {
		this.content = content;
		this.total = total;
		this.hasNext = hasNext;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	public List<T> getContent() {
		return content;
	}

	public int getTotal() {
		return total;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public <U> PageData<U> map(Function<? super T, ? extends U> converter) {
		return new PageData<>(getConvertedContent(converter), total, hasNext, pageNumber, pageSize);
	}

	protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {

		Assert.notNull(converter, "Function must not be null!");

		return this.content.stream().map(converter).collect(Collectors.toList());
	}

}
