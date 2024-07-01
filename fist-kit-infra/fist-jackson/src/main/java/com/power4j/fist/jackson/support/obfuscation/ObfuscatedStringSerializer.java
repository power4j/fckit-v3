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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.power4j.fist.jackson.annotation.Obfuscation;

import java.io.IOException;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 * @see StringObfuscateRegistry
 */
public class ObfuscatedStringSerializer extends StdSerializer<String> implements ContextualSerializer {

	private final StringObfuscate obfuscate;

	public ObfuscatedStringSerializer() {
		super(String.class);
		this.obfuscate = SimpleStringObfuscate.ofDefault();
	}

	public ObfuscatedStringSerializer(StringObfuscate obfuscate) {
		super(String.class);
		this.obfuscate = obfuscate;
	}

	@Override
	public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
		if (value == null) {
			jsonGenerator.writeNull();
		}
		else if (value.isEmpty()) {
			jsonGenerator.writeString(value);
		}
		else {
			String obfuscated;
			try {
				obfuscated = obfuscate.algorithm() + "." + obfuscate.obfuscate(value);
			}
			catch (Exception e) {
				throw new JsonGenerationException(e, jsonGenerator);
			}

			jsonGenerator.writeString(obfuscated);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
			throws JsonMappingException {
		if (property == null) {
			return prov.findNullValueSerializer(property);
		}
		Obfuscation annotation = property.getAnnotation(Obfuscation.class);
		if (annotation == null) {
			return prov.findContentValueSerializer(property.getType(), property);
		}
		Class<? extends StringObfuscate> obfuscate = annotation.processor();
		Optional<StringObfuscate> processor = StringObfuscateRegistry.getObfuscateInstance(obfuscate);
		if (processor.isEmpty()) {
			throw new IllegalStateException(
					String.format("Obfuscation processor not registered: %s", annotation.processor().getName()));
		}
		return new ObfuscatedStringSerializer(processor.get());
	}

}
