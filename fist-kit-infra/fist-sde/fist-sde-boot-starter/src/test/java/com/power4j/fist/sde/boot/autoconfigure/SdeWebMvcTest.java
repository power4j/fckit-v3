package com.power4j.fist.sde.boot.autoconfigure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
import com.power4j.fist.sde.extra.crypto.AesGcmCryptoHandler;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.nonce.SecureRandomNonceGenerator;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import com.power4j.fist.sde.extra.signature.HmacSha256SignatureHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
		"fist.sde.web.default-policy-id=body-strict-v1", "fist.sde.policies.body-strict-v1.request-body-mode=required",
		"fist.sde.policies.body-strict-v1.response-body-mode=enabled" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcTest {

	private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

	private final MockMvc mockMvc;

	private final ObjectMapper objectMapper;

	@Autowired
	SdeWebMvcTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@Test
	void shouldDecryptRequestAndEncryptPojoResponse() throws Exception {
		String response = this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content(envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode node = this.objectMapper.readTree(response);
		assertThat(node.get("scope").asText()).isEqualTo("responseBody");
		assertThat(node.get("data").asText()).isNotBlank();
		assertThat(node.get("sign").asText()).isNotBlank();
	}

	@Test
	void shouldHandleStringByteArrayNullAndExceptionResponses() throws Exception {
		this.mockMvc
			.perform(post("/sde/string").contentType(MediaType.APPLICATION_JSON)
				.content(envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect((result) -> assertThat(result.getResponse().getContentAsString()).contains("\"scope\""));

		this.mockMvc
			.perform(
					post("/sde/bytes").contentType(MediaType.APPLICATION_JSON).content(envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andExpect((result) -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());

		this.mockMvc
			.perform(post("/sde/null").contentType(MediaType.APPLICATION_JSON).content(envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andExpect(content().string(""));

		this.mockMvc
			.perform(
					post("/sde/error").contentType(MediaType.APPLICATION_JSON).content(envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isBadRequest())
			.andExpect(content().string("bad request"));
	}

	@Test
	void shouldRejectPlainRequiredBodyAndExcludeMultipart() throws Exception {
		assertThatThrownBy(() -> this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"fist\"}")))
			.hasRootCauseInstanceOf(com.power4j.fist.sde.core.exception.SecureEnvelopeException.class);

		this.mockMvc.perform(multipart("/sde/upload").file("file", "plain".getBytes(StandardCharsets.UTF_8)))
			.andExpect(status().isOk())
			.andExpect(content().string("upload"));
	}

	@Test
	void shouldRejectEmptyRequiredBody() {
		assertThatThrownBy(
				() -> this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content("")))
			.hasRootCauseInstanceOf(com.power4j.fist.sde.core.exception.SecureEnvelopeException.class)
			.hasMessageContaining("secure request body is required");
	}

	@Test
	void shouldRejectRequiredEnvelopeWithoutSignature() {
		assertThatThrownBy(() -> this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
			.content(missingSignatureEnvelope("{\"name\":\"fist\"}"))))
			.hasRootCauseInstanceOf(com.power4j.fist.sde.core.exception.SecureEnvelopeException.class)
			.hasMessageContaining("request envelope signature is required");
	}

	@Test
	void shouldRejectRequiredEnvelopeWithWrongSignature() throws Exception {
		assertThatThrownBy(() -> this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
			.content(wrongSignatureEnvelope("{\"name\":\"fist\"}"))))
			.hasRootCauseInstanceOf(com.power4j.fist.sde.core.exception.SecureSignatureException.class)
			.hasMessageContaining("secure request signature verification failed");
	}

	static byte[] envelope(String plain) {
		return envelope(plain, "body-strict-v1");
	}

	static byte[] envelope(String plain, String policyId) {
		AesGcmCryptoHandler crypto = new AesGcmCryptoHandler();
		HmacSha256SignatureHandler signatureHandler = new HmacSha256SignatureHandler();
		SecureKey key = new SecureKey("tenant-a", "AES", KEY);
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope("body");
		envelope.setPayload(new String(
				crypto.encrypt(plain.getBytes(StandardCharsets.UTF_8),
						com.power4j.fist.sde.core.crypto.CryptoContext.outbound(SecureScope.BODY, key)),
				StandardCharsets.UTF_8));
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce("nonce-" + System.nanoTime());
		envelope.setKeyRef("tenant-a");
		envelope.setPolicyId(policyId);
		byte[] input = new DefaultSignatureCanonicalizer().canonicalize(envelope,
				SecureExchangeContext.outbound(SecureScope.BODY));
		envelope.setSignature(new String(
				signatureHandler.sign(input,
						com.power4j.fist.sde.core.signature.SignContext.outbound(SecureScope.BODY, key)),
				StandardCharsets.UTF_8));
		return encode(envelope);
	}

	static byte[] missingSignatureEnvelope(String plain) {
		SecureEnvelope envelope = read(envelope(plain));
		envelope.setSignature(null);
		return encode(envelope);
	}

	static byte[] wrongSignatureEnvelope(String plain) {
		SecureEnvelope envelope = read(envelope(plain));
		envelope.setSignature("wrong-signature");
		return encode(envelope);
	}

	private static SecureEnvelope read(byte[] body) {
		return new JacksonSecureEnvelopeCodec().decode(body, SecureEnvelopeContext.defaults());
	}

	private static byte[] encode(SecureEnvelope envelope) {
		return new JacksonSecureEnvelopeCodec().encodeToBytes(envelope, SecureEnvelopeContext.defaults());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestApplication {

		@Bean
		AesGcmCryptoHandler aesGcmCryptoHandler() {
			return new AesGcmCryptoHandler();
		}

		@Bean
		HmacSha256SignatureHandler hmacSha256SignatureHandler() {
			return new HmacSha256SignatureHandler();
		}

		@Bean
		SecureRandomNonceGenerator secureRandomNonceGenerator() {
			return new SecureRandomNonceGenerator();
		}

		@Bean
		InMemoryReplayGuard replayGuard() {
			return new InMemoryReplayGuard(Duration.ofMinutes(5));
		}

		@Bean
		StaticSecureKeyResolver staticSecureKeyResolver() {
			return StaticSecureKeyResolver.symmetric("tenant-a", KEY);
		}

	}

	@RestController
	static class TestController {

		@PostMapping("/sde/echo")
		Map<String, String> echo(@RequestBody Map<String, String> body) {
			return Collections.singletonMap("name", body.get("name"));
		}

		@PostMapping("/sde/string")
		String string(@RequestBody Map<String, String> body) {
			return body.get("name");
		}

		@PostMapping("/sde/bytes")
		byte[] bytes(@RequestBody Map<String, String> body) {
			return body.get("name").getBytes(StandardCharsets.UTF_8);
		}

		@PostMapping("/sde/null")
		String nullBody(@RequestBody Map<String, String> body) {
			return null;
		}

		@PostMapping("/sde/error")
		String error(@RequestBody Map<String, String> body) {
			throw new IllegalArgumentException("bad request");
		}

		@PostMapping(path = "/sde/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
		String upload() {
			return "upload";
		}

		@ExceptionHandler(IllegalArgumentException.class)
		org.springframework.http.ResponseEntity<String> handle(IllegalArgumentException ex) {
			return org.springframework.http.ResponseEntity.badRequest().body(ex.getMessage());
		}

	}

}
