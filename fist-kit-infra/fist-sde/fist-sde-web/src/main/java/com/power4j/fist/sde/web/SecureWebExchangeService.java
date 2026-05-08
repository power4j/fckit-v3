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
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureExchangeException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class SecureWebExchangeService {

	static final String REQUEST_SECURE_KEY_REF = SecureWebExchangeService.class.getName() + ".KEY_REF";

	private final SecurePolicyRegistry policyRegistry;

	private final SecureEnvelopeCodec envelopeCodec;

	private final SignatureCanonicalizer canonicalizer;

	private final SecureJsonCodec jsonCodec;

	private final Map<String, CryptoHandler> cryptoHandlers;

	private final Map<String, SignatureHandler> signatureHandlers;

	private final Map<String, SecureKeyResolver> keyResolvers;

	private final Map<String, NonceGenerator> nonceGenerators;

	private final Map<String, ReplayGuard> replayGuards;

	public SecureWebExchangeService(SecurePolicyRegistry policyRegistry, SecureEnvelopeCodec envelopeCodec,
			SignatureCanonicalizer canonicalizer, SecureJsonCodec jsonCodec, Map<String, CryptoHandler> cryptoHandlers,
			Map<String, SignatureHandler> signatureHandlers, Map<String, SecureKeyResolver> keyResolvers,
			Map<String, NonceGenerator> nonceGenerators, Map<String, ReplayGuard> replayGuards) {
		this.policyRegistry = policyRegistry;
		this.envelopeCodec = envelopeCodec;
		this.canonicalizer = canonicalizer;
		this.jsonCodec = jsonCodec;
		this.cryptoHandlers = cryptoHandlers;
		this.signatureHandlers = signatureHandlers;
		this.keyResolvers = keyResolvers;
		this.nonceGenerators = nonceGenerators;
		this.replayGuards = replayGuards;
	}

	SecurePolicy defaultPolicy() {
		return this.policyRegistry.getPolicy(this.policyRegistry.getDefaultPolicyId());
	}

	SecureRequestBody readSecureRequest(byte[] input, SecurePolicy policy) {
		SecureEnvelopeContext envelopeContext = this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName());
		SecureEnvelope envelope = this.envelopeCodec.decode(input, envelopeContext);
		if (!SecureScope.BODY.getValue().equals(envelope.getScope())) {
			throw new SecureEnvelopeException("request envelope scope must be body");
		}
		SecureExchangeContext exchange = new SecureExchangeContext(SecureScope.BODY, SecureDirection.INBOUND,
				policy.getId(), envelope.getAlgorithm(), envelope.getKeyRef(), policy.getTimestampWindow(), null);
		byte[] signingInput = this.canonicalizer.canonicalize(envelope, exchange);
		SecureKey verifyKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.VERIFY);
		boolean verified = signature(policy).verify(signingInput,
				envelope.getSignature().getBytes(StandardCharsets.UTF_8), new SignContext(exchange, verifyKey));
		if (!verified) {
			throw new SecureSignatureException("secure request signature verification failed");
		}
		replay(policy).checkAndMark(new ReplayContext(exchange, envelope.getKeyRef(), policy.getId(),
				envelope.getNonce(), envelope.getTimestamp()));
		SecureKey decryptKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.DECRYPT);
		byte[] plain = crypto(policy).decrypt(envelope.getPayload().getBytes(StandardCharsets.UTF_8),
				new CryptoContext(exchange, decryptKey));
		return new SecureRequestBody(plain, envelope.getKeyRef());
	}

	Object writeSecureResponse(Object body, Type bodyType, Class<?> converterType, SecurePolicy policy, String keyRef) {
		byte[] plain = this.jsonCodec.serialize(body, bodyType);
		String resolvedKeyRef = StringUtils.hasText(keyRef) ? keyRef : "tenant-a";
		SecureExchangeContext exchange = new SecureExchangeContext(SecureScope.RESPONSE_BODY, SecureDirection.OUTBOUND,
				policy.getId(), null, resolvedKeyRef, policy.getTimestampWindow(), null);
		SecureKey encryptKey = key(policy, exchange, resolvedKeyRef, SecureKeyUsage.ENCRYPT);
		String payload = new String(crypto(policy).encrypt(plain, new CryptoContext(exchange, encryptKey)),
				StandardCharsets.UTF_8);
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope(SecureScope.RESPONSE_BODY.getValue());
		envelope.setPayload(payload);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce(nonce(policy).generate(new NonceContext(exchange)));
		envelope.setKeyRef(resolvedKeyRef);
		envelope.setPolicyId(policy.getId());
		SecureKey signKey = key(policy, exchange, resolvedKeyRef, SecureKeyUsage.SIGN);
		envelope.setSignature(new String(signature(policy).sign(this.canonicalizer.canonicalize(envelope, exchange),
				new SignContext(exchange, signKey)), StandardCharsets.UTF_8));
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
