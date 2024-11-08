/*
 * Copyright 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.jackson.support.obfuscation;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class NoopStringObfuscate implements StringObfuscate {

	public final static String MODEL_ID = "noop";

	@Override
	public String modeId() {
		return MODEL_ID;
	}

	@Override
	public String obfuscate(String value) throws Exception {
		return value;
	}

	@Override
	public String deobfuscate(String value) throws Exception {
		return value;
	}

}
