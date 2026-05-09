package com.power4j.fist.sde.client.webclient;

import com.power4j.fist.sde.client.SecureExchangeClientContext;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.net.URI;

/**
 * WebClient 交换过滤器。
 * <p>
 * 过滤器在请求发送前捕获并封装非空、非 multipart 请求体，并在响应返回后解码响应 envelope。
 */
public class SecureWebClientExchangeFilterFunction implements ExchangeFilterFunction {

	private final SecureExchangeOperations operations;

	private final SecureExchangeClientContext context;

	private final ExchangeStrategies strategies;

	private final DataBufferFactory bufferFactory = DefaultDataBufferFactory.sharedInstance;

	public SecureWebClientExchangeFilterFunction(SecureExchangeOperations operations) {
		this(operations, null, ExchangeStrategies.withDefaults());
	}

	public SecureWebClientExchangeFilterFunction(SecureExchangeOperations operations,
			SecureExchangeClientContext context) {
		this(operations, context, ExchangeStrategies.withDefaults());
	}

	public SecureWebClientExchangeFilterFunction(SecureExchangeOperations operations,
			SecureExchangeClientContext context, ExchangeStrategies strategies) {
		this.operations = operations;
		this.context = context;
		this.strategies = strategies == null ? ExchangeStrategies.withDefaults() : strategies;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		if (isMultipart(request.headers())) {
			return next.exchange(request);
		}
		return captureBody(request).flatMap((body) -> {
			if (body.length == 0) {
				return next.exchange(request);
			}
			byte[] envelope = this.operations.encodeRequest(body, this.context);
			ClientRequest secureRequest = ClientRequest.from(request).headers((headers) -> {
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setContentLength(envelope.length);
			}).body(BodyInserters.fromDataBuffers(Flux.just(this.bufferFactory.wrap(envelope)))).build();
			return next.exchange(secureRequest);
		}).flatMap(this::decodeResponse);
	}

	private Mono<byte[]> captureBody(ClientRequest request) {
		BodyCaptureClientHttpRequest outputMessage = new BodyCaptureClientHttpRequest(request.method(), request.url());
		outputMessage.getHeaders().putAll(request.headers());
		return request.writeTo(outputMessage, this.strategies).then(Mono.fromSupplier(outputMessage::toByteArray));
	}

	private Mono<ClientResponse> decodeResponse(ClientResponse response) {
		return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0]).map((envelope) -> {
			if (envelope.length == 0) {
				return response.mutate().body(Flux.<DataBuffer>empty()).build();
			}
			byte[] plain = this.operations.decodeResponse(envelope, this.context);
			return response.mutate().headers((headers) -> {
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setContentLength(plain.length);
			}).body(Flux.just(this.bufferFactory.wrap(plain))).build();
		});
	}

	private boolean isMultipart(HttpHeaders headers) {
		MediaType contentType = headers.getContentType();
		return contentType != null && MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType);
	}

	private static class BodyCaptureClientHttpRequest extends AbstractClientHttpRequest {

		private final HttpMethod method;

		private final URI uri;

		private final ByteArrayOutputStream body = new ByteArrayOutputStream();

		BodyCaptureClientHttpRequest(HttpMethod method, URI uri) {
			this.method = method;
			this.uri = uri;
		}

		@Override
		public HttpMethod getMethod() {
			return this.method;
		}

		@Override
		public URI getURI() {
			return this.uri;
		}

		@Override
		public DataBufferFactory bufferFactory() {
			return DefaultDataBufferFactory.sharedInstance;
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			return Flux.from(body).doOnNext(this::writeBuffer).then();
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			return writeWith(Flux.from(body).flatMap((buffers) -> buffers));
		}

		@Override
		public Mono<Void> setComplete() {
			return Mono.empty();
		}

		@Override
		public <T> T getNativeRequest() {
			return null;
		}

		@Override
		protected void applyHeaders() {
		}

		@Override
		protected void applyCookies() {
		}

		byte[] toByteArray() {
			return this.body.toByteArray();
		}

		private void writeBuffer(DataBuffer buffer) {
			try {
				byte[] bytes = new byte[buffer.readableByteCount()];
				buffer.read(bytes);
				this.body.write(bytes, 0, bytes.length);
			}
			finally {
				DataBufferUtils.release(buffer);
			}
		}

	}

}
