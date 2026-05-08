package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureScope;
import feign.Response;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;

class PrototypeSecureFeignDecoder implements Decoder {

	private final Decoder delegate;

	private final PrototypeSecureFeignSupport support;

	PrototypeSecureFeignDecoder(Decoder delegate, PrototypeSecureFeignSupport support) {
		this.delegate = delegate;
		this.support = support;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		SecureEnvelope envelope = this.support.decode(response.body().asInputStream().readAllBytes());
		byte[] plain = this.support.decryptAndVerify(envelope, SecureScope.RESPONSE_BODY);
		return this.delegate.decode(PrototypeFeignResponses.replaceBody(response, plain), type);
	}

}
