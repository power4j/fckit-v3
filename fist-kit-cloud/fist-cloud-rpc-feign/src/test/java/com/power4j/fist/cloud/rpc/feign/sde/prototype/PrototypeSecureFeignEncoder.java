package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.power4j.fist.sde.core.SecureScope;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

class PrototypeSecureFeignEncoder implements Encoder {

	private final Encoder delegate;

	private final PrototypeSecureFeignSupport support;

	PrototypeSecureFeignEncoder(Encoder delegate, PrototypeSecureFeignSupport support) {
		this.delegate = delegate;
		this.support = support;
	}

	@Override
	public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
		RequestTemplate delegateTemplate = new RequestTemplate();
		this.delegate.encode(object, bodyType, delegateTemplate);
		for (Map.Entry<String, Collection<String>> entry : delegateTemplate.headers().entrySet()) {
			template.header(entry.getKey(), entry.getValue());
		}
		template.body(this.support.encryptAndSign(delegateTemplate.body(), SecureScope.BODY), StandardCharsets.UTF_8);
	}

}
