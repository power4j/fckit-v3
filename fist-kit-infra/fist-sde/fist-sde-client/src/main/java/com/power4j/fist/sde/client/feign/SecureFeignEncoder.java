package com.power4j.fist.sde.client.feign;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;

public class SecureFeignEncoder implements Encoder {

	private final Encoder delegate;

	private final SecureExchangeOperations operations;

	public SecureFeignEncoder(Encoder delegate, SecureExchangeOperations operations) {
		this.delegate = delegate;
		this.operations = operations;
	}

	@Override
	public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
		this.delegate.encode(object, bodyType, template);
		SecureExchange annotation = SecureFeignSupport.findSecureExchange(template);
		if (annotation == null || SecureFeignSupport.isMultipart(template)) {
			return;
		}
		byte[] body = template.body();
		if (body == null) {
			return;
		}
		byte[] envelope = this.operations.encodeRequest(body, SecureFeignSupport.clientContext(annotation));
		SecureFeignSupport.useJsonBody(template, envelope);
	}

}
