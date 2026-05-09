package com.power4j.fist.sde.client.feign;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Feign 响应解码器。
 * <p>
 * 解码器先读取响应 envelope 并还原为明文响应体，再委托原始 Feign 解码器转换为业务对象。
 */
public class SecureFeignDecoder implements Decoder {

	private final Decoder delegate;

	private final SecureExchangeOperations operations;

	public SecureFeignDecoder(Decoder delegate, SecureExchangeOperations operations) {
		this.delegate = delegate;
		this.operations = operations;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		SecureExchange annotation = SecureFeignSupport.findSecureExchange(response.request());
		if (annotation == null || response.body() == null) {
			return this.delegate.decode(response, type);
		}
		byte[] envelope;
		try {
			envelope = Util.toByteArray(response.body().asInputStream());
		}
		finally {
			Util.ensureClosed(response.body());
		}
		byte[] plain = this.operations.decodeResponse(envelope, SecureFeignSupport.clientContext(annotation));
		return this.delegate.decode(response.toBuilder().body(plain).build(), type);
	}

}
