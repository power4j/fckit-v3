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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.power4j.fist.logback.api.MessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
class MessageProcessorLayoutTest {

	private LoggerContext loggerContext;

	private Logger testLogger;

	@BeforeEach
	void setUp() {
		loggerContext = new LoggerContext();
		loggerContext.setName("test");
		testLogger = loggerContext.getLogger("com.example.TestService");
	}

	private MessageProcessorLayout layoutWith(String pattern, List<MessageProcessor> processors) {
		MessageProcessorLayout layout = new MessageProcessorLayout() {
			@Override
			protected List<MessageProcessor> loadProcessors() {
				return processors;
			}
		};
		layout.setContext(loggerContext);
		layout.setPattern(pattern);
		layout.start();
		return layout;
	}

	private ILoggingEvent event(String message) {
		return new LoggingEvent("test", testLogger, Level.INFO, message, null, null);
	}

	@Test
	void shouldPreservePatternFieldsWhenNoProcessors() {
		MessageProcessorLayout layout = layoutWith("%level %logger - %msg", List.of());
		String output = layout.doLayout(event("hello world"));

		assertTrue(output.contains("INFO"));
		assertTrue(output.contains("com.example.TestService"));
		assertTrue(output.contains("hello world"));
	}

	@Test
	void shouldProcessMessageBodyOnly() {
		MessageProcessor maskMobile = (msg, ctx) -> msg.replace("13012345678", "130****5678");
		MessageProcessorLayout layout = layoutWith("%level %logger - %msg", List.of(maskMobile));

		ILoggingEvent evt = event("mobile=13012345678");
		String output = layout.doLayout(evt);

		assertTrue(output.contains("INFO"));
		assertTrue(output.contains("com.example.TestService"));
		assertTrue(output.contains("mobile=130****5678"));
		assertFalse(output.contains("13012345678"));
	}

	@Test
	void shouldNotModifyOutputWhenMessageUnchanged() {
		MessageProcessor noOp = (msg, ctx) -> msg;
		MessageProcessorLayout layout = layoutWith("%level - %msg", List.of(noOp));

		String output = layout.doLayout(event("unchanged"));
		assertTrue(output.contains("unchanged"));
	}

}
