package com.power4j.fist.jasypt.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FistJasyptEnvironmentPostProcessorTest {

	private final FistJasyptEnvironmentPostProcessor processor = new FistJasyptEnvironmentPostProcessor();

	@Test
	void shouldExposeDefaultCipherPrefixAndSuffixToJasypt() {
		StandardEnvironment environment = new StandardEnvironment();

		this.processor.postProcessEnvironment(environment, new DefaultBootstrapContext());

		assertThat(environment.getProperty("jasypt.encryptor.property.prefix")).isEqualTo("GMENC(");
		assertThat(environment.getProperty("jasypt.encryptor.property.suffix")).isEqualTo(")");
	}

	@Test
	void shouldUseFistCipherPrefixAndSuffix() {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources()
			.addFirst(new MapPropertySource("test",
					Map.of("fist.jasypt.cipher-prefix", "GM[", "fist.jasypt.cipher-suffix", "]")));

		this.processor.postProcessEnvironment(environment, new DefaultBootstrapContext());

		assertThat(environment.getProperty("jasypt.encryptor.property.prefix")).isEqualTo("GM[");
		assertThat(environment.getProperty("jasypt.encryptor.property.suffix")).isEqualTo("]");
	}

}
