/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.mybatis.extension.meta;

import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public interface ValueHandler {

	/**
	 * Field value evaluation
	 * @param root root object(entity)
	 * @param fieldName field name
	 * @param fieldType field class
	 * @return value
	 * @throws MetaHandlerException if error occurred
	 */
	Object getValue(Object root, String fieldName, Class<?> fieldType);

}
