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

package com.power4j.fist.logback.api;

import java.util.Map;

/**
 * 转换器：对 {@link MatchSpan} 内的文本做纯函数变换。
 * <p>
 * 契约：{@link #transform} 禁止返回 {@code null}。实现类只能访问 span 内文本，无法访问原始消息或 span
 * 位置，保证变换的纯函数性。实现类在 {@link #init} 完成后须保证线程安全。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public interface Transformer {

	/**
	 * 初始化，由框架在启动时调用一次。框架传入不可变副本，实现类不得修改。
	 * @param props 已剥离前缀的配置参数
	 */
	default void init(Map<String, String> props) {
	}

	/**
	 * 对 span 内文本进行变换。
	 * @param value 由 {@link MatchSpan#extract} 提取的文本
	 * @param context 消息上下文
	 * @return 变换后的文本，不得为 {@code null}
	 */
	String transform(String value, LogMessageContext context);

	/**
	 * 释放资源，由框架在销毁阶段调用一次。
	 */
	default void destroy() {
	}

}
