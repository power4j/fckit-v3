package com.power4j.fist.sde.boot.autoconfigure;

import com.power4j.fist.sde.core.exception.SecureEnvelopeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
				.content(SdeWebMvcTest.envelope("{\"name\":\"fist\"}")))
			.andExpect(status().isOk())
			.andExpect((result) -> assertThat(result.getResponse().getContentAsString()).contains("\"scope\""));
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
