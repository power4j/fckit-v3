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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
class ProcessorChainTest {

	private LoggerContext loggerContext;

	@BeforeEach
	void setUp() {
		loggerContext = new LoggerContext();
		loggerContext.setName("test");
	}

	private LogMessageContext ctx() {
		return new LogMessageContext(Level.INFO, "test.Logger", "main", System.currentTimeMillis(), null, () -> null);
	}

	@Test
	void shouldReturnNullMessageUnchanged() {
		ProcessorChain chain = new ProcessorChain(List.of());
		chain.setContext(loggerContext);
		chain.start();
		assertNull(chain.execute(null, ctx()));
	}

	@Test
	void shouldReturnEmptyMessageUnchanged() {
		ProcessorChain chain = new ProcessorChain(List.of());
		chain.setContext(loggerContext);
		chain.start();
		assertEquals("", chain.execute("", ctx()));
	}

	@Test
	void shouldExecuteProcessorsInAscendingOrder() {
		List<String> order = new ArrayList<>();
		MessageProcessor p1 = new MessageProcessor() {
			@Override
			public int order() {
				return 10;
			}

			@Override
			public String process(String msg, LogMessageContext context) {
				order.add("p1");
				return msg + "-p1";
			}
		};
		MessageProcessor p2 = new MessageProcessor() {
			@Override
			public int order() {
				return 5;
			}

			@Override
			public String process(String msg, LogMessageContext context) {
				order.add("p2");
				return msg + "-p2";
			}
		};
		ProcessorChain chain = new ProcessorChain(Arrays.asList(p1, p2));
		chain.setContext(loggerContext);
		chain.start();
		String result = chain.execute("msg", ctx());
		assertEquals(Arrays.asList("p2", "p1"), order);
		assertEquals("msg-p2-p1", result);
	}

	@Test
	void shouldSkipProcessorWhenSupportsReturnsFalse() {
		MessageProcessor skipped = new MessageProcessor() {
			@Override
			public boolean supports(LogMessageContext context) {
				return false;
			}

			@Override
			public String process(String msg, LogMessageContext context) {
				return "SHOULD_NOT_APPEAR";
			}
		};
		ProcessorChain chain = new ProcessorChain(List.of(skipped));
		chain.setContext(loggerContext);
		chain.start();
		assertEquals("original", chain.execute("original", ctx()));
	}

	@Test
	void shouldSkipExceptionProcessorAndContinueChain() {
		MessageProcessor throwing = new MessageProcessor() {
			@Override
			public int order() {
				return 0;
			}

			@Override
			public String process(String msg, LogMessageContext context) {
				throw new RuntimeException("test error");
			}
		};
		MessageProcessor appender = new MessageProcessor() {
			@Override
			public int order() {
				return 1;
			}

			@Override
			public String process(String msg, LogMessageContext context) {
				return msg + "-ok";
			}
		};
		ProcessorChain chain = new ProcessorChain(Arrays.asList(throwing, appender));
		chain.setContext(loggerContext);
		chain.start();
		assertEquals("msg-ok", chain.execute("msg", ctx()));
	}

}
