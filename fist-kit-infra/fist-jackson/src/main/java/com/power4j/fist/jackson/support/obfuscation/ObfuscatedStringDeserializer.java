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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.power4j.fist.jackson.annotation.Obfuscation;

import java.io.IOException;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 * @see StringObfuscateRegistry
 */
public class ObfuscatedStringDeserializer extends StdDeserializer<String> implements ContextualDeserializer {

	private final StringObfuscate obfuscate;

	private final ObfuscateProcessorProvider resolver;

	public ObfuscatedStringDeserializer() {
		super(String.class);
		this.obfuscate = SimpleStringObfuscate.ofDefault();
		this.resolver = StringObfuscateRegistry.INSTANCE;
	}

	protected ObfuscatedStringDeserializer(StringObfuscate obfuscate, ObfuscateProcessorProvider resolver) {
		super(String.class);
		this.obfuscate = obfuscate;
		this.resolver = resolver;
	}

	@Override
	public String deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JacksonException {
		String value = p.getValueAsString();
		if (value == null) {
			return null;
		}
		else if (!value.startsWith(obfuscate.algorithm() + ".")) {
			return value;
		}
		value = value.substring(obfuscate.algorithm().length() + 1);
		try {
			return obfuscate.deobfuscate(value);
		}
		catch (Exception e) {
			throw new JsonParseException(p, e.getMessage(), e);
		}
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property)
			throws JsonMappingException {
		if (property == null) {
			return StringDeserializer.instance;
		}
		Obfuscation annotation = property.getAnnotation(Obfuscation.class);
		if (annotation == null) {
			return StringDeserializer.instance;
		}
		Class<? extends StringObfuscate> obfuscate = annotation.processor();
		Optional<StringObfuscate> processor = resolver.getInstance(obfuscate);
		if (processor.isEmpty()) {
			throw new IllegalStateException(
					String.format("Obfuscation processor not registered: %s", annotation.processor().getName()));
		}
		return new ObfuscatedStringDeserializer(processor.get(), resolver);
	}

}
