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

package com.power4j.fist.logback.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchSpanTest {

	@Test
	void extract_returnsCorrectSubstring() {
		MatchSpan span = new MatchSpan(3, 7);
		assertEquals("3456", span.extract("0123456789"));
	}

	@Test
	void extract_fullString() {
		MatchSpan span = new MatchSpan(0, 5);
		assertEquals("hello", span.extract("hello"));
	}

	@Test
	void extract_singleChar() {
		MatchSpan span = new MatchSpan(2, 3);
		assertEquals("c", span.extract("abcde"));
	}

	@Test
	void extract_outOfBounds_throws() {
		MatchSpan span = new MatchSpan(0, 10);
		assertThrows(StringIndexOutOfBoundsException.class, () -> span.extract("short"));
	}

}
