/*
 * Copyright 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.data.migrate;

import com.alibaba.excel.context.AnalysisContext;
import com.power4j.fist.data.excel.ExcelParser;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@UtilityClass
public class MigrateUtil {

	private final static int DEFAULT_BATCH_SIZE = 1_000;

	public <T> ImportStatistic importExcel(InputStream stream, Class<T> docType, int batchSize,
			DataImporter<T> consumer) {
		int batch = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
		AtomicLong total = new AtomicLong();
		List<T> list = new ArrayList<>(batch);
		BiConsumer<? super T, AnalysisContext> handler = (T data, AnalysisContext context) -> {
			total.incrementAndGet();
			list.add(data);
			if (list.size() >= batch) {
				processAndClear(list, consumer);
			}
		};
		consumer.beforeAll();
		// @formatter:off
        ExcelParser.<T>builder()
                .docType(docType)
                .handler(handler)
                .build()
                .process(stream);
        // @formatter:on
		processAndClear(list, consumer);
		consumer.afterAll();
		return consumer.statistic();
	}

	static <T> void processAndClear(Collection<T> data, DataImporter<T> consumer) {
		if (data.isEmpty()) {
			return;
		}
		consumer.accept(new ArrayList<>(data));
		data.clear();
	}

}
