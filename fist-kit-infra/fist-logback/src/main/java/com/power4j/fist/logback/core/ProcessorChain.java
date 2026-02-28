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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理链，负责编排 {@link MessageProcessor} 的初始化、排序、过滤、串行执行与销毁。
 * <p>
 * 使用方式：
 * <ol>
 * <li>构造时传入处理器列表与全局 options。</li>
 * <li>调用 {@link #start()} 完成排序与初始化，之后活跃列表不可变。</li>
 * <li>调用 {@link #execute(String, LogMessageContext)} 执行处理链。</li>
 * <li>调用 {@link #destroy()} 释放资源。</li>
 * </ol>
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public class ProcessorChain extends ContextAwareBase {

	private final List<MessageProcessor> input;

	private final Map<String, String> globalOptions;

	/** init 成功的处理器，start() 后不可变 */
	private List<MessageProcessor> active;

	public ProcessorChain(List<MessageProcessor> processors, Map<String, String> options) {
		this.input = processors;
		this.globalOptions = options;
	}

	/**
	 * 按 {@link MessageProcessor#order()} 升序排序，依次调用 {@code init()}。init 失败的处理器被禁用并
	 * addWarn，不影响其余处理器。
	 */
	public void start() {
		List<MessageProcessor> sorted = new ArrayList<>(input);
		sorted.sort(Comparator.comparingInt(MessageProcessor::order));
		List<MessageProcessor> initialized = new ArrayList<>();
		for (MessageProcessor p : sorted) {
			try {
				p.init(buildOptions(p.name()));
				initialized.add(p);
				addInfo("[init] " + p.name() + " ok");
			}
			catch (Throwable t) {
				addWarn("[init] " + p.name() + " failed, disabled: " + t.getMessage());
			}
		}
		this.active = Collections.unmodifiableList(initialized);
		addInfo("ProcessorChain started with " + active.size() + " active processor(s)");
	}

	/**
	 * 串行执行处理链。单个处理器抛出 {@link Throwable} 时，保留当前消息并继续后续链路。
	 * @param message 待处理消息，为 null 或空时直接返回
	 * @param context 消息处理上下文
	 * @return 处理后的消息
	 */
	public String execute(String message, LogMessageContext context) {
		if (message == null || message.isEmpty()) {
			return message;
		}
		if (active == null || active.isEmpty()) {
			return message;
		}
		String result = message;
		for (MessageProcessor processor : active) {
			try {
				if (!processor.supports(context)) {
					continue;
				}
				String out = processor.process(result, context);
				result = (out != null) ? out : result;
			}
			catch (Throwable t) {
				addWarn("[process] " + processor.name() + " threw exception, keeping original: " + t.getMessage());
			}
		}
		return result;
	}

	/**
	 * 依次调用每个活跃处理器的 {@link MessageProcessor#destroy()}，释放资源。
	 */
	public void destroy() {
		if (active == null) {
			return;
		}
		for (MessageProcessor p : active) {
			try {
				p.destroy();
			}
			catch (Throwable t) {
				addWarn("[destroy] " + p.name() + ": " + t.getMessage());
			}
		}
	}

	/**
	 * 构建处理器专属 options：全局键（排除 {@code processor.*} 命名空间）加上 {@code processor.<name>.*}
	 * 覆盖项（剥离前缀后合并）。
	 */
	private Map<String, String> buildOptions(String processorName) {
		Map<String, String> result = new LinkedHashMap<>();
		String ns = "processor." + processorName + ".";
		globalOptions.forEach((k, v) -> {
			if (!k.startsWith("processor.")) {
				result.put(k, v);
			}
		});
		globalOptions.forEach((k, v) -> {
			if (k.startsWith(ns)) {
				result.put(k.substring(ns.length()), v);
			}
		});
		return Collections.unmodifiableMap(result);
	}

}
