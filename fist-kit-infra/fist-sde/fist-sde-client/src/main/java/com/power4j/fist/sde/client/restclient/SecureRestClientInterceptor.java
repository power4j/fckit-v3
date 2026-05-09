package com.power4j.fist.sde.client.restclient;

import com.power4j.fist.sde.client.SecureExchangeClientContext;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SecureRestClientInterceptor implements ClientHttpRequestInterceptor {

	private final SecureExchangeOperations operations;

	private final SecureExchangeClientContext context;

	public SecureRestClientInterceptor(SecureExchangeOperations operations) {
		this(operations, null);
	}

	public SecureRestClientInterceptor(SecureExchangeOperations operations, SecureExchangeClientContext context) {
		this.operations = operations;
		this.context = context;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if (isMultipart(request.getHeaders()) || body == null || body.length == 0) {
			return execution.execute(request, body);
		}
		byte[] envelope = this.operations.encodeRequest(body, this.context);
		ClientHttpResponse response = execution.execute(new SecureHttpRequest(request, envelope.length), envelope);
		byte[] responseEnvelope = StreamUtils.copyToByteArray(response.getBody());
		if (responseEnvelope.length == 0) {
			return new SecureClientHttpResponse(response, responseEnvelope);
		}
		byte[] plain = this.operations.decodeResponse(responseEnvelope, this.context);
		return new SecureClientHttpResponse(response, plain);
	}

	private boolean isMultipart(HttpHeaders headers) {
		MediaType contentType = headers.getContentType();
		return contentType != null && MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType);
	}

	private static class SecureHttpRequest extends HttpRequestWrapper {

		private final HttpHeaders headers;

		SecureHttpRequest(HttpRequest request, int contentLength) {
			super(request);
			this.headers = new HttpHeaders();
			this.headers.putAll(request.getHeaders());
			this.headers.setContentType(MediaType.APPLICATION_JSON);
			this.headers.setContentLength(contentLength);
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}

	}

	private static class SecureClientHttpResponse implements ClientHttpResponse {

		private final ClientHttpResponse delegate;

		private final byte[] body;

		private final HttpHeaders headers;

		SecureClientHttpResponse(ClientHttpResponse delegate, byte[] body) {
			this.delegate = delegate;
			this.body = body;
			this.headers = new HttpHeaders();
			this.headers.putAll(delegate.getHeaders());
			this.headers.setContentType(MediaType.APPLICATION_JSON);
			this.headers.setContentLength(body.length);
		}

		@Override
		public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
			return this.delegate.getStatusCode();
		}

		@Override
		public String getStatusText() throws IOException {
			return this.delegate.getStatusText();
		}

		@Override
		public void close() {
			this.delegate.close();
		}

		@Override
		public InputStream getBody() {
			return new ByteArrayInputStream(this.body);
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}

	}

}
