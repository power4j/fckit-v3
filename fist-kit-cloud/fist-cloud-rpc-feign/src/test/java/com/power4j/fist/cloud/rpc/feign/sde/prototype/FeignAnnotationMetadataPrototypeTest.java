package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;
import com.power4j.fist.sde.core.annotation.SecureExchange;
import feign.MethodMetadata;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class FeignAnnotationMetadataPrototypeTest {

	@Test
	void shouldExposeSecureAnnotationMetadataToEncoderTemplate() throws Exception {
		Method method = AnnotationClient.class.getMethod("send", Payload.class);
		MethodMetadata metadata = new SpringMvcContract().parseAndValidateMetadata(AnnotationClient.class, method);

		RequestTemplate template = RequestTemplate.from(metadata.template()).methodMetadata(metadata);

		SecureExchange secureExchange = template.methodMetadata().method().getAnnotation(SecureExchange.class);
		assertThat(secureExchange).isNotNull();
		assertThat(secureExchange.value()).isEqualTo("body-strict-v1");
		assertThat(secureExchange.requestBody()).isEqualTo(SecureInputMode.REQUIRED);
		assertThat(secureExchange.responseBody()).isEqualTo(SecureResponseMode.ENABLED);
		assertThat(template.methodMetadata().bodyIndex()).isZero();
	}

	interface AnnotationClient {

		@SecureExchange(value = "body-strict-v1", requestBody = SecureInputMode.REQUIRED,
				responseBody = SecureResponseMode.ENABLED)
		@PostMapping(path = "/sde/echo")
		Payload send(@RequestBody Payload payload);

	}

	static class Payload {

		private String name;

		String getName() {
			return this.name;
		}

		void setName(String name) {
			this.name = name;
		}

	}

}
