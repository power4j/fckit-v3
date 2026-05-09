package com.power4j.fist.sde.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.SimpleSecurePolicyRegistry;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class DefaultSecureExchangeOperationsTest {

	private static final String POLICY_ID = "body-strict-v1";

	private static final String KEY_REF = "tenant-a";

	private final SecureEnvelopeCodec envelopeCodec = new JacksonSecureEnvelopeCodec(new ObjectMapper());

	@Test
	void shouldEncodeRequestAndDecodeResponseEnvelope() {
		RecordingLogger logger = new RecordingLogger();
		SecureExchangeClientProperties properties = new SecureExchangeClientProperties();
		properties.setLogPayload(true);
		SecureExchangeOperations operations = operations(properties, logger);
		SecureExchangeClientContext context = new SecureExchangeClientContext(POLICY_ID, KEY_REF);

		byte[] requestEnvelope = operations.encodeRequest(bytes("{\"orderNo\":\"A-1\"}"), context);

		SecureEnvelope request = decode(requestEnvelope);
		Assertions.assertThat(request.getVersion()).isEqualTo("1");
		Assertions.assertThat(request.getScope()).isEqualTo("body");
		Assertions.assertThat(request.getPayload()).isNotEqualTo("{\"orderNo\":\"A-1\"}");
		Assertions.assertThat(request.getSignature()).isNotBlank();
		Assertions.assertThat(request.getKeyRef()).isEqualTo(KEY_REF);
		Assertions.assertThat(request.getPolicyId()).isEqualTo(POLICY_ID);
		Assertions.assertThat(logger.events).contains("request-plain", "request-envelope");

		byte[] responseEnvelope = operations.encodeResponse(bytes("{\"status\":\"ACCEPTED\"}"), context);
		byte[] responseBody = operations.decodeResponse(responseEnvelope, context);

		Assertions.assertThat(new String(responseBody, StandardCharsets.UTF_8)).isEqualTo("{\"status\":\"ACCEPTED\"}");
		Assertions.assertThat(logger.events).contains("response-envelope", "response-plain");
	}

	@Test
	void shouldNotLogPayloadByDefault() {
		RecordingLogger logger = new RecordingLogger();
		SecureExchangeOperations operations = operations(new SecureExchangeClientProperties(), logger);

		operations.encodeRequest(bytes("{\"orderNo\":\"A-2\"}"), new SecureExchangeClientContext(POLICY_ID, KEY_REF));

		Assertions.assertThat(logger.events).isEmpty();
	}

	private SecureExchangeOperations operations(SecureExchangeClientProperties properties,
			SecureExchangeClientLogger logger) {
		Map<String, CryptoHandler> cryptoHandlers = new LinkedHashMap<>();
		cryptoHandlers.put("aesGcmCryptoHandler", new AesGcmCryptoHandler());
		Map<String, SignatureHandler> signatureHandlers = new LinkedHashMap<>();
		signatureHandlers.put("hmacSha256SignatureHandler", new HmacSha256SignatureHandler());
		Map<String, SecureKeyResolver> keyResolvers = new LinkedHashMap<>();
		keyResolvers.put("staticSecureKeyResolver",
				StaticSecureKeyResolver.symmetric(KEY_REF, "0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
		Map<String, NonceGenerator> nonceGenerators = new LinkedHashMap<>();
		nonceGenerators.put("secureRandomNonceGenerator", new SecureRandomNonceGenerator());
		Map<String, ReplayGuard> replayGuards = new LinkedHashMap<>();
		replayGuards.put("replayGuard", new InMemoryReplayGuard(Duration.ofMinutes(5)));
		return DefaultSecureExchangeOperations.builder()
			.policyRegistry(policyRegistry())
			.envelopeCodec(this.envelopeCodec)
			.canonicalizer(new DefaultSignatureCanonicalizer())
			.cryptoHandlers(cryptoHandlers)
			.signatureHandlers(signatureHandlers)
			.keyResolvers(keyResolvers)
			.nonceGenerators(nonceGenerators)
			.replayGuards(replayGuards)
			.properties(properties)
			.logger(logger)
			.build();
	}

	private SecurePolicyRegistry policyRegistry() {
		SecurePolicy policy = new SecurePolicy();
		policy.setId(POLICY_ID);
		policy.setRequestBodyMode(SecureInputMode.REQUIRED);
		policy.setResponseBodyMode(SecureResponseMode.FOLLOW_REQUEST);
		Map<String, SecurePolicy> policies = new LinkedHashMap<>();
		policies.put(POLICY_ID, policy);
		Map<String, SecureEnvelopeContext> envelopes = new LinkedHashMap<>();
		envelopes.put("default", SecureEnvelopeContext.defaults());
		return new SimpleSecurePolicyRegistry(POLICY_ID, policies, envelopes);
	}

	private SecureEnvelope decode(byte[] bytes) {
		return this.envelopeCodec.decode(bytes, SecureEnvelopeContext.defaults());
	}

	private byte[] bytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	private static class RecordingLogger implements SecureExchangeClientLogger {

		private final java.util.List<String> events = new java.util.ArrayList<>();

		@Override
		public void requestPlain(byte[] body, SecureExchangeClientContext context) {
			this.events.add("request-plain");
		}

		@Override
		public void requestEnvelope(byte[] envelope, SecureExchangeClientContext context) {
			this.events.add("request-envelope");
		}

		@Override
		public void responseEnvelope(byte[] envelope, SecureExchangeClientContext context) {
			this.events.add("response-envelope");
		}

		@Override
		public void responsePlain(byte[] body, SecureExchangeClientContext context) {
			this.events.add("response-plain");
		}

	}

}
