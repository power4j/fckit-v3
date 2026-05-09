package com.power4j.fist.sde.web;

import com.power4j.fist.sde.core.SecureDirection;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.annotation.SecureBody;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureExchangeException;
import com.power4j.fist.sde.core.exception.SecureExchangeExceptionTranslator;
import com.power4j.fist.sde.core.exception.SecureKeyResolveException;
import com.power4j.fist.sde.core.exception.SecureSignatureException;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import com.power4j.fist.sde.core.key.SecureKeyContext;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceContext;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayContext;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class SecureWebExchangeService {

	static final String REQUEST_SECURE_KEY_REF = SecureWebExchangeService.class.getName() + ".KEY_REF";

	private static final String ENVELOPE_VERSION = "1";

	private final SecurePolicyRegistry policyRegistry;

	private final SecureEnvelopeCodec envelopeCodec;

	private final SignatureCanonicalizer canonicalizer;

	private final SecureJsonCodec jsonCodec;

	private final Map<String, CryptoHandler> cryptoHandlers;

	private final Map<String, SignatureHandler> signatureHandlers;

	private final Map<String, SecureKeyResolver> keyResolvers;

	private final Map<String, NonceGenerator> nonceGenerators;

	private final Map<String, ReplayGuard> replayGuards;

	private final SecureExchangeExceptionTranslator exceptionTranslator;

	@Builder
	public SecureWebExchangeService(SecurePolicyRegistry policyRegistry, SecureEnvelopeCodec envelopeCodec,
			SignatureCanonicalizer canonicalizer, SecureJsonCodec jsonCodec, Map<String, CryptoHandler> cryptoHandlers,
			Map<String, SignatureHandler> signatureHandlers, Map<String, SecureKeyResolver> keyResolvers,
			Map<String, NonceGenerator> nonceGenerators, Map<String, ReplayGuard> replayGuards,
			@Nullable SecureExchangeExceptionTranslator exceptionTranslator) {
		this.policyRegistry = policyRegistry;
		this.envelopeCodec = envelopeCodec;
		this.canonicalizer = canonicalizer;
		this.jsonCodec = jsonCodec;
		this.cryptoHandlers = cryptoHandlers;
		this.signatureHandlers = signatureHandlers;
		this.keyResolvers = keyResolvers;
		this.nonceGenerators = nonceGenerators;
		this.replayGuards = replayGuards;
		this.exceptionTranslator = exceptionTranslator;
	}

	SecurePolicy defaultPolicy() {
		return this.policyRegistry.getPolicy(this.policyRegistry.getDefaultPolicyId());
	}

	SecurePolicy policy(MethodParameter parameter) {
		Method method = parameter.getMethod();
		Class<?> containingClass = parameter.getContainingClass();
		if (method != null) {
			SecureBody methodBody = AnnotatedElementUtils.findMergedAnnotation(method, SecureBody.class);
			SecureExchange methodExchange = AnnotatedElementUtils.findMergedAnnotation(method, SecureExchange.class);
			if (methodBody != null && methodExchange != null) {
				throw new SecureEnvelopeException("conflicting SDE annotations on method: " + method);
			}
			if (methodBody != null) {
				return policy(methodBody.value(), methodBody.request(), methodBody.response());
			}
			if (methodExchange != null) {
				return policy(methodExchange.value(), methodExchange.requestBody(), methodExchange.responseBody());
			}
		}
		SecureBody classBody = AnnotatedElementUtils.findMergedAnnotation(containingClass, SecureBody.class);
		SecureExchange classExchange = AnnotatedElementUtils.findMergedAnnotation(containingClass,
				SecureExchange.class);
		if (classBody != null && classExchange != null) {
			throw new SecureEnvelopeException("conflicting SDE annotations on class: " + containingClass.getName());
		}
		if (classBody != null) {
			return policy(classBody.value(), classBody.request(), classBody.response());
		}
		if (classExchange != null) {
			return policy(classExchange.value(), classExchange.requestBody(), classExchange.responseBody());
		}
		return defaultPolicy();
	}

	private SecurePolicy policy(String policyId, SecureInputMode requestMode, SecureResponseMode responseMode) {
		SecurePolicy source = StringUtils.hasText(policyId) ? this.policyRegistry.getPolicy(policyId) : defaultPolicy();
		if (requestMode == SecureInputMode.INHERIT && responseMode == SecureResponseMode.INHERIT) {
			return source;
		}
		SecurePolicy policy = copy(source);
		if (requestMode != SecureInputMode.INHERIT) {
			policy.setRequestBodyMode(requestMode);
		}
		if (responseMode != SecureResponseMode.INHERIT) {
			policy.setResponseBodyMode(responseMode);
		}
		return policy;
	}

	private SecurePolicy copy(SecurePolicy source) {
		SecurePolicy policy = new SecurePolicy();
		policy.setId(source.getId());
		policy.setRequestBodyMode(source.getRequestBodyMode());
		policy.setResponseBodyMode(source.getResponseBodyMode());
		policy.setCryptoEnabled(source.isCryptoEnabled());
		policy.setSignatureEnabled(source.isSignatureEnabled());
		policy.setCryptoHandlerName(source.getCryptoHandlerName());
		policy.setSignatureHandlerName(source.getSignatureHandlerName());
		policy.setKeyResolverName(source.getKeyResolverName());
		policy.setNonceGeneratorName(source.getNonceGeneratorName());
		policy.setReplayGuardName(source.getReplayGuardName());
		policy.setEnvelopeName(source.getEnvelopeName());
		policy.setTimestampWindow(source.getTimestampWindow());
		return policy;
	}

	SecureRequestBody readSecureRequest(byte[] input, SecurePolicy policy) {
		SecureEnvelopeContext envelopeContext = this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName());
		SecureEnvelope envelope = this.envelopeCodec.decode(input, envelopeContext);
		if (!SecureScope.BODY.getValue().equals(envelope.getScope())) {
			throw new SecureEnvelopeException("request envelope scope must be body");
		}
		validateRequestEnvelope(envelope, policy);
		if (StringUtils.hasText(envelope.getPolicyId()) && !policy.getId().equals(envelope.getPolicyId())) {
			throw new SecureEnvelopeException("request envelope policyId does not match current policy");
		}
		SecureExchangeContext exchange = SecureExchangeContext.builder()
			.scope(SecureScope.BODY)
			.direction(SecureDirection.INBOUND)
			.policyId(policy.getId())
			.algorithm(envelope.getAlgorithm())
			.keyRef(envelope.getKeyRef())
			.timestampWindow(policy.getTimestampWindow())
			.build();
		if (policy.isSignatureEnabled()) {
			byte[] signingInput = this.canonicalizer.canonicalize(envelope, exchange);
			SecureKey verifyKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.VERIFY);
			boolean verified = signature(policy).verify(signingInput,
					envelope.getSignature().getBytes(StandardCharsets.UTF_8), new SignContext(exchange, verifyKey));
			if (!verified) {
				throw new SecureSignatureException("secure request signature verification failed");
			}
		}
		replay(policy).checkAndMark(ReplayContext.builder()
			.exchangeContext(exchange)
			.keyRef(envelope.getKeyRef())
			.policyId(policy.getId())
			.nonce(envelope.getNonce())
			.timestamp(envelope.getTimestamp())
			.build());
		byte[] plain = envelope.getPayload().getBytes(StandardCharsets.UTF_8);
		if (policy.isCryptoEnabled()) {
			SecureKey decryptKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.DECRYPT);
			plain = crypto(policy).decrypt(plain, new CryptoContext(exchange, decryptKey));
		}
		return new SecureRequestBody(plain, envelope.getKeyRef());
	}

	boolean isSecureRequestEnvelope(byte[] input, SecurePolicy policy) {
		try {
			SecureEnvelopeContext envelopeContext = this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName());
			SecureEnvelope envelope = this.envelopeCodec.decode(input, envelopeContext);
			return SecureScope.BODY.getValue().equals(envelope.getScope()) && StringUtils.hasText(envelope.getVersion())
					&& StringUtils.hasText(envelope.getPayload());
		}
		catch (SecureEnvelopeException ex) {
			return false;
		}
	}

	private void validateRequestEnvelope(SecureEnvelope envelope, SecurePolicy policy) {
		required("version", envelope.getVersion());
		if (!ENVELOPE_VERSION.equals(envelope.getVersion())) {
			throw new SecureEnvelopeException("unsupported request envelope version: " + envelope.getVersion());
		}
		required("payload", envelope.getPayload());
		required("timestamp", envelope.getTimestamp());
		required("nonce", envelope.getNonce());
		required("keyRef", envelope.getKeyRef());
		if (policy.isSignatureEnabled()) {
			required("signature", envelope.getSignature());
		}
	}

	private void required(String field, String value) {
		if (!StringUtils.hasText(value)) {
			throw new SecureEnvelopeException("request envelope " + field + " is required");
		}
	}

	Object writeSecureResponse(Object body, Type bodyType, Class<?> converterType, SecurePolicy policy, String keyRef) {
		byte[] plain = this.jsonCodec.serialize(body, bodyType);
		if (!StringUtils.hasText(keyRef)) {
			throw new SecureKeyResolveException("response keyRef is required for secure response");
		}
		String resolvedKeyRef = keyRef;
		SecureExchangeContext exchange = SecureExchangeContext.builder()
			.scope(SecureScope.RESPONSE_BODY)
			.direction(SecureDirection.OUTBOUND)
			.policyId(policy.getId())
			.keyRef(resolvedKeyRef)
			.timestampWindow(policy.getTimestampWindow())
			.build();
		String payload = new String(plain, StandardCharsets.UTF_8);
		if (policy.isCryptoEnabled()) {
			SecureKey encryptKey = key(policy, exchange, resolvedKeyRef, SecureKeyUsage.ENCRYPT);
			payload = new String(crypto(policy).encrypt(plain, new CryptoContext(exchange, encryptKey)),
					StandardCharsets.UTF_8);
		}
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion(ENVELOPE_VERSION);
		envelope.setScope(SecureScope.RESPONSE_BODY.getValue());
		envelope.setPayload(payload);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce(nonce(policy).generate(new NonceContext(exchange)));
		envelope.setKeyRef(resolvedKeyRef);
		envelope.setPolicyId(policy.getId());
		if (policy.isSignatureEnabled()) {
			SecureKey signKey = key(policy, exchange, resolvedKeyRef, SecureKeyUsage.SIGN);
			envelope.setSignature(new String(signature(policy).sign(this.canonicalizer.canonicalize(envelope, exchange),
					new SignContext(exchange, signKey)), StandardCharsets.UTF_8));
		}
		SecureEnvelopeContext envelopeContext = this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName())
			.withSelectedConverterType(converterType);
		if (converterType != null && StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return new String(this.envelopeCodec.encodeToBytes(envelope, envelopeContext), StandardCharsets.UTF_8);
		}
		if (converterType != null && ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return this.envelopeCodec.encodeToBytes(envelope, envelopeContext);
		}
		return this.envelopeCodec.encodeToBody(envelope, envelopeContext);
	}

	boolean shouldRead(SecurePolicy policy) {
		return policy.getRequestBodyMode() == SecureInputMode.REQUIRED
				|| policy.getRequestBodyMode() == SecureInputMode.OPTIONAL
				|| policy.getRequestBodyMode() == SecureInputMode.PLAIN;
	}

	boolean shouldWrite(SecurePolicy policy, boolean requestWasSecure) {
		return policy.getResponseBodyMode() == SecureResponseMode.ENABLED
				|| (policy.getResponseBodyMode() == SecureResponseMode.FOLLOW_REQUEST && requestWasSecure);
	}

	RuntimeException translate(SecureExchangeException exception, @Nullable SecurePolicy policy, SecureScope scope,
			SecureDirection direction, @Nullable String keyRef) {
		if (this.exceptionTranslator == null) {
			return exception;
		}
		SecureExchangeContext context = SecureExchangeContext.builder()
			.scope(scope)
			.direction(direction)
			.policyId(policy == null ? null : policy.getId())
			.keyRef(keyRef)
			.timestampWindow(policy == null ? null : policy.getTimestampWindow())
			.build();
		RuntimeException translated = this.exceptionTranslator.translate(exception, context);
		return translated == null ? exception : translated;
	}

	HttpHeaders secureHeaders(HttpHeaders original) {
		HttpHeaders headers = new HttpHeaders();
		headers.putAll(original);
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
		return headers;
	}

	private CryptoHandler crypto(SecurePolicy policy) {
		return required(this.cryptoHandlers, policy.getCryptoHandlerName(), "CryptoHandler");
	}

	private SignatureHandler signature(SecurePolicy policy) {
		return required(this.signatureHandlers, policy.getSignatureHandlerName(), "SignatureHandler");
	}

	private NonceGenerator nonce(SecurePolicy policy) {
		return required(this.nonceGenerators, policy.getNonceGeneratorName(), "NonceGenerator");
	}

	private ReplayGuard replay(SecurePolicy policy) {
		return required(this.replayGuards, policy.getReplayGuardName(), "ReplayGuard");
	}

	private SecureKey key(SecurePolicy policy, SecureExchangeContext exchange, String keyRef, SecureKeyUsage usage) {
		return required(this.keyResolvers, policy.getKeyResolverName(), "SecureKeyResolver")
			.resolve(new SecureKeyContext(exchange, keyRef, usage));
	}

	private static <T> T required(Map<String, T> beans, String name, String type) {
		T bean = beans.get(name);
		if (bean == null) {
			throw new SecureExchangeException(type + " bean not found: " + name);
		}
		return bean;
	}

	static class SecureRequestBody {

		private final byte[] body;

		private final String keyRef;

		SecureRequestBody(byte[] body, String keyRef) {
			this.body = body;
			this.keyRef = keyRef;
		}

		byte[] getBody() {
			return this.body;
		}

		String getKeyRef() {
			return this.keyRef;
		}

	}

}
