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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
class SensitiveConverterTest {

	private LoggerContext loggerContext;

	private Logger testLogger;

	@BeforeEach
	void setUp() {
		loggerContext = new LoggerContext();
		loggerContext.setName("test");
		testLogger = loggerContext.getLogger("com.example.TestService");
	}

	private SensitiveConverter converterWith(List<MessageProcessor> processors) {
		SensitiveConverter converter = new SensitiveConverter() {
			@Override
			protected List<MessageProcessor> loadProcessors() {
				return processors;
			}
		};
		converter.setContext(loggerContext);
		converter.start();
		return converter;
	}

	private ILoggingEvent event(String message) {
		return new LoggingEvent("test", testLogger, Level.INFO, message, null, null);
	}

	@Test
	void shouldReturnOriginalMessageWhenNoProcessors() {
		SensitiveConverter converter = converterWith(List.of());
		assertEquals("hello world", converter.convert(event("hello world")));
	}

	@Test
	void shouldApplyProcessorToMessageOnly() {
		MessageProcessor maskMobile = (msg, ctx) -> msg.replace("13012345678", "130****5678");
		SensitiveConverter converter = converterWith(List.of(maskMobile));

		ILoggingEvent evt = event("mobile=13012345678, amount=100");
		String result = converter.convert(evt);

		assertEquals("mobile=130****5678, amount=100", result);
	}

	@Test
	void shouldApplyMultipleProcessorsInOrder() {
		MessageProcessor p1 = new MessageProcessor() {
			@Override
			public int order() {
				return 1;
			}

			@Override
			public String process(String msg, LogMessageContext ctx) {
				return msg.replace("PHONE", "***");
			}
		};
		MessageProcessor p2 = new MessageProcessor() {
			@Override
			public int order() {
				return 2;
			}

			@Override
			public String process(String msg, LogMessageContext ctx) {
				return msg.replace("EMAIL", "e***");
			}
		};
		SensitiveConverter converter = converterWith(List.of(p2, p1));

		String result = converter.convert(event("PHONE EMAIL"));
		assertEquals("*** e***", result);
	}

	@Test
	void shouldProvideCorrectContextToProcessor() {
		final Level[] capturedLevel = new Level[1];
		final String[] capturedLogger = new String[1];
		MessageProcessor inspector = (msg, ctx) -> {
			capturedLevel[0] = ctx.getLevel();
			capturedLogger[0] = ctx.getLoggerName();
			return msg;
		};
		SensitiveConverter converter = converterWith(List.of(inspector));
		converter.convert(event("test"));

		assertEquals(Level.INFO, capturedLevel[0]);
		assertTrue(capturedLogger[0].contains("TestService"));
	}

}
