package com.power4j.fist3.examples.sde.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureDirection;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
class ExampleFeignEnvelopeSupport {

	private static final Duration TIMESTAMP_WINDOW = Duration.ofMinutes(5);

	private final ObjectMapper objectMapper;

	private final SecureEnvelopeCodec envelopeCodec;

	private final CryptoHandler cryptoHandler;

	private final SignatureHandler signatureHandler;

	private final SignatureCanonicalizer canonicalizer;

	private final SecureKeyResolver keyResolver;

	private final NonceGenerator nonceGenerator;

	private final ReplayGuard replayGuard;

	ExampleFeignEnvelopeSupport(ObjectMapper objectMapper, SecureEnvelopeCodec envelopeCodec,
			CryptoHandler cryptoHandler, SignatureHandler signatureHandler, SignatureCanonicalizer canonicalizer,
			SecureKeyResolver keyResolver, NonceGenerator nonceGenerator, ReplayGuard replayGuard) {
		this.objectMapper = objectMapper;
		this.envelopeCodec = envelopeCodec;
		this.cryptoHandler = cryptoHandler;
		this.signatureHandler = signatureHandler;
		this.canonicalizer = canonicalizer;
		this.keyResolver = keyResolver;
		this.nonceGenerator = nonceGenerator;
		this.replayGuard = replayGuard;
	}

	String encryptAndSign(byte[] plain, SecureScope scope, String policyId) {
		SecureExchangeContext exchange = exchange(scope, SecureDirection.OUTBOUND, policyId);
		SecureKey encryptKey = key(exchange, SecureKeyUsage.ENCRYPT);
		String payload = new String(this.cryptoHandler.encrypt(plain, new CryptoContext(exchange, encryptKey)),
				StandardCharsets.UTF_8);
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope(scope.getValue());
		envelope.setPayload(payload);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce(this.nonceGenerator.generate(new NonceContext(exchange)));
		envelope.setKeyRef(SdeFeignExampleConfiguration.KEY_REF);
		envelope.setPolicyId(policyId);
		SecureKey signKey = key(exchange, SecureKeyUsage.SIGN);
		envelope.setSignature(new String(this.signatureHandler.sign(this.canonicalizer.canonicalize(envelope, exchange),
				new SignContext(exchange, signKey)), StandardCharsets.UTF_8));
		return new String(this.envelopeCodec.encodeToBytes(envelope, SecureEnvelopeContext.defaults()),
				StandardCharsets.UTF_8);
	}

	byte[] decryptAndVerify(String envelopeJson, SecureScope expectedScope, String policyId) {
		SecureEnvelope envelope = this.envelopeCodec.decode(envelopeJson.getBytes(StandardCharsets.UTF_8),
				SecureEnvelopeContext.defaults());
		if (!expectedScope.getValue().equals(envelope.getScope())) {
			throw new SecureEnvelopeException("unexpected Feign envelope scope: " + envelope.getScope());
		}
		SecureExchangeContext exchange = exchange(expectedScope, SecureDirection.INBOUND, policyId);
		SecureKey verifyKey = key(exchange, SecureKeyUsage.VERIFY);
		boolean verified = this.signatureHandler.verify(this.canonicalizer.canonicalize(envelope, exchange),
				envelope.getSignature().getBytes(StandardCharsets.UTF_8), new SignContext(exchange, verifyKey));
		if (!verified) {
			throw new SecureSignatureException("Feign response envelope signature verification failed");
		}
		this.replayGuard.checkAndMark(new ReplayContext(exchange, envelope.getKeyRef(), envelope.getPolicyId(),
				envelope.getNonce(), envelope.getTimestamp()));
		SecureKey decryptKey = key(exchange, SecureKeyUsage.DECRYPT);
		return this.cryptoHandler.decrypt(envelope.getPayload().getBytes(StandardCharsets.UTF_8),
				new CryptoContext(exchange, decryptKey));
	}

	String prettyJson(String json) throws IOException {
		return this.objectMapper.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this.objectMapper.readTree(json));
	}

	private SecureKey key(SecureExchangeContext exchange, SecureKeyUsage usage) {
		return this.keyResolver
			.resolve(new SecureKeyContext(exchange, SdeFeignExampleConfiguration.KEY_REF, usage));
	}

	private SecureExchangeContext exchange(SecureScope scope, SecureDirection direction, String policyId) {
		return new SecureExchangeContext(scope, direction, policyId, null, SdeFeignExampleConfiguration.KEY_REF,
				TIMESTAMP_WINDOW, null);
	}

}
