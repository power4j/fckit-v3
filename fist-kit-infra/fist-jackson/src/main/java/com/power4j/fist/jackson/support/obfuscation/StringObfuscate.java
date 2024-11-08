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

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface StringObfuscate {

	String HEAD = "$";

	/**
	 * The mode id, {@code $} is <b> reserved</b>.
	 * @return String
	 */
	String modeId();

	/**
	 * Obfuscate string value
	 * @param value the value to encode,must not be null,may be empty
	 * @return Obfuscated string
	 */
	String obfuscate(String value) throws Exception;

	/**
	 * Deobfuscate
	 * @param value the value to deobfuscate,must not be null,may be empty
	 * @return original string
	 */
	String deobfuscate(String value) throws Exception;

}
