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

package com.power4j.fist.logback.layout;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;
import com.power4j.fist.logback.core.ProcessorChain;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 消息处理 Layout 兼容层，内部委托 {@link ProcessorChain} 处理消息后再交给 {@link PatternLayout} 输出整行。
 * <p>
 * 仅用于兼容旧 layout 配置，不推荐新增使用。推荐使用
 * {@code com.power4j.fist.logback.converter.SensitiveConverter} 作为主路径。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 * @deprecated 请使用 SensitiveConverter + {@code %mask} pattern 替代。
 */
@Deprecated
public class MessageProcessorLayout extends PatternLayout {

	private ProcessorChain processorChain;

	@Override
	public void start() {
		processorChain = new ProcessorChain(loadProcessors());
		processorChain.setContext(getContext());
		processorChain.start();
		super.start();
	}

	/**
	 * 加载消息处理器列表，默认通过 SPI 自动发现。子类可覆盖此方法以注入测试处理器。
	 * @return 处理器列表
	 */
	protected List<MessageProcessor> loadProcessors() {
		List<MessageProcessor> processors = new ArrayList<>();
		ServiceLoader<MessageProcessor> loader = ServiceLoader.load(MessageProcessor.class);
		for (MessageProcessor processor : loader) {
			processors.add(processor);
		}
		return processors;
	}

	@Override
	public String doLayout(ILoggingEvent event) {
		String fullLine = super.doLayout(event);
		String originalMessage = event.getFormattedMessage();
		if (originalMessage == null || originalMessage.isEmpty()) {
			return fullLine;
		}
		LogMessageContext context = new LogMessageContext(event.getLevel(), event.getLoggerName(),
				event.getThreadName(), event.getTimeStamp(),
				event.getMarkerList() != null && !event.getMarkerList().isEmpty() ? event.getMarkerList().get(0) : null,
				MDC::getCopyOfContextMap);
		String processedMessage = processorChain.execute(originalMessage, context);
		if (processedMessage.equals(originalMessage)) {
			return fullLine;
		}
		int idx = fullLine.indexOf(originalMessage);
		if (idx < 0) {
			return fullLine;
		}
		return fullLine.substring(0, idx) + processedMessage + fullLine.substring(idx + originalMessage.length());
	}

}
