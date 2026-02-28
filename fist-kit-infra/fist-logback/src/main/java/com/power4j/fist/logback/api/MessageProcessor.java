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
 * 日志消息处理器，对消息内容进行加工（脱敏、归一化、裁剪等）。
 * <p>
 * 实现类通过 SPI（{@code ServiceLoader}）注册，由 {@code ProcessorChain} 统一编排。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public interface MessageProcessor {

	/**
	 * 处理器名称，用于日志与诊断输出。
	 * @return 处理器名称
	 */
	default String name() {
		return getClass().getSimpleName();
	}

	/**
	 * 执行顺序，值越小越先执行。
	 * @return 顺序值
	 */
	default int order() {
		return 0;
	}

	/**
	 * 判断当前处理器是否处理该上下文。
	 * @param context 消息处理上下文
	 * @return {@code true} 表示参与处理
	 */
	default boolean supports(LogMessageContext context) {
		return true;
	}

	/**
	 * 接收来自 Converter 的配置参数（如 configFile 路径）。由 {@code SensitiveConverter} 在启动时调用。
	 * @param options 从 pattern 选项解析出的键值对，例如 {@code %mask{configFile=path}}
	 */
	default void configure(Map<String, String> options) {
	}

	/**
	 * 对消息内容进行加工。
	 * @param message 待处理的消息文本
	 * @param context 消息处理上下文
	 * @return 加工后的消息文本
	 */
	String process(String message, LogMessageContext context);

}
