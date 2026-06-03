package com.power4j.fist.jasypt.boot.autoconfigure;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FistJasyptAutoConfiguration} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class FistJasyptAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(FistJasyptAutoConfiguration.class));

	@TempDir
	Path tempDir;

	@Test
	void shouldStayDisabledByDefault() {
		this.runner.run((context) -> assertThat(context).doesNotHaveBean("jasyptStringEncryptor"));
	}

	@Test
	void shouldCreateJasyptStringEncryptorWhenEnabled() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "master-secret");

		this.runner
			.withPropertyValues("fist.jasypt.enabled=true",
					"fist.jasypt.master-key-file=" + masterKeyFile.toAbsolutePath())
			.run((context) -> {
				assertThat(context).hasSingleBean(StringEncryptor.class);
				StringEncryptor encryptor = context.getBean("jasyptStringEncryptor", StringEncryptor.class);
				String cipher = encryptor.encrypt("hmac-secret");
				assertThat(cipher).startsWith("GMENC(v1:");
				assertThat(encryptor.decrypt(cipher)).isEqualTo("hmac-secret");
			});
	}

	@Test
	void shouldUseConfiguredCipherPrefixAndSuffix() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "master-secret");

		this.runner
			.withPropertyValues("fist.jasypt.enabled=true",
					"fist.jasypt.master-key-file=" + masterKeyFile.toAbsolutePath(), "fist.jasypt.cipher-prefix=ENC[",
					"fist.jasypt.cipher-suffix=]")
			.run((context) -> {
				StringEncryptor encryptor = context.getBean("jasyptStringEncryptor", StringEncryptor.class);
				String cipher = encryptor.encrypt("hmac-secret");
				String body = cipher.substring("ENC[".length(), cipher.length() - "]".length());
				assertThat(cipher).startsWith("ENC[v1:");
				assertThat(encryptor.decrypt(body)).isEqualTo("hmac-secret");
			});
	}

	@Test
	void shouldFailWhenEnabledAndMasterKeyMissing() {
		this.runner.withPropertyValues("fist.jasypt.enabled=true")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasMessageContaining("FIST_JASYPT_MASTER_KEY"));
	}

}
