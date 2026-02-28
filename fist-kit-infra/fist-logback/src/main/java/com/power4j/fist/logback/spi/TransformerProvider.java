/*
 * Copyright 2026. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.logback.spi;

import com.power4j.fist.logback.api.Transformer;

/**
 * {@link Transformer} 工厂，通过 SPI 注册。每条规则调用 {@link #create()} 获取独立实例。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public interface TransformerProvider {

	/**
	 * Provider 名称，对应配置中 {@code rule.<name>.transformer=<providerName>}，大小写敏感。
	 * @return provider 名称
	 */
	String name();

	/**
	 * 创建新的 {@link Transformer} 实例。
	 * @return 新实例
	 */
	Transformer create();

}
