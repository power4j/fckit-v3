package com.power4j.fist.sde.client;

import com.power4j.fist.sde.core.SecureDirection;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureExchangeException;
import com.power4j.fist.sde.core.exception.SecureKeyResolveException;
import com.power4j.fist.sde.core.exception.SecureSignatureException;
import com.power4j.fist.sde.core.key.SecureKeyContext;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceContext;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayContext;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class DefaultSecureExchangeOperations implements SecureExchangeOperations {

	private static final String ENVELOPE_VERSION = "1";

	private final SecurePolicyRegistry policyRegistry;

	private final SecureEnvelopeCodec envelopeCodec;

	private final SignatureCanonicalizer canonicalizer;

	private final Map<String, CryptoHandler> cryptoHandlers;

	private final Map<String, SignatureHandler> signatureHandlers;

	private final Map<String, SecureKeyResolver> keyResolvers;

	private final Map<String, NonceGenerator> nonceGenerators;

	private final Map<String, ReplayGuard> replayGuards;

	private final SecureExchangeClientProperties properties;

	private final SecureExchangeClientLogger logger;

	public DefaultSecureExchangeOperations(SecurePolicyRegistry policyRegistry, SecureEnvelopeCodec envelopeCodec,
			SignatureCanonicalizer canonicalizer, Map<String, CryptoHandler> cryptoHandlers,
			Map<String, SignatureHandler> signatureHandlers, Map<String, SecureKeyResolver> keyResolvers,
			Map<String, NonceGenerator> nonceGenerators, Map<String, ReplayGuard> replayGuards,
			SecureExchangeClientProperties properties, SecureExchangeClientLogger logger) {
		this.policyRegistry = policyRegistry;
		this.envelopeCodec = envelopeCodec;
		this.canonicalizer = canonicalizer;
		this.cryptoHandlers = safeMap(cryptoHandlers);
		this.signatureHandlers = safeMap(signatureHandlers);
		this.keyResolvers = safeMap(keyResolvers);
		this.nonceGenerators = safeMap(nonceGenerators);
		this.replayGuards = safeMap(replayGuards);
		this.properties = properties == null ? new SecureExchangeClientProperties() : properties;
		this.logger = logger == null ? SecureExchangeClientLogger.NONE : logger;
	}

	@Override
	public byte[] encodeRequest(byte[] body, SecureExchangeClientContext context) {
		return encode(body, context, SecureScope.BODY);
	}

	@Override
	public byte[] encodeResponse(byte[] body, SecureExchangeClientContext context) {
		return encode(body, context, SecureScope.RESPONSE_BODY);
	}

	@Override
	public byte[] decodeResponse(byte[] input, SecureExchangeClientContext context) {
		if (this.properties.isLogPayload()) {
			this.logger.responseEnvelope(input, context);
		}
		SecurePolicy policy = policy(context);
		SecureEnvelopeContext envelopeContext = this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName());
		SecureEnvelope envelope = this.envelopeCodec.decode(input, envelopeContext);
		if (!SecureScope.RESPONSE_BODY.getValue().equals(envelope.getScope())) {
			throw new SecureEnvelopeException("response envelope scope must be responseBody");
		}
		validateEnvelope(envelope, policy, "response");
		if (hasText(envelope.getPolicyId()) && !policy.getId().equals(envelope.getPolicyId())) {
			throw new SecureEnvelopeException("response envelope policyId does not match current policy");
		}
		SecureExchangeContext exchange = new SecureExchangeContext(SecureScope.RESPONSE_BODY, SecureDirection.INBOUND,
				policy.getId(), envelope.getAlgorithm(), envelope.getKeyRef(), policy.getTimestampWindow(),
				context == null ? null : context.getRequestContext());
		if (policy.isSignatureEnabled()) {
			SecureKey verifyKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.VERIFY);
			boolean verified = signature(policy).verify(this.canonicalizer.canonicalize(envelope, exchange),
					envelope.getSignature().getBytes(StandardCharsets.UTF_8), new SignContext(exchange, verifyKey));
			if (!verified) {
				throw new SecureSignatureException("secure response signature verification failed");
			}
		}
		replay(policy).checkAndMark(new ReplayContext(exchange, envelope.getKeyRef(), policy.getId(),
				envelope.getNonce(), envelope.getTimestamp()));
		byte[] plain = envelope.getPayload().getBytes(StandardCharsets.UTF_8);
		if (policy.isCryptoEnabled()) {
			SecureKey decryptKey = key(policy, exchange, envelope.getKeyRef(), SecureKeyUsage.DECRYPT);
			plain = crypto(policy).decrypt(plain, new CryptoContext(exchange, decryptKey));
		}
		if (this.properties.isLogPayload()) {
			this.logger.responsePlain(plain, context);
		}
		return plain;
	}

	private byte[] encode(byte[] body, SecureExchangeClientContext context, SecureScope scope) {
		SecurePolicy policy = policy(context);
		String keyRef = keyRef(context);
		if (!hasText(keyRef)) {
			throw new SecureKeyResolveException("client keyRef is required for secure envelope");
		}
		if (this.properties.isLogPayload() && scope == SecureScope.BODY) {
			this.logger.requestPlain(body, context);
		}
		SecureExchangeContext exchange = new SecureExchangeContext(scope, SecureDirection.OUTBOUND, policy.getId(),
				null, keyRef, policy.getTimestampWindow(), context == null ? null : context.getRequestContext());
		String payload = new String(body, StandardCharsets.UTF_8);
		if (policy.isCryptoEnabled()) {
			SecureKey encryptKey = key(policy, exchange, keyRef, SecureKeyUsage.ENCRYPT);
			payload = new String(crypto(policy).encrypt(body, new CryptoContext(exchange, encryptKey)),
					StandardCharsets.UTF_8);
		}
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion(ENVELOPE_VERSION);
		envelope.setScope(scope.getValue());
		envelope.setPayload(payload);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce(nonce(policy).generate(new NonceContext(exchange)));
		envelope.setKeyRef(keyRef);
		envelope.setPolicyId(policy.getId());
		if (policy.isSignatureEnabled()) {
			SecureKey signKey = key(policy, exchange, keyRef, SecureKeyUsage.SIGN);
			envelope.setSignature(new String(signature(policy).sign(this.canonicalizer.canonicalize(envelope, exchange),
					new SignContext(exchange, signKey)), StandardCharsets.UTF_8));
		}
		byte[] encoded = this.envelopeCodec.encodeToBytes(envelope,
				this.policyRegistry.getEnvelopeContext(policy.getEnvelopeName()));
		if (this.properties.isLogPayload() && scope == SecureScope.BODY) {
			this.logger.requestEnvelope(encoded, context);
		}
		return encoded;
	}

	private void validateEnvelope(SecureEnvelope envelope, SecurePolicy policy, String label) {
		required(label, "version", envelope.getVersion());
		if (!ENVELOPE_VERSION.equals(envelope.getVersion())) {
			throw new SecureEnvelopeException("unsupported " + label + " envelope version: " + envelope.getVersion());
		}
		required(label, "payload", envelope.getPayload());
		required(label, "timestamp", envelope.getTimestamp());
		required(label, "nonce", envelope.getNonce());
		required(label, "keyRef", envelope.getKeyRef());
		if (policy.isSignatureEnabled()) {
			required(label, "signature", envelope.getSignature());
		}
	}

	private void required(String label, String field, String value) {
		if (!hasText(value)) {
			throw new SecureEnvelopeException(label + " envelope " + field + " is required");
		}
	}

	private SecurePolicy policy(SecureExchangeClientContext context) {
		String policyId = context == null ? null : context.getPolicyId();
		if (!hasText(policyId)) {
			policyId = this.properties.getDefaultPolicyId();
		}
		if (!hasText(policyId)) {
			policyId = this.policyRegistry.getDefaultPolicyId();
		}
		return this.policyRegistry.getPolicy(policyId);
	}

	private String keyRef(SecureExchangeClientContext context) {
		if (context != null && hasText(context.getKeyRef())) {
			return context.getKeyRef();
		}
		return this.properties.getDefaultKeyRef();
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

	private static boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

	private static <K, V> Map<K, V> safeMap(Map<K, V> source) {
		return source == null ? Collections.<K, V>emptyMap() : source;
	}

}
