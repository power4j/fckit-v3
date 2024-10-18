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

package com.power4j.fist.boot.mybaits.migrate;

import java.util.Collection;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface BatchUniqueResolver<T> {

	/**
	 * 检测是记录否存在
	 * @param data 数据
	 * @return 已经存在数据条数
	 */
	long exists(Collection<T> data);

	/**
	 * 删除已经存在的记录
	 * @param data 数据
	 */
	void removeExists(Collection<T> data);

}
