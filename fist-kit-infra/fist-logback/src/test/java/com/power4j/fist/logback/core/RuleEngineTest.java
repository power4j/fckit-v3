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

import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MatchSpan;
import com.power4j.fist.logback.builtin.MaskAllTransformer;
import com.power4j.fist.logback.builtin.RegexDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleEngineTest {

	private static final LogMessageContext CTX = null;

	private RuleEngine engine;

	@BeforeEach
	void setUp() {
		engine = new RuleEngine();
	}

	private ProcessingRule phoneRule() {
		RegexDetector d = new RegexDetector();
		d.init(Map.of("pattern", "1[3-9]\\d{9}"));
		MaskAllTransformer t = new MaskAllTransformer();
		t.init(Map.of());
		return new ProcessingRule("phone", d, t, 0);
	}

	@Test
	void process_singleRule_masksMatch() {
		engine.setRules(List.of(phoneRule()));
		String result = engine.process("call 13812345678 now", CTX);
		assertEquals("call *********** now", result);
	}

	@Test
	void process_noRules_passThrough() {
		engine.setRules(Collections.emptyList());
		assertEquals("hello", engine.process("hello", CTX));
	}

	@Test
	void process_overlappingSpans_greedyDedup() {
		// detector returns overlapping spans [0,5) and [2,7)
		engine.setRules(
				List.of(new ProcessingRule("test", (msg, ctx) -> List.of(new MatchSpan(0, 5), new MatchSpan(2, 7)),
						(val, ctx) -> "*".repeat(val.length()), 0)));
		// greedy: [0,5) accepted, [2,7) overlaps → skipped
		String result = engine.process("0123456789", CTX);
		assertEquals("*****56789", result);
	}

	@Test
	void process_adjacentSpans_bothAccepted() {
		engine.setRules(
				List.of(new ProcessingRule("test", (msg, ctx) -> List.of(new MatchSpan(0, 3), new MatchSpan(3, 6)),
						(val, ctx) -> "*".repeat(val.length()), 0)));
		assertEquals("******6789", engine.process("0123456789", CTX));
	}

	@Test
	void process_detectorThrows_skipsRule() {
		engine.setRules(List.of(new ProcessingRule("bad", (msg, ctx) -> {
			throw new RuntimeException("boom");
		}, (val, ctx) -> val, 0)));
		assertEquals("original", engine.process("original", CTX));
	}

	@Test
	void process_transformerReturnsNull_keepsOriginal() {
		engine.setRules(List
			.of(new ProcessingRule("nullt", (msg, ctx) -> List.of(new MatchSpan(0, 3)), (val, ctx) -> null, 0)));
		assertEquals("abcdef", engine.process("abcdef", CTX));
	}

	@Test
	void process_outOfBoundsSpan_discarded() {
		engine.setRules(
				List.of(new ProcessingRule("oob", (msg, ctx) -> List.of(new MatchSpan(0, 100)), (val, ctx) -> "X", 0)));
		assertEquals("hello", engine.process("hello", CTX));
	}

	@Test
	void process_multipleRules_serialExecution() {
		// rule1: mask digits → ***; rule2: mask *** → [MASKED]
		RegexDetector d1 = new RegexDetector();
		d1.init(Map.of("pattern", "\\d+"));
		MaskAllTransformer t1 = new MaskAllTransformer();
		t1.init(Map.of());
		ProcessingRule rule1 = new ProcessingRule("digits", d1, t1, 0);

		RegexDetector d2 = new RegexDetector();
		d2.init(Map.of("pattern", "\\*+"));
		ProcessingRule rule2 = new ProcessingRule("stars", d2, (val, ctx) -> "[MASKED]", 1);

		engine.setRules(List.of(rule1, rule2));
		assertEquals("abc [MASKED] def", engine.process("abc 123 def", CTX));
	}

}
