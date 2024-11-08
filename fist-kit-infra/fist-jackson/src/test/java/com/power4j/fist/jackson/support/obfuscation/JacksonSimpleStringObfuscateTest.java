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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.power4j.fist.jackson.annotation.Obfuscation;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class JacksonSimpleStringObfuscateTest {

	@Data
	public static class Foo {

		@Obfuscation(mode = SimpleStringObfuscate.MODE_ID)
		private String name;

	}

	private static ObjectMapper objectMapper;

	@BeforeAll
	public static void init() {
		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector sis = mapper.getSerializationConfig().getAnnotationIntrospector();
		AnnotationIntrospector is1 = AnnotationIntrospectorPair.pair(sis,
				new ObfuscatedAnnotationIntrospector(StringObfuscateRegistry.INSTANCE));
		mapper.setAnnotationIntrospector(is1);
		objectMapper = mapper;
	}

	@Test
	public void testSerialize() throws IOException {
		Foo foo = new Foo();
		foo.setName("hello world");
		String value = objectMapper.writeValueAsString(foo);
		System.out.println("Serialized: " + value);
		Assertions.assertEquals("{\"name\":\"OBF.XOR_V1$aGRub2slcWh6ZW4=\"}", value);
	}

	@Test
	public void testSerializeNull() throws IOException {
		Foo foo = new Foo();
		foo.setName(null);
		String value = objectMapper.writeValueAsString(foo);
		System.out.println("Serialized: " + value);
		Assertions.assertEquals("{\"name\":null}", value);
	}

	@Test
	public void testSerializeEmpty() throws IOException {
		Foo foo = new Foo();
		foo.setName("");
		String value = objectMapper.writeValueAsString(foo);
		System.out.println("Serialized: " + value);
		Assertions.assertEquals("{\"name\":\"\"}", value);
	}

	@Test
	public void testDeserialize() throws IOException {
		String json = "{\"name\":\"OBF.XOR_V1$aGRub2slcWh6ZW4=\"}";
		Foo foo = objectMapper.readValue(json, Foo.class);
		System.out.println("Deserialized: " + foo.getName());
		Assertions.assertEquals("hello world", foo.getName());
	}

	@Test
	public void testDeserializeNull() throws IOException {
		String json = "{\"name\":null}";
		Foo foo = objectMapper.readValue(json, Foo.class);
		System.out.println("Deserialized: " + foo.getName());
		Assertions.assertNull(foo.getName());
	}

	@Test
	public void testDeserializeEmpty() throws IOException {
		String json = "{\"name\":\"\"}";
		Foo foo = objectMapper.readValue(json, Foo.class);
		System.out.println("Deserialized: " + foo.getName());
		Assertions.assertTrue(foo.getName().isEmpty());
	}

}
