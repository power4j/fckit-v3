package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.annotation.SecureBody;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeContext;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import com.power4j.fist.sde.core.exception.SecureExchangeExceptionTranslator;
import com.power4j.fist.sde.core.signature.DefaultSignatureCanonicalizer;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SdeWebMvcTest.TestApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=body-optional-v1",
				"fist.sde.policies.body-optional-v1.request-body-mode=optional",
				"fist.sde.policies.body-optional-v1.response-body-mode=follow_request" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcOptionalModeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldAcceptPlainBodyInOptionalMode() throws Exception {
		this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"plain\"}"))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"name\":\"plain\"}"));
	}

	@Test
	void shouldWrapResponseOnlyWhenOptionalRequestIsSecure() throws Exception {
		this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
				.content(SdeWebMvcTest.envelope("{\"name\":\"fist\"}", "body-optional-v1")))
			.andExpect(status().isOk())
			.andExpect((result) -> assertThat(result.getResponse().getContentAsString()).contains("\"scope\""));
	}

	@Test
	void shouldRejectEnvelopeWhenPolicyIdDoesNotMatchCurrentPolicy() {
		assertThatThrownBy(() -> this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
			.content(SdeWebMvcTest.envelope("{\"name\":\"fist\"}", "other-policy-v1"))))
			.hasRootCauseInstanceOf(SecureEnvelopeException.class);
	}

}

@SpringBootTest(classes = SdeWebMvcTest.TestApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=body-plain-v1",
				"fist.sde.policies.body-plain-v1.request-body-mode=plain",
				"fist.sde.policies.body-plain-v1.response-body-mode=disabled" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcPlainModeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldRejectSecureEnvelopeInPlainMode() {
		assertThatThrownBy(() -> this.mockMvc.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
			.content(SdeWebMvcTest.envelope("{\"name\":\"fist\"}"))))
			.hasRootCauseInstanceOf(SecureEnvelopeException.class);
	}

}

@SpringBootTest(classes = SdeWebMvcTest.TestApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=response-enabled-v1",
				"fist.sde.policies.response-enabled-v1.request-body-mode=optional",
				"fist.sde.policies.response-enabled-v1.response-body-mode=enabled" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcResponseKeyRefTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldRejectResponseEncryptionWithoutRequestKeyRef() {
		assertThatThrownBy(() -> this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"plain\"}")))
			.hasRootCauseInstanceOf(com.power4j.fist.sde.core.exception.SecureKeyResolveException.class);
	}

}

@SpringBootTest(classes = SdeWebMvcSignOnlyTest.SignOnlyApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=body-sign-only-v1",
				"fist.sde.policies.body-sign-only-v1.request-body-mode=required",
				"fist.sde.policies.body-sign-only-v1.response-body-mode=follow_request",
				"fist.sde.policies.body-sign-only-v1.crypto-enabled=false",
				"fist.sde.policies.body-sign-only-v1.signature-enabled=true" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcSignOnlyTest {

	private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldProcessSignOnlyRequestAndResponseWithoutCryptoHandler() throws Exception {
		String response = this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON)
				.content(signOnlyEnvelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
			.readTree(response);
		assertThat(node.get("scope").asText()).isEqualTo("responseBody");
		assertThat(node.get("data").asText()).isEqualTo("{\"name\":\"fist\"}");
		assertThat(node.get("sign").asText()).isNotBlank();
	}

	private static byte[] signOnlyEnvelope(String plain) {
		HmacSha256SignatureHandler signatureHandler = new HmacSha256SignatureHandler();
		SecureKey key = new SecureKey("tenant-a", "HmacSHA256", KEY);
		SecureEnvelope envelope = new SecureEnvelope();
		envelope.setVersion("1");
		envelope.setScope("body");
		envelope.setPayload(plain);
		envelope.setTimestamp(Instant.now().toString());
		envelope.setNonce("nonce-" + System.nanoTime());
		envelope.setKeyRef("tenant-a");
		envelope.setPolicyId("body-sign-only-v1");
		byte[] input = new DefaultSignatureCanonicalizer().canonicalize(envelope,
				SecureExchangeContext.outbound(SecureScope.BODY));
		envelope.setSignature(new String(
				signatureHandler.sign(input,
						com.power4j.fist.sde.core.signature.SignContext.outbound(SecureScope.BODY, key)),
				StandardCharsets.UTF_8));
		return new JacksonSecureEnvelopeCodec().encodeToBytes(envelope, SecureEnvelopeContext.defaults());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class SignOnlyApplication {

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

}

@SpringBootTest(classes = SdeWebMvcTest.TestApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=body-strict-v1",
				"fist.sde.policies.body-strict-v1.request-body-mode=required",
				"fist.sde.policies.body-strict-v1.response-body-mode=enabled",
				"fist.sde.policies.body-plain-v1.request-body-mode=plain",
				"fist.sde.policies.body-plain-v1.response-body-mode=disabled" })
@AutoConfigureMockMvc
@Import(SdeWebMvcAnnotationModeTest.AnnotationController.class)
class SdeWebMvcAnnotationModeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldUseMethodSecureBodyPolicyBeforeClassSecureExchange() throws Exception {
		this.mockMvc
			.perform(post("/sde/annotation/plain").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"plain\"}"))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"name\":\"plain\"}"));
	}

	@Test
	void shouldRejectMethodAnnotationConflict() {
		assertThatThrownBy(
				() -> this.mockMvc.perform(post("/sde/annotation/conflict").contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"plain\"}")))
			.hasRootCauseInstanceOf(SecureEnvelopeException.class)
			.hasMessageContaining("conflicting SDE annotations");
	}

	@RestController
	@SecureExchange("body-strict-v1")
	static class AnnotationController {

		@PostMapping("/sde/annotation/plain")
		@SecureBody(value = "body-plain-v1", request = SecureInputMode.PLAIN, response = SecureResponseMode.DISABLED)
		Map<String, String> plain(@RequestBody Map<String, String> body) {
			return Collections.singletonMap("name", body.get("name"));
		}

		@PostMapping("/sde/annotation/conflict")
		@SecureBody("body-plain-v1")
		@SecureExchange("body-strict-v1")
		Map<String, String> conflict(@RequestBody Map<String, String> body) {
			return Collections.singletonMap("name", body.get("name"));
		}

	}

}

@SpringBootTest(classes = SdeWebMvcExceptionTranslatorTest.TranslatorApplication.class,
		properties = { "fist.sde.enabled=true", "fist.sde.web.enabled=true",
				"fist.sde.web.default-policy-id=body-strict-v1",
				"fist.sde.policies.body-strict-v1.request-body-mode=required",
				"fist.sde.policies.body-strict-v1.response-body-mode=disabled" })
@AutoConfigureMockMvc
@Import(SdeWebMvcTest.TestController.class)
class SdeWebMvcExceptionTranslatorTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldTranslateSecureExchangeExceptionWhenTranslatorBeanExists() {
		assertThatThrownBy(() -> this.mockMvc
			.perform(post("/sde/echo").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"plain\"}")))
			.hasRootCauseInstanceOf(IllegalStateException.class)
			.hasMessageContaining("translated: request envelope scope must be body");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TranslatorApplication extends SdeWebMvcTest.TestApplication {

		@Bean
		SecureExchangeExceptionTranslator secureExchangeExceptionTranslator() {
			return (exception, context) -> new IllegalStateException("translated: " + exception.getMessage());
		}

	}

}
