package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.power4j.fist.sde.core.SecureDirection;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureSignatureException;
import com.power4j.fist.sde.core.replay.ReplayContext;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

class PrototypeSecureFeignSupport {

	private static final String KEY_REF = "tenant-a";

	private static final String POLICY_ID = "body-strict-v1";

	private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

	private final JacksonSecureEnvelopeCodec envelopeCodec = new JacksonSecureEnvelopeCodec();

	private final CryptoHandler cryptoHandler = new AesGcmCryptoHandler();

	private final SignatureHandler signatureHandler = new HmacSha256SignatureHandler();

	private final SignatureCanonicalizer canonicalizer = new DefaultSignatureCanonicalizer();

	private final ReplayGuard replayGuard = new InMemoryReplayGuard(Duration.ofMinutes(5));

	private final SecureKey secureKey = new SecureKey(KEY_REF, "AES", KEY);

	static PrototypeSecureFeignSupport defaults() {
		return new PrototypeSecureFeignSupport();
	}

	byte[] encryptAndSign(byte[] plain, SecureScope scope) {
		SecureExchangeContext exchange = exchange(scope, SecureDirection.OUTBOUND);
		String payload = new String(this.cryptoHandler.encrypt(plain, new CryptoContext(exchange, this.secureKey)),
				StandardCharsets.UTF_8);
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope(scope.getValue());
		envelope.setPayload(payload);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce("nonce-" + System.nanoTime());
		envelope.setKeyRef(KEY_REF);
		envelope.setPolicyId(POLICY_ID);
		envelope.setSignature(new String(this.signatureHandler.sign(this.canonicalizer.canonicalize(envelope, exchange),
				new SignContext(exchange, this.secureKey)), StandardCharsets.UTF_8));
		return this.envelopeCodec.encodeToBytes(envelope, SecureEnvelopeContext.defaults());
	}

	byte[] decryptAndVerify(SecureEnvelope envelope, SecureScope expectedScope) {
		if (!expectedScope.getValue().equals(envelope.getScope())) {
			throw new SecureEnvelopeException("unexpected secure envelope scope: " + envelope.getScope());
		}
		SecureExchangeContext exchange = exchange(expectedScope, SecureDirection.INBOUND);
		byte[] signingInput = this.canonicalizer.canonicalize(envelope, exchange);
		boolean verified = this.signatureHandler.verify(signingInput,
				envelope.getSignature().getBytes(StandardCharsets.UTF_8), new SignContext(exchange, this.secureKey));
		if (!verified) {
			throw new SecureSignatureException("secure feign signature verification failed");
		}
		this.replayGuard.checkAndMark(new ReplayContext(exchange, envelope.getKeyRef(), envelope.getPolicyId(),
				envelope.getNonce(), envelope.getTimestamp()));
		return this.cryptoHandler.decrypt(envelope.getPayload().getBytes(StandardCharsets.UTF_8),
				new CryptoContext(exchange, this.secureKey));
	}

	SecureEnvelope decode(byte[] body) {
		return this.envelopeCodec.decode(body, SecureEnvelopeContext.defaults());
	}

	private SecureExchangeContext exchange(SecureScope scope, SecureDirection direction) {
		return new SecureExchangeContext(scope, direction, POLICY_ID, null, KEY_REF, Duration.ofMinutes(5), null);
	}

}
