/*
 * Copyright (c) 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
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

package com.power4j.fist.jackson.support.obfuscation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
class SimpleStringObfuscateTest {

	@Test
	void handleEmptyString() throws Exception {
		StringObfuscate obfuscate = SimpleStringObfuscate.ofDefault();
		String original = "";
		String obfuscated = obfuscate.obfuscate(original);
		assertEquals("", obfuscated);
		String deobfuscated = obfuscate.deobfuscate(obfuscated);
		assertEquals(original, deobfuscated);
	}

	@Test
	void obfuscate() throws Exception {
		StringObfuscate obfuscate = SimpleStringObfuscate.ofDefault();
		String original = "hello world";
		String obfuscated = obfuscate.obfuscate(original);
		String deobfuscated = obfuscate.deobfuscate(obfuscated);
		assertEquals(original, deobfuscated);

		original = "你好！世界";
		obfuscated = obfuscate.obfuscate(original);
		deobfuscated = obfuscate.deobfuscate(obfuscated);
		assertEquals(original, deobfuscated);
	}

}
