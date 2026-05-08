package com.power4j.fist.sde.web;

import com.power4j.fist.sde.core.SecureDirection;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureExchangeException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

@ControllerAdvice
public class SecureRequestBodyAdvice extends RequestBodyAdviceAdapter {

	private final SecureWebExchangeService service;

	public SecureRequestBodyAdvice(SecureWebExchangeService service) {
		this.service = service;
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		if (!methodParameter.hasParameterAnnotation(RequestBody.class)) {
			return false;
		}
		try {
			return this.service.shouldRead(this.service.policy(methodParameter));
		}
		catch (SecureExchangeException ex) {
			return true;
		}
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		MediaType contentType = inputMessage.getHeaders().getContentType();
		if (contentType != null && MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
			return inputMessage;
		}
		SecurePolicy policy = null;
		try {
			policy = this.service.policy(parameter);
			byte[] input = StreamUtils.copyToByteArray(inputMessage.getBody());
			if (input.length == 0) {
				if (policy.getRequestBodyMode() == SecureInputMode.REQUIRED) {
					throw new SecureEnvelopeException("secure request body is required");
				}
				return new SecureHttpInputMessage(input, inputMessage.getHeaders());
			}
			if (policy.getRequestBodyMode() == SecureInputMode.PLAIN) {
				if (this.service.isSecureRequestEnvelope(input, policy)) {
					throw new SecureEnvelopeException("secure request body is not allowed");
				}
				return new SecureHttpInputMessage(input, inputMessage.getHeaders());
			}
			if (policy.getRequestBodyMode() == SecureInputMode.OPTIONAL
					&& !this.service.isSecureRequestEnvelope(input, policy)) {
				return new SecureHttpInputMessage(input, inputMessage.getHeaders());
			}
			SecureWebExchangeService.SecureRequestBody secureBody = this.service.readSecureRequest(input, policy);
			org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder
				.getRequestAttributes();
			if (attributes != null) {
				attributes.setAttribute(SecureWebExchangeService.REQUEST_SECURE_KEY_REF, secureBody.getKeyRef(),
						org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
			}
			return new SecureHttpInputMessage(secureBody.getBody(), inputMessage.getHeaders());
		}
		catch (SecureExchangeException ex) {
			throw this.service.translate(ex, policy, SecureScope.BODY, SecureDirection.INBOUND, null);
		}
	}

}
