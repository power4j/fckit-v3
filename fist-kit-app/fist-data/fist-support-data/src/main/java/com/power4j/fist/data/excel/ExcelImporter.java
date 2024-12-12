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

package com.power4j.fist.data.excel;

import cn.idev.excel.context.AnalysisContext;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/11
 * @since 1.0
 * @param <T> Excel 文档模型
 * @param <D> 数据库实体类型
 */
@Slf4j
@Builder
public class ExcelImporter<T, D> {

	private final static int DEFAULT_BATCH_SIZE = 1_000;

	private final Class<T> docType;

	private final Converter<T, D> converter;

	private final Consumer<Collection<D>> entityHandler;

	private final int batchSize;

	/**
	 * 解析流,并导入数据库
	 * @param stream InputStream
	 * @return 返回导入数据条数
	 */
	public long doImport(InputStream stream) {
		int batch = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
		AtomicLong total = new AtomicLong();
		List<T> list = new ArrayList<>(batch);
		BiConsumer<? super T, AnalysisContext> handler = (T data, AnalysisContext context) -> {
			total.incrementAndGet();
			list.add(data);
			if (list.size() >= batch) {
				processAndClear(list);
			}
		};
		// @formatter:off
		ExcelParser.<T>builder()
				.docType(docType)
				.handler(handler)
				.build()
				.process(stream);
		// @formatter:on
		processAndClear(list);
		return total.get();
	}

	protected void processAndClear(Collection<T> data) {
		if (data.isEmpty()) {
			return;
		}
		List<D> entityList = data.stream().map(converter::convert).collect(Collectors.toList());
		entityHandler.accept(entityList);
		data.clear();
	}

}
