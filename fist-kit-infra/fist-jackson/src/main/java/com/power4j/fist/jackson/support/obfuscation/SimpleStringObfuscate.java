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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class SimpleStringObfuscate implements StringObfuscate {

	public final static String MODE_ID = "OBF.XOR_V1";

	private final Base64.Encoder ENCODER = Base64.getEncoder();

	private final Base64.Decoder DECODER = Base64.getDecoder();

	private final byte[] key;

	public static SimpleStringObfuscate ofDefault() {
		return new SimpleStringObfuscate(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
	}

	public SimpleStringObfuscate(byte[] key) {
		this.key = key;
	}

	@Override
	public String modeId() {
		return MODE_ID;
	}

	@Override
	public String obfuscate(String value) {
		if (value.isEmpty()) {
			return value;
		}
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (byte) (bytes[i] ^ key[i % key.length]);
		}
		return ENCODER.encodeToString(result);
	}

	@Override
	public String deobfuscate(String value) throws Exception {
		if (value.isEmpty()) {
			return value;
		}
		byte[] bytes;
		try {
			bytes = DECODER.decode(value);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalStateException("Base64 decode error", e);
		}
		byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (byte) (bytes[i] ^ key[i % key.length]);
		}
		return new String(result, StandardCharsets.UTF_8);
	}

}
