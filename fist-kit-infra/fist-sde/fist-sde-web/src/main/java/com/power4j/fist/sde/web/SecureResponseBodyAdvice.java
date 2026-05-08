package com.power4j.fist.sde.web;

import com.power4j.fist.sde.core.SecurePolicy;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class SecureResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	private final SecureWebExchangeService service;

	public SecureResponseBodyAdvice(SecureWebExchangeService service) {
		this.service = service;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		MediaType requestContentType = request.getHeaders().getContentType();
		if (requestContentType != null && MediaType.MULTIPART_FORM_DATA.isCompatibleWith(requestContentType)) {
			return body;
		}
		if (body == null || returnType.hasMethodAnnotation(ExceptionHandler.class)) {
			return body;
		}
		SecurePolicy policy = this.service.defaultPolicy();
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		String keyRef = attributes == null ? null : (String) attributes
			.getAttribute(SecureWebExchangeService.REQUEST_SECURE_KEY_REF, RequestAttributes.SCOPE_REQUEST);
		if (!this.service.shouldWrite(policy, keyRef != null)) {
			return body;
		}
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		return this.service.writeSecureResponse(body, returnType.getGenericParameterType(), selectedConverterType,
				policy, keyRef);
	}

}
