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

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.power4j.fist.jackson.annotation.Obfuscation;

import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ObfuscatedAnnotationIntrospector extends NopAnnotationIntrospector {

	private final StringObfuscate defaultProcessor = SimpleStringObfuscate.ofDefault();

	@Override
	public Object findSerializer(Annotated am) {
		Obfuscation obfuscation = am.getAnnotation(Obfuscation.class);
		if (Objects.nonNull(obfuscation)) {
			return new ObfuscatedStringSerializer(defaultProcessor);
		}
		return super.findSerializer(am);
	}

	@Override
	public Object findDeserializer(Annotated am) {
		Obfuscation obfuscation = am.getAnnotation(Obfuscation.class);
		if (Objects.nonNull(obfuscation)) {
			return new ObfuscatedStringDeserializer(defaultProcessor);
		}
		return super.findContentDeserializer(am);
	}

}
