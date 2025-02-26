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

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface UniqueResolver<T> {

	/**
	 * 检测是记录否存在
	 * @param example 数据
	 * @return true 表示存在
	 */
	boolean exists(T example);

	/**
	 * 删除记录
	 * @param example 数据
	 */
	void remove(T example);

	UniqueResolver<?> INVALID = new UniqueResolver<>() {
		@Override
		public boolean exists(Object example) {
			throw new IllegalStateException("Invalid unique resolver");
		}

		@Override
		public void remove(Object example) {
			throw new IllegalStateException("Invalid unique resolver");
		}
	};

}
