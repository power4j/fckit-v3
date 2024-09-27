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

import com.power4j.tile.crypto.utils.Sm3Util;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;

import java.nio.charset.StandardCharsets;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/3/7
 * @since 1.0
 */
public class Sm3PasswordEncoder extends AbstractPasswordEncoder {

	public final static String ID = "sm3";

	@Override
	protected byte[] encode(CharSequence rawPassword, byte[] salt) {
		byte[] input = rawPassword.toString().getBytes(StandardCharsets.UTF_8);
		return Sm3Util.hash(input, salt);
	}

}
