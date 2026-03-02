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
import ch.qos.logback.core.read.ListAppender;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;
import com.power4j.fist.logback.core.RuleEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集成测试：通过 SensitiveConverter 验证完整脱敏链路。
 */
class SensitiveConverterIntegrationTest {

	private LoggerContext context;

	private Logger logger;

	private ListAppender<ILoggingEvent> appender;

	private SensitiveConverter converter;

	@BeforeEach
	void setUp() {
		context = new LoggerContext();
		logger = context.getLogger("test");
		logger.setLevel(Level.DEBUG);

		appender = new ListAppender<>();
		appender.setContext(context);
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void tearDown() {
		if (converter != null) {
			converter.stop();
		}
		context.stop();
	}

	private SensitiveConverter buildConverter(String configFile) {
		SensitiveConverter c = new SensitiveConverter() {
			@Override
			protected java.util.List<MessageProcessor> loadProcessors() {
				RuleEngine engine = new RuleEngine();
				return java.util.List.of(engine);
			}
		};
		c.setContext(context);
		if (configFile != null) {
			c.setOptionList(java.util.List.of("configFile=" + configFile));
		}
		c.start();
		return c;
	}

	@Test
	void phoneMasking_singleRule() {
		converter = buildConverter("test-masking-phone.properties");
		logger.info("call 13812345678 now");
		ILoggingEvent event = appender.list.get(0);
		String masked = converter.convert(event);
		assertEquals("call 138****5678 now", masked);
	}

	@Test
	void multiRule_phoneAndEmail() {
		converter = buildConverter("test-masking-multi.properties");
		logger.info("phone 13812345678 email user@example.com");
		ILoggingEvent event = appender.list.get(0);
		String masked = converter.convert(event);
		assertTrue(masked.contains("138****5678"), "phone should be masked");
		assertTrue(!masked.contains("user@example.com"), "email should be masked");
	}

	@Test
	void noConfigFile_passThrough() {
		converter = buildConverter("nonexistent-config.properties");
		logger.info("sensitive 13812345678 data");
		ILoggingEvent event = appender.list.get(0);
		String result = converter.convert(event);
		assertEquals("sensitive 13812345678 data", result);
	}

	@Test
	void stop_triggersDestroy() {
		converter = buildConverter("test-masking-phone.properties");
		converter.stop();
		// stop() should not throw
		converter = null;
	}

	@Test
	void noProcessor_passThrough() {
		SensitiveConverter c = new SensitiveConverter() {
			@Override
			protected java.util.List<MessageProcessor> loadProcessors() {
				return java.util.List.of();
			}
		};
		c.setContext(context);
		c.start();
		logger.info("raw message");
		ILoggingEvent event = appender.list.get(0);
		assertEquals("raw message", c.convert(event));
		c.stop();
	}

	@Test
	void customProcessorAndRuleEngine_orderRespected() {
		// custom processor prepends "[P1]", RuleEngine masks phone
		MessageProcessor custom = new MessageProcessor() {
			@Override
			public String name() {
				return "custom";
			}

			@Override
			public int order() {
				return -1;
			}

			@Override
			public String process(String message, LogMessageContext ctx) {
				return "[P1] " + message;
			}
		};

		SensitiveConverter c = new SensitiveConverter() {
			@Override
			protected List<MessageProcessor> loadProcessors() {
				RuleEngine engine = new RuleEngine();
				return List.of(custom, engine);
			}
		};
		c.setContext(context);
		c.setOptionList(java.util.List.of("configFile=test-masking-phone.properties"));
		c.start();

		logger.info("call 13812345678");
		ILoggingEvent event = appender.list.get(0);
		String result = c.convert(event);
		assertTrue(result.startsWith("[P1] "), "custom processor should run first");
		assertTrue(result.contains("138****5678"), "phone should be masked");
		c.stop();
	}

}
