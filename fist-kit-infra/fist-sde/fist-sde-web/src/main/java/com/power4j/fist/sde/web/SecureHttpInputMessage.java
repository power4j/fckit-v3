package com.power4j.fist.sde.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将解密后的 JSON 字节重新交给 Spring MVC 消息转换器读取的输入消息。
 */
class SecureHttpInputMessage implements HttpInputMessage {

	private final byte[] body;

	private final HttpHeaders headers;

	SecureHttpInputMessage(byte[] body, HttpHeaders originalHeaders) {
		this.body = body;
		this.headers = new HttpHeaders();
		this.headers.putAll(originalHeaders);
		this.headers.setContentType(MediaType.APPLICATION_JSON);
		this.headers.setContentLength(body.length);
	}

	@Override
	public InputStream getBody() throws IOException {
		return new ByteArrayInputStream(this.body);
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}

}
