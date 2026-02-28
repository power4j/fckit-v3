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

import java.util.List;
import java.util.Map;

/**
 * 识别器：从消息中找出需要处理的区间。
 * <p>
 * 契约：{@link #detect} 禁止返回 {@code null}，无匹配时返回空列表。实现类在 {@link #init} 完成后须保证线程安全。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public interface Detector {

	/**
	 * 初始化，由框架在启动时调用一次。框架传入不可变副本，实现类不得修改。
	 * @param props 已剥离前缀的配置参数
	 */
	default void init(Map<String, String> props) {
	}

	/**
	 * 从消息中检测需要处理的区间列表。
	 * @param message 待检测消息
	 * @param context 消息上下文
	 * @return 区间列表，不得为 {@code null}
	 */
	List<MatchSpan> detect(String message, LogMessageContext context);

	/**
	 * 释放资源，由框架在销毁阶段调用一次。
	 */
	default void destroy() {
	}

}
