package com.power4j.fist.jasypt.cli;

import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FistJasyptCliApplication} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class FistJasyptCliApplicationTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldEncryptAndDecryptWithMasterKeyFile() throws Exception {
		Path masterKeyFile = writeMasterKey();

		String cipher = run("encrypt", "--master-key-file", masterKeyFile.toString(), "--value", "hmac-secret");
		assertThat(cipher).startsWith("GMENC(v1:");
		assertThat(new GmTextEncryptor().decrypt(cipher, "master-secret")).isEqualTo("hmac-secret");

		String plain = run("decrypt", "--master-key-file", masterKeyFile.toString(), "--value", cipher);
		assertThat(plain).isEqualTo("hmac-secret");
	}

	@Test
	void shouldEncryptAndDecryptWithCustomCipherPrefixAndSuffix() throws Exception {
		Path masterKeyFile = writeMasterKey();

		String cipher = run("encrypt", "--master-key-file", masterKeyFile.toString(), "--value", "hmac-secret",
				"--prefix", "ENC[", "--suffix", "]");
		String plain = run("decrypt", "--master-key-file", masterKeyFile.toString(), "--value", cipher, "--prefix",
				"ENC[", "--suffix", "]");

		assertThat(cipher).startsWith("ENC[v1:");
		assertThat(plain).isEqualTo("hmac-secret");
	}

	@Test
	void shouldGenerateRandomKeys() {
		String key = run("generate-key", "--bytes", "32");

		assertThat(key).matches("[A-Za-z0-9+/=]+");
		assertThat(key).hasSize(44);
	}

	@Test
	void shouldPrintStableFingerprintWithoutPlainKey() throws Exception {
		Path masterKeyFile = writeMasterKey();

		String fingerprint = run("fingerprint", "--master-key-file", masterKeyFile.toString());

		assertThat(fingerprint).startsWith("SM3:");
		assertThat(fingerprint).doesNotContain("master-secret");
		assertThat(run("fingerprint", "--master-key-file", masterKeyFile.toString())).isEqualTo(fingerprint);
	}

	@Test
	void shouldRejectMissingRequiredArgument() {
		assertThatThrownBy(() -> run("encrypt", "--value", "hmac-secret")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("--master-key-file");
	}

	@Test
	void shouldPrintHelp() {
		assertThat(run("help")).contains("Usage:", "Commands:", "encrypt", "decrypt", "generate-key", "fingerprint");
		assertThat(run("--help")).contains("Usage:", "--master-key-file", "--prefix", "--suffix");
	}

	private Path writeMasterKey() throws Exception {
		Path masterKeyFile = this.tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "master-secret", StandardCharsets.UTF_8);
		return masterKeyFile;
	}

	private static String run(String... args) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;
		try {
			System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
			FistJasyptCliApplication.main(args);
			return out.toString(StandardCharsets.UTF_8).trim();
		}
		finally {
			System.setOut(originalOut);
		}
	}

}
