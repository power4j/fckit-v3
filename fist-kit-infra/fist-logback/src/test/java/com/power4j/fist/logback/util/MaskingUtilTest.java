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

package com.power4j.fist.logback.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
class MaskingUtilTest {

	@Test
	void shouldMaskHeadWithDefaultChar() {
		assertEquals("***12345678", MaskingUtil.maskHead("13012345678", 3));
	}

	@Test
	void shouldMaskHeadWithCustomChar() {
		assertEquals("##012345678", MaskingUtil.maskHead("13012345678", 2, '#'));
	}

	@Test
	void shouldMaskAllWhenMaskHeadLengthExceedsInputLength() {
		assertEquals("*****", MaskingUtil.maskHead("abcde", 10));
	}

	@Test
	void shouldMaskTailWithDefaultChar() {
		assertEquals("13012345***", MaskingUtil.maskTail("13012345678", 3));
	}

	@Test
	void shouldMaskTailWithCustomChar() {
		assertEquals("130123456##", MaskingUtil.maskTail("13012345678", 2, '#'));
	}

	@Test
	void shouldMaskAllWhenMaskTailLengthExceedsInputLength() {
		assertEquals("*****", MaskingUtil.maskTail("abcde", 10));
	}

	@Test
	void shouldMaskHeadKeepLastWithDefaultChar() {
		assertEquals("*******5678", MaskingUtil.maskHeadKeepLast("13012345678", 4));
	}

	@Test
	void shouldMaskHeadKeepLastWithCustomChar() {
		assertEquals("########678", MaskingUtil.maskHeadKeepLast("13012345678", 3, '#'));
	}

	@Test
	void shouldMaskAllWhenHeadKeepLastVisibleTailIsZero() {
		assertEquals("***********", MaskingUtil.maskHeadKeepLast("13012345678", 0));
	}

	@Test
	void shouldReturnOriginWhenHeadKeepLastVisibleTailExceedsInputLength() {
		assertEquals("abc", MaskingUtil.maskHeadKeepLast("abc", 10));
	}

	@Test
	void shouldThrowWhenHeadKeepLastVisibleTailIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskHeadKeepLast("abc", -1));
	}

	@Test
	void shouldMaskTailKeepFirstWithDefaultChar() {
		assertEquals("130********", MaskingUtil.maskTailKeepFirst("13012345678", 3));
	}

	@Test
	void shouldMaskTailKeepFirstWithCustomChar() {
		assertEquals("1301#######", MaskingUtil.maskTailKeepFirst("13012345678", 4, '#'));
	}

	@Test
	void shouldMaskAllWhenTailKeepFirstVisibleHeadIsZero() {
		assertEquals("***********", MaskingUtil.maskTailKeepFirst("13012345678", 0));
	}

	@Test
	void shouldReturnOriginWhenTailKeepFirstVisibleHeadExceedsInputLength() {
		assertEquals("abc", MaskingUtil.maskTailKeepFirst("abc", 10));
	}

	@Test
	void shouldThrowWhenTailKeepFirstVisibleHeadIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskTailKeepFirst("abc", -1));
	}

	@Test
	void shouldMaskMiddleKeepCenterWithDefaultChar() {
		assertEquals("****EFG****", MaskingUtil.maskMiddleKeepCenter("ABCDEFGHIJK", 3));
	}

	@Test
	void shouldMaskMiddleKeepCenterWithCustomChar() {
		assertEquals("###DEFG###", MaskingUtil.maskMiddleKeepCenter("ABCDEFGHIJ", 4, '#'));
	}

	@Test
	void shouldMaskAllWhenMiddleKeepCenterVisibleIsZero() {
		assertEquals("*****", MaskingUtil.maskMiddleKeepCenter("abcde", 0));
	}

	@Test
	void shouldReturnOriginWhenMiddleKeepCenterVisibleExceedsInputLength() {
		assertEquals("abc", MaskingUtil.maskMiddleKeepCenter("abc", 10));
	}

	@Test
	void shouldThrowWhenMiddleKeepCenterVisibleIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskMiddleKeepCenter("abc", -1));
	}

	@Test
	void shouldMaskMiddleWithDefaultChar() {
		assertEquals("130****5678", MaskingUtil.maskMiddle("13012345678", 3, 4));
	}

	@Test
	void shouldMaskMiddleWithCustomChar() {
		assertEquals("13#######78", MaskingUtil.maskMiddle("13012345678", 2, 2, '#'));
	}

	@Test
	void shouldReturnOriginWhenKeepAllInMiddleMask() {
		assertEquals("abcde", MaskingUtil.maskMiddle("abcde", 2, 3));
	}

	@Test
	void shouldReturnOriginWhenInputLengthLessThanKeepHead() {
		assertEquals("abc", MaskingUtil.maskMiddle("abc", 5, 0));
	}

	@Test
	void shouldReturnOriginWhenInputLengthLessThanKeepTail() {
		assertEquals("abc", MaskingUtil.maskMiddle("abc", 0, 5));
	}

	@Test
	void shouldThrowWhenMaskHeadLengthIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskHead("abc", -1));
	}

	@Test
	void shouldThrowWhenMaskTailLengthIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskTail("abc", -1));
	}

	@Test
	void shouldThrowWhenKeepHeadOrTailIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskMiddle("abc", -1, 1));
		assertThrows(IllegalArgumentException.class, () -> MaskingUtil.maskMiddle("abc", 1, -1));
	}

}
