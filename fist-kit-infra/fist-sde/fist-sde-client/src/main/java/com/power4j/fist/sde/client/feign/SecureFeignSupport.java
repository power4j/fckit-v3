package com.power4j.fist.sde.client.feign;

import com.power4j.fist.sde.client.SecureExchangeClientContext;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Feign 客户端适配的内部工具。
 */
final class SecureFeignSupport {

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String APPLICATION_JSON = "application/json";

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	private SecureFeignSupport() {
	}

	static SecureExchange findSecureExchange(RequestTemplate template) {
		if (template == null) {
			return null;
		}
		MethodMetadata metadata = template.methodMetadata();
		return metadata == null ? null : findSecureExchange(metadata.method());
	}

	static SecureExchange findSecureExchange(Request request) {
		return request == null ? null : findSecureExchange(request.requestTemplate());
	}

	static SecureExchange findSecureExchange(Method method) {
		if (method == null) {
			return null;
		}
		SecureExchange annotation = method.getAnnotation(SecureExchange.class);
		return annotation == null ? method.getDeclaringClass().getAnnotation(SecureExchange.class) : annotation;
	}

	static SecureExchangeClientContext clientContext(SecureExchange annotation) {
		String policyId = annotation == null || !hasText(annotation.value()) ? null : annotation.value();
		return new SecureExchangeClientContext(policyId, null);
	}

	static void useJsonBody(RequestTemplate template, byte[] body) {
		template.removeHeader(CONTENT_TYPE);
		template.header(CONTENT_TYPE, APPLICATION_JSON);
		template.body(body, java.nio.charset.StandardCharsets.UTF_8);
	}

	static boolean isMultipart(RequestTemplate template) {
		if (template == null) {
			return false;
		}
		Collection<String> values = template.headers().get(CONTENT_TYPE);
		if (values == null) {
			values = template.headers().get(CONTENT_TYPE.toLowerCase(java.util.Locale.ENGLISH));
		}
		if (values == null) {
			return false;
		}
		for (String value : values) {
			if (value != null && value.toLowerCase(java.util.Locale.ENGLISH).contains(MULTIPART_FORM_DATA)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

}
