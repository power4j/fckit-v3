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

package com.power4j.fist.logback.converter;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;
import com.power4j.fist.logback.core.ProcessorChain;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 日志消息脱敏转换器，作为 logback pattern 中 {@code %mask} 的实现。
 * <p>
 * 仅处理 {@code event.getFormattedMessage()}，不触碰 {@code %d/%thread/%logger} 等元数据字段。
 * <p>
 * 配置示例： <pre>{@code
 * <conversionRule conversionWord="mask"
 *     converterClass="com.power4j.fist.logback.converter.SensitiveConverter"/>
 *
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *     <encoder>
 *         <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %mask%n</pattern>
 *     </encoder>
 * </appender>
 * }</pre>
 * <p>
 * {@link MessageProcessor} 实现类通过 SPI 自动发现，在 {@code META-INF/services/} 下注册。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public class SensitiveConverter extends ClassicConverter {

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
	public String convert(ILoggingEvent event) {
		LogMessageContext context = new LogMessageContext(event.getLevel(), event.getLoggerName(),
				event.getThreadName(), event.getTimeStamp(),
				event.getMarkerList() != null && !event.getMarkerList().isEmpty() ? event.getMarkerList().get(0) : null,
				MDC::getCopyOfContextMap);
		return processorChain.execute(event.getFormattedMessage(), context);
	}

}
