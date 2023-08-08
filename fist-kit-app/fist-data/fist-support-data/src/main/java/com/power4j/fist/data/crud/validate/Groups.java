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

package com.power4j.fist.data.crud.validate;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/9
 * @since 1.0
 */
public interface Groups {

	/**
	 * 默认（通用规则）
	 */
	interface Default extends jakarta.validation.groups.Default {

	}

	/**
	 * 新增操作特有规则
	 */
	interface Create extends Default {

	}

	/**
	 * 更新操作特有规则
	 */
	interface Update extends Default {

	}

}
