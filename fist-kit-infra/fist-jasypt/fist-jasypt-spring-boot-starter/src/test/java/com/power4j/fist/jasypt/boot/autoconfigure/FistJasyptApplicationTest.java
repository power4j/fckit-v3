package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class FistJasyptApplicationTest {

	@Test
	void shouldDecryptPropertyDuringApplicationStartupWithRawMasterKey() {
		String cipher = new GmTextEncryptor().encrypt("hmac-secret", "master-secret");
		SpringApplication application = new SpringApplication(TestApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		try (ConfigurableApplicationContext context = application.run("--fist.jasypt.enabled=true",
				"--fist.jasypt.master-key-source=raw", "--fist.jasypt.master-key-value=master-secret",
				"--fist.jasypt.master-key-file=missing.key", "--demo.hmac-key=" + cipher)) {
			assertThat(context.getBean("decryptedKey", String.class)).isEqualTo("hmac-secret");
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	static class TestApplication {

		@Bean
		String decryptedKey(@Value("${demo.hmac-key}") String value) {
			return value;
		}

	}

}
