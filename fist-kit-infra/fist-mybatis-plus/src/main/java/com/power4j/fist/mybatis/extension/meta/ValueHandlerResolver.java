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

import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public interface ValueHandlerResolver {

	/**
	 * 根据类型查找MetaHandler
	 * @param cls 类型
	 * @return 返回Signer实例
	 */
	Optional<ValueHandler> resolve(Class<?> cls);

}
