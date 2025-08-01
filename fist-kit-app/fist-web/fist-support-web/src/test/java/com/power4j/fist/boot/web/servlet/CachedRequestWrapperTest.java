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

package com.power4j.fist.boot.web.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.1
 */
class CachedRequestWrapperTest {

	@Test
	void updateParameterMap() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		final String p1 = "p1";
		final String v1 = "v1";
		final String v2 = "v2";
		final String v3 = "v3";
		request.setParameter(p1, v1);

		CachedRequestWrapper wrapper = new CachedRequestWrapper(request);
		Assertions.assertEquals(v1, wrapper.getParameter(p1));

		wrapper.updateParameterMap(map -> map.put(p1, new String[] { v2 }));
		Assertions.assertEquals(v2, wrapper.getParameter(p1));

		wrapper.updateParameterMap(map -> map.put(p1, new String[] { v3 }));
		Assertions.assertEquals(v3, wrapper.getParameter(p1));
	}

	@Test
	void resetParameterMap() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		final String p1 = "p1";
		final String v1 = "v1";
		request.setParameter(p1, v1);

		CachedRequestWrapper wrapper = new CachedRequestWrapper(request);
		Assertions.assertEquals(v1, wrapper.getParameter(p1));

		final String p2 = "p2";
		final String v2 = "v2";
		java.util.Map<String, String[]> newMap = new java.util.HashMap<>();
		newMap.put(p2, new String[] { v2 });

		wrapper.resetParameterMap(newMap);

		Assertions.assertNull(wrapper.getParameter(p1));
		Assertions.assertEquals(v2, wrapper.getParameter(p2));
	}

	@Test
	void getReader() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		final String body = "test body content";
		request.setContent(body.getBytes(StandardCharsets.UTF_8));

		CachedRequestWrapper wrapper = new CachedRequestWrapper(request);

		// read
		try (BufferedReader reader = wrapper.getReader()) {
			String content = reader.readLine();
			Assertions.assertEquals(body, content);
		}

		// read again
		try (BufferedReader reader = wrapper.getReader()) {
			String content = reader.readLine();
			Assertions.assertEquals(body, content);
		}
	}

}
