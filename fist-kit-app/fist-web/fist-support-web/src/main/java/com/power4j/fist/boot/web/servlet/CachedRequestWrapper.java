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

package com.power4j.fist.boot.web.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Original code comes from <a href="https://github.com/pig-mesh/pig">pig-mesh/pig</a>
 *
 * @author Hccake
 * @since 3.1
 */
public class CachedRequestWrapper extends HttpServletRequestWrapper {

	private final byte[] bodyByteArray;

	private final Map<String, String[]> parameterMap;

	public CachedRequestWrapper(HttpServletRequest request) {
		super(request);
		this.bodyByteArray = getByteBody(request);
		this.parameterMap = new HashMap<>(request.getParameterMap());
	}

	@Override
	public BufferedReader getReader() {
		return ObjectUtils.isEmpty(this.bodyByteArray) ? null
				: new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.bodyByteArray);
		return new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// doNoting
			}

			@Override
			public int read() {
				return byteArrayInputStream.read();
			}
		};
	}

	private static byte[] getByteBody(HttpServletRequest request) {
		byte[] body;
		try {
			body = StreamUtils.copyToByteArray(request.getInputStream());
		}
		catch (IOException e) {
			throw new IllegalStateException("Can not read request body", e);
		}
		return body;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return this.parameterMap;
	}

	public void updateParameterMap(Consumer<Map<String, String[]>> consumer) {
		consumer.accept(this.parameterMap);
	}

	public void resetParameterMap(Map<String, String[]> parameterMap) {
		updateParameterMap(m -> {
			m.clear();
			m.putAll(parameterMap);
		});
	}

	@Override
	public String getParameter(String name) {
		String[] values = parameterMap.get(name);
		return (values != null && values.length > 0) ? values[0] : null;
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameterMap.get(name);
	}

}
