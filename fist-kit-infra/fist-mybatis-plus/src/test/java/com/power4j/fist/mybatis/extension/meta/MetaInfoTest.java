/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.mybatis.extension.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
class MetaInfoTest {

	@Test
	void decode() {
		Assertions.assertNull(MetaInfo.decode(null).orElse(null));
		Assertions.assertNull(MetaInfo.decode("").orElse(null));
		Assertions.assertNull(MetaInfo.decode("m:").orElse(null));
		Assertions.assertNull(MetaInfo.decode("m:s").orElse(null));
		Assertions.assertNull(MetaInfo.decode("m:s:").orElse(null));
		Assertions.assertNull(MetaInfo.decode("::").orElse(null));

		MetaInfo metaInfo1 = MetaInfo.decode("m@s@hello", "@").orElse(null);
		Assertions.assertNotNull(metaInfo1);
		Assertions.assertEquals("m", metaInfo1.getMainType());
		Assertions.assertEquals("s", metaInfo1.getSubType());
		Assertions.assertEquals("hello", metaInfo1.getValue());

		MetaInfo metaInfo2 = MetaInfo.decode("m:s:hello").orElse(null);
		Assertions.assertNotNull(metaInfo2);
		Assertions.assertEquals("m", metaInfo2.getMainType());
		Assertions.assertEquals("s", metaInfo2.getSubType());
		Assertions.assertEquals("hello", metaInfo2.getValue());

	}

	@Test
	void encode() {
		Assertions.assertEquals("m:s:v", MetaInfo.of("m", "s", "v").encode());
		Assertions.assertEquals("m@s@v", MetaInfo.of("m", "s", "v").encode("@"));
	}

	@Test
	void shouldThrowExceptionWhenAnyParamIsNull() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of(null, "s", "v"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of("", "s", "v"));

		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of("m", null, "v"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of("m", "", "v"));

		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of("m", "s", null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> MetaInfo.of("m", "s", ""));
	}

}
