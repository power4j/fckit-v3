/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.support.spring.web.servlet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/1/11
 * @since 1.0
 */
@UtilityClass
public class HttpServletResponseUtil {

	private final static ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

	public static void writeJson(HttpServletResponse response, Object payload, HttpStatus status) throws IOException {
		writeJson(response, DEFAULT_OBJECT_MAPPER, payload, status);
	}

	/**
	 * 获取当请求对应的响应
	 * @see RequestContextListener
	 * @return
	 */
	public Optional<HttpServletResponse> getCurrentRequest() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
			.map(x -> (ServletRequestAttributes) x)
			.map(ServletRequestAttributes::getResponse);
	}

	public static void writeJson(HttpServletResponse response, ObjectMapper objectMapper, Object payload,
			HttpStatus status) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().println(objectMapper.writeValueAsString(payload));
		response.getWriter().flush();
	}

	public static void setNoCache(HttpServletResponse response) {
		response.setHeader(HttpHeaders.PRAGMA, "No-cache");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.setDateHeader(HttpHeaders.EXPIRES, 0);
	}

	public void setAttachmentHeaders(HttpServletResponse response, String fileName) {
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", fileName));
	}

	public void writeAttachment(HttpServletResponse response, byte[] data, String fileName, boolean closeOutputStream)
			throws IOException {
		setAttachmentHeaders(response, fileName);
		final OutputStream output = response.getOutputStream();
		try {
			IOUtils.write(data, output);
		}
		finally {
			if (closeOutputStream) {
				IOUtils.close(output);
			}
		}
	}

}
