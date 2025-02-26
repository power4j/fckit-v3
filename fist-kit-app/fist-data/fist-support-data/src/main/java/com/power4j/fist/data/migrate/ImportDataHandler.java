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

import java.util.Collection;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface ImportDataHandler<T> {

	/**
	 * 处理输入数据
	 * @param data 输入
	 */
	void accept(Collection<T> data);

	/**
	 * 读取统计数据
	 * @return ImportStatistic
	 */
	ImportStatistic statistic();

	/**
	 * 回调方法,在执行导入前调用
	 */
	default void beforeAll() {
		// do nothing
	}

	/**
	 * 回调方法,在执行导入后调用
	 */
	default void afterAll() {
		// do nothing
	}

}
