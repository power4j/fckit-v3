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

import cn.idev.excel.EasyExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/11
 * @since 1.0
 * @param <T> Excel 文档模型
 */
@Slf4j
@Builder
public class ExcelParser<T> {

	@Getter
	private final Class<T> docType;

	private final BiConsumer<? super T, AnalysisContext> handler;

	/**
	 * 解析输入流中的文档对象
	 * @param stream 输入流
	 */
	public void process(InputStream stream) {
		AtomicLong total = new AtomicLong();
		EasyExcel.read(stream, docType, new AnalysisEventListener<T>() {
			@Override
			public void invoke(T data, AnalysisContext context) {
				handler.accept(data, context);
			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {
				if (log.isDebugEnabled()) {
					log.debug("Analyse complete,total = {}", total.get());
				}
			}
		}).sheet().doRead();
	}

}
