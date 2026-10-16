package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Throwables.getStackTrace;

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

	@Test
	void shouldKeepLegacyEnvironmentSourceAndIgnoreRawValue() {
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-env=test.master-key",
					"test.master-key=  legacy-secret  ", "fist.jasypt.master-key-value=ignored")
			.run((context) -> {
				assertThat(context).hasNotFailed();
				String cipher = new GmTextEncryptor().encrypt("hmac-secret", "legacy-secret");
				assertThat(context.getBean(StringEncryptor.class).decrypt(cipher)).isEqualTo("hmac-secret");
			});
	}

	@Test
	void shouldKeepLegacyFilePriorityOverEnvironment() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "  file-secret\n");
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-file=" + masterKeyFile,
					"FIST_JASYPT_MASTER_KEY=ignored", "fist.jasypt.master-key-value=ignored")
			.run((context) -> {
				assertThat(context).hasNotFailed();
				String cipher = new GmTextEncryptor().encrypt("hmac-secret", "file-secret");
				assertThat(context.getBean(StringEncryptor.class).decrypt(cipher)).isEqualTo("hmac-secret");
			});
	}

	@Test
	void shouldDecryptFileCipherWithRawKeyAndIgnoreOtherSources() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "  同一主密钥\n");
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=file",
					"fist.jasypt.master-key-file=" + masterKeyFile, "fist.jasypt.master-key-value=ignored")
			.run((fileContext) -> {
				assertThat(fileContext).hasNotFailed();
				String cipher = fileContext.getBean(StringEncryptor.class).encrypt("hmac-secret");
				this.runner
					.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=raw",
							"fist.jasypt.master-key-value=  同一主密钥  ", "fist.jasypt.master-key-file=missing.key",
							"FIST_JASYPT_MASTER_KEY=ignored")
					.run((rawContext) -> {
						assertThat(rawContext).hasNotFailed();
						assertThat(rawContext.getBean(StringEncryptor.class).decrypt(cipher)).isEqualTo("hmac-secret");
					});
			});
	}

	@Test
	void shouldFailRawWithoutValueEvenWithOtherSources() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "fallback-secret");
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=raw",
					"fist.jasypt.master-key-file=" + masterKeyFile, "FIST_JASYPT_MASTER_KEY=fallback-secret")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasRootCauseMessage("FIST Jasypt raw master key is missing. Set fist.jasypt.master-key-value."));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   " })
	void shouldFailRawWithBlankValue(String value) {
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=raw",
					"fist.jasypt.master-key-value=" + value)
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasRootCauseMessage("FIST Jasypt raw master key is missing. Set fist.jasypt.master-key-value."));
	}

	@Test
	void shouldRejectEncryptedRawMasterKey() {
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=raw",
					"fist.jasypt.master-key-value=GMENC(secret-not-in-error)")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasRootCauseMessage("FIST Jasypt raw master key must not be encrypted with itself.")
				.satisfies((failure) -> assertThat(getStackTrace(failure)).doesNotContain("secret-not-in-error")));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "unknown", "RAW" })
	void shouldRejectInvalidSource(String source) {
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=" + source,
					"fist.jasypt.master-key-value=secret-not-in-error", "FIST_JASYPT_MASTER_KEY=fallback-secret")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasRootCauseMessage("FIST Jasypt master-key-source must be file or raw.")
				.satisfies((failure) -> assertThat(getStackTrace(failure)).doesNotContain("secret-not-in-error",
						"fallback-secret")));
	}

	@Test
	void shouldFailExplicitFileWithoutPathInsteadOfUsingEnvironment() {
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=file",
					"fist.jasypt.master-key-value=fallback-secret", "FIST_JASYPT_MASTER_KEY=fallback-secret")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasRootCauseMessage("FIST Jasypt file master key is missing. Set fist.jasypt.master-key-file."));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "file" })
	void shouldNotFallBackWhenFileCannotBeRead(String source) {
		ApplicationContextRunner configured = this.runner.withPropertyValues("fist.jasypt.enabled=true",
				"fist.jasypt.master-key-file=" + this.tempDir.resolve("missing.key"),
				"fist.jasypt.master-key-value=fallback-secret", "FIST_JASYPT_MASTER_KEY=fallback-secret");
		if (!source.isEmpty()) {
			configured = configured.withPropertyValues("fist.jasypt.master-key-source=" + source);
		}
		configured.run((context) -> assertThat(context).hasFailed()
			.getFailure()
			.hasMessageContaining("master key file cannot be read")
			.satisfies((failure) -> assertThat(getStackTrace(failure)).doesNotContain("fallback-secret")));
	}

	@Test
	void shouldNotFallBackWhenFileIsEmpty() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, " \n");
		this.runner
			.withPropertyValues("fist.jasypt.enabled=true", "fist.jasypt.master-key-source=file",
					"fist.jasypt.master-key-file=" + masterKeyFile, "fist.jasypt.master-key-value=fallback-secret",
					"FIST_JASYPT_MASTER_KEY=fallback-secret")
			.run((context) -> assertThat(context).hasFailed()
				.getFailure()
				.hasMessageContaining("master key file is empty"));
	}

}
