package com.power4j.fist.sde.client.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.client.DefaultSecureExchangeOperations;
import com.power4j.fist.sde.client.SecureExchangeClientLogger;
import com.power4j.fist.sde.client.SecureExchangeClientProperties;
import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecurePolicy;
import com.power4j.fist.sde.core.SecurePolicyRegistry;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.SimpleSecurePolicyRegistry;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.crypto.CryptoHandler;
import com.power4j.fist.sde.core.json.JacksonSecureJsonCodec;
import com.power4j.fist.sde.core.json.SecureJsonCodec;
import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.nonce.NonceGenerator;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureCanonicalizer;
import com.power4j.fist.sde.core.signature.SignatureHandler;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import com.power4j.fist.sde.web.SecureRequestBodyAdvice;
import com.power4j.fist.sde.web.SecureResponseBodyAdvice;
import com.power4j.fist.sde.web.SecureWebExchangeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest(classes = SecureRestClientTest.TestApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecureRestClientTest {

	private static final String POLICY_ID = "body-strict-v1";

	private static final String KEY_REF = "tenant-a";

	@LocalServerPort
	private int port;

	@org.springframework.beans.factory.annotation.Autowired
	private SecureExchangeOperations operations;

	@org.springframework.beans.factory.annotation.Autowired
	private RecordingOrderController controller;

	@Test
	void shouldExchangeSecureEnvelopeWithRestClient() {
		RestClient client = RestClient.builder()
			.baseUrl("http://localhost:" + this.port)
			.requestInterceptor(new SecureRestClientInterceptor(this.operations))
			.build();

		OrderResponse response = client.post()
			.uri("/orders")
			.body(new OrderRequest("TEST-RESTCLIENT-1", new BigDecimal("9.00")))
			.retrieve()
			.body(OrderResponse.class);

		Assertions.assertThat(response.getOrderNo()).isEqualTo("TEST-RESTCLIENT-1");
		Assertions.assertThat(response.getStatus()).isEqualTo("ACCEPTED");
		Assertions.assertThat(this.controller.getLastRequest().getOrderNo()).isEqualTo("TEST-RESTCLIENT-1");
		Assertions.assertThat(this.controller.getLastRequest().getAmount()).isEqualByComparingTo("9.00");
	}

	@RestController
	static class RecordingOrderController {

		private OrderRequest lastRequest;

		@PostMapping("/orders")
		@SecureExchange(POLICY_ID)
		OrderResponse create(@RequestBody OrderRequest request) {
			this.lastRequest = request;
			return new OrderResponse(request.getOrderNo(), "ACCEPTED");
		}

		OrderRequest getLastRequest() {
			return this.lastRequest;
		}

	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestApplication {

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		SecureEnvelopeCodec secureEnvelopeCodec(ObjectMapper objectMapper) {
			return new JacksonSecureEnvelopeCodec(objectMapper);
		}

		@Bean
		SecureJsonCodec secureJsonCodec(ObjectMapper objectMapper) {
			return new JacksonSecureJsonCodec(objectMapper);
		}

		@Bean
		SignatureCanonicalizer signatureCanonicalizer() {
			return new DefaultSignatureCanonicalizer();
		}

		@Bean
		SecurePolicyRegistry securePolicyRegistry() {
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

		@Bean
		CryptoHandler aesGcmCryptoHandler() {
			return new AesGcmCryptoHandler();
		}

		@Bean
		SignatureHandler hmacSha256SignatureHandler() {
			return new HmacSha256SignatureHandler();
		}

		@Bean
		SecureKeyResolver staticSecureKeyResolver() {
			return StaticSecureKeyResolver.symmetric(KEY_REF, "0123456789abcdef".getBytes(StandardCharsets.UTF_8));
		}

		@Bean
		NonceGenerator secureRandomNonceGenerator() {
			return new SecureRandomNonceGenerator();
		}

		@Bean
		ReplayGuard replayGuard() {
			return new InMemoryReplayGuard(Duration.ofMinutes(5));
		}

		@Bean
		SecureExchangeClientProperties secureExchangeClientProperties() {
			SecureExchangeClientProperties properties = new SecureExchangeClientProperties();
			properties.setDefaultPolicyId(POLICY_ID);
			properties.setDefaultKeyRef(KEY_REF);
			return properties;
		}

		@Bean
		SecureExchangeOperations secureExchangeOperations(SecurePolicyRegistry policyRegistry,
				SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer,
				Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
				Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
				Map<String, ReplayGuard> replayGuards, SecureExchangeClientProperties properties) {
			return new DefaultSecureExchangeOperations(policyRegistry, envelopeCodec, canonicalizer, cryptoHandlers,
					signatureHandlers, keyResolvers, nonceGenerators, replayGuards, properties,
					SecureExchangeClientLogger.NONE);
		}

		@Bean
		SecureWebExchangeService secureWebExchangeService(SecurePolicyRegistry policyRegistry,
				SecureEnvelopeCodec envelopeCodec, SignatureCanonicalizer canonicalizer, SecureJsonCodec jsonCodec,
				Map<String, CryptoHandler> cryptoHandlers, Map<String, SignatureHandler> signatureHandlers,
				Map<String, SecureKeyResolver> keyResolvers, Map<String, NonceGenerator> nonceGenerators,
				Map<String, ReplayGuard> replayGuards,
				ObjectProvider<com.power4j.fist.sde.core.exception.SecureExchangeExceptionTranslator> translator) {
			return new SecureWebExchangeService(policyRegistry, envelopeCodec, canonicalizer, jsonCodec, cryptoHandlers,
					signatureHandlers, keyResolvers, nonceGenerators, replayGuards, translator.getIfAvailable());
		}

		@Bean
		SecureRequestBodyAdvice secureRequestBodyAdvice(SecureWebExchangeService service) {
			return new SecureRequestBodyAdvice(service);
		}

		@Bean
		SecureResponseBodyAdvice secureResponseBodyAdvice(SecureWebExchangeService service) {
			return new SecureResponseBodyAdvice(service);
		}

		@Bean
		RecordingOrderController recordingOrderController() {
			return new RecordingOrderController();
		}

	}

	static class OrderRequest {

		private String orderNo;

		private BigDecimal amount;

		OrderRequest() {
		}

		OrderRequest(String orderNo, BigDecimal amount) {
			this.orderNo = orderNo;
			this.amount = amount;
		}

		public String getOrderNo() {
			return this.orderNo;
		}

		public void setOrderNo(String orderNo) {
			this.orderNo = orderNo;
		}

		public BigDecimal getAmount() {
			return this.amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

	}

	static class OrderResponse {

		private String orderNo;

		private String status;

		OrderResponse() {
		}

		OrderResponse(String orderNo, String status) {
			this.orderNo = orderNo;
			this.status = status;
		}

		public String getOrderNo() {
			return this.orderNo;
		}

		public void setOrderNo(String orderNo) {
			this.orderNo = orderNo;
		}

		public String getStatus() {
			return this.status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}

}
