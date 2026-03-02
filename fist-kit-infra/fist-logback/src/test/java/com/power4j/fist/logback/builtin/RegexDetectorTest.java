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

package com.power4j.fist.logback.builtin;

import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MatchSpan;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexDetectorTest {

	private static final LogMessageContext CTX = null;

	@Test
	void detect_returnsMatchingSpans() {
		RegexDetector d = new RegexDetector();
		d.init(Map.of("pattern", "\\d+"));
		List<MatchSpan> spans = d.detect("abc 123 def 456", CTX);
		assertEquals(2, spans.size());
		assertEquals(new MatchSpan(4, 7), spans.get(0));
		assertEquals(new MatchSpan(12, 15), spans.get(1));
	}

	@Test
	void detect_noMatch_returnsEmpty() {
		RegexDetector d = new RegexDetector();
		d.init(Map.of("pattern", "\\d+"));
		List<MatchSpan> spans = d.detect("no digits here", CTX);
		assertTrue(spans.isEmpty());
	}

	@Test
	void init_missingPattern_throws() {
		RegexDetector d = new RegexDetector();
		assertThrows(IllegalArgumentException.class, () -> d.init(Map.of()));
	}

	@Test
	void init_invalidRegex_throws() {
		RegexDetector d = new RegexDetector();
		assertThrows(Exception.class, () -> d.init(Map.of("pattern", "[")));
	}

}
