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
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/15
 * @since 1.0
 */
public class PageData<T> implements Serializable {

	private final static long serialVersionUID = 1L;

	private final List<T> content;

	private final long total;

	private final boolean hasNext;

	public static <T> PageData<T> empty() {
		return new PageData<>(Collections.emptyList(), 0L, false);
	}

	public static <T> PageData<T> of(List<T> content, long total, boolean hasNext) {
		return new PageData<>(content, total, hasNext);
	}

	public static <T> PageData<T> of(Paged<T> paged) {
		return new PageData<>(paged.getContent(), paged.getTotalElements(), paged.hasNext());
	}

	PageData(List<T> content, long total, boolean hasNext) {
		this.content = content;
		this.total = total;
		this.hasNext = hasNext;
	}

	public List<T> getContent() {
		return content;
	}

	public long getTotal() {
		return total;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public <U> PageData<U> map(Function<? super T, ? extends U> converter) {
		return new PageData<>(getConvertedContent(converter), total, hasNext);
	}

	protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {

		Assert.notNull(converter, "Function must not be null!");

		return this.content.stream().map(converter).collect(Collectors.toList());
	}

}