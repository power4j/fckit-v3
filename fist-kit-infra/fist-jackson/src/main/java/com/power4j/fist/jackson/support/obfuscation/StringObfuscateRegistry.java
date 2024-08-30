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

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
public class StringObfuscateRegistry {

	private final static Map<Class<?>, Supplier<StringObfuscate>> OBFUSCATE_MAP = new ConcurrentHashMap<>();

	static {
		registerObfuscate(SimpleStringObfuscate.class, SimpleStringObfuscate::ofDefault);
	}

	public static Optional<StringObfuscate> getObfuscateInstance(Class<? extends StringObfuscate> obfuscate) {
		return Optional.ofNullable(OBFUSCATE_MAP.get(obfuscate)).map(Supplier::get);
	}

	public static void registerObfuscate(Class<? extends StringObfuscate> obfuscate,
			Supplier<StringObfuscate> supplier) {
		OBFUSCATE_MAP.put(obfuscate, supplier);
	}

}
