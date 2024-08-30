/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.security.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/3/7
 * @since 1.0
 */
class Sm3PasswordEncoderTest {

	@Test
	void encode() {
		final String raw = "root";
		Sm3PasswordEncoder encoder = new Sm3PasswordEncoder();
		String encoded = encoder.encode(raw);
		boolean matched = encoder.matches(raw, encoded);
		Assertions.assertTrue(matched);
	}

	@Test
	void testSalted() {
		final String raw = "root";
		Sm3PasswordEncoder encoder = new Sm3PasswordEncoder();
		String enc_1 = encoder.encode(raw);
		String enc_2 = encoder.encode(raw);
		Assertions.assertNotEquals(enc_1, enc_2);
	}

}
