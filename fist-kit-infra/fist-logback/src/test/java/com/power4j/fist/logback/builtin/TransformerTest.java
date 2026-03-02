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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransformerTest {

	private static final LogMessageContext CTX = null;

	// --- MaskMiddleTransformer ---

	@Test
	void maskMiddle_defaultParams() {
		MaskMiddleTransformer t = new MaskMiddleTransformer();
		t.init(Map.of());
		// keepFirst=0, keepLast=0 → all masked
		assertEquals("***", t.transform("abc", CTX));
	}

	@Test
	void maskMiddle_keepFirstAndLast() {
		MaskMiddleTransformer t = new MaskMiddleTransformer();
		t.init(Map.of("keepFirst", "3", "keepLast", "4"));
		assertEquals("138****5678", t.transform("13812345678", CTX));
	}

	@Test
	void maskMiddle_customMaskChar() {
		MaskMiddleTransformer t = new MaskMiddleTransformer();
		t.init(Map.of("keepFirst", "1", "keepLast", "1", "maskChar", "#"));
		assertEquals("a###e", t.transform("abcde", CTX));
	}

	// --- MaskAllTransformer ---

	@Test
	void maskAll_replacesAll() {
		MaskAllTransformer t = new MaskAllTransformer();
		t.init(Map.of());
		assertEquals("*****", t.transform("hello", CTX));
	}

	@Test
	void maskAll_customChar() {
		MaskAllTransformer t = new MaskAllTransformer();
		t.init(Map.of("maskChar", "X"));
		assertEquals("XXX", t.transform("abc", CTX));
	}

	// --- ReplaceTransformer ---

	@Test
	void replace_basic() {
		ReplaceTransformer t = new ReplaceTransformer();
		t.init(Map.of("pattern", "\\d", "replacement", "*"));
		assertEquals("abc***", t.transform("abc123", CTX));
	}

	@Test
	void replace_missingPattern_throws() {
		ReplaceTransformer t = new ReplaceTransformer();
		assertThrows(IllegalArgumentException.class, () -> t.init(Map.of("replacement", "*")));
	}

	@Test
	void replace_missingReplacement_throws() {
		ReplaceTransformer t = new ReplaceTransformer();
		assertThrows(IllegalArgumentException.class, () -> t.init(Map.of("pattern", "\\d")));
	}

}
