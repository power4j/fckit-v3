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

package com.power4j.fist.logback.core;

import ch.qos.logback.core.spi.ContextAwareBase;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 消息处理链，负责编排 {@link MessageProcessor} 的排序、过滤与串行执行。
 * <p>
 * 使用方式：
 * <ol>
 * <li>构造时传入处理器列表。</li>
 * <li>调用 {@link #start()} 完成排序与缓存，之后列表不可变。</li>
 * <li>调用 {@link #execute(String, LogMessageContext)} 执行处理链。</li>
 * </ol>
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public class ProcessorChain extends ContextAwareBase {

	private final List<MessageProcessor> input;

	private List<MessageProcessor> sorted;

	public ProcessorChain(List<MessageProcessor> processors) {
		this.input = processors;
	}

	/**
	 * 按 {@link MessageProcessor#order()} 升序排序并缓存处理器列表，同时输出加载统计。
	 */
	public void start() {
		List<MessageProcessor> copy = new java.util.ArrayList<>(input);
		copy.sort(Comparator.comparingInt(MessageProcessor::order));
		sorted = Collections.unmodifiableList(copy);
		addInfo("ProcessorChain started with " + sorted.size() + " processor(s):");
		for (MessageProcessor p : sorted) {
			addInfo("  [order=" + p.order() + "] " + p.name());
		}
	}

	/**
	 * 串行执行处理链。
	 * <p>
	 * 单个处理器抛出异常时，跳过该处理器并继续后续链路，同时记录错误日志。
	 * @param message 待处理消息，为 null 或空时直接返回
	 * @param context 消息处理上下文
	 * @return 处理后的消息
	 */
	public String execute(String message, LogMessageContext context) {
		if (message == null || message.isEmpty()) {
			return message;
		}
		if (sorted == null || sorted.isEmpty()) {
			return message;
		}
		String result = message;
		for (MessageProcessor processor : sorted) {
			try {
				if (!processor.supports(context)) {
					continue;
				}
				result = processor.process(result, context);
			}
			catch (Exception e) {
				addError("Processor [" + processor.name() + "] threw an exception, skipping: " + e.getMessage(), e);
			}
		}
		return result;
	}

}
