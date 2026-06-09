package com.power4j.fist.jasypt.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FistJasyptCliApplication} 分发包测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class FistJasyptCliApplicationIT {

	@TempDir
	Path tempDir;

	@Test
	void shouldRunFromPackagedJarWithoutExternalLibDirectory() throws Exception {
		Path masterKeyFile = tempDir.resolve("master.key");
		Files.writeString(masterKeyFile, "master-secret", StandardCharsets.UTF_8);
		Path isolatedJar = tempDir.resolve("dist").resolve("fist-jasypt-cli.jar");
		Files.createDirectories(isolatedJar.getParent());
		Files.copy(Path.of("target", "fist-jasypt-cli.jar"), isolatedJar);

		Process process = new ProcessBuilder("java", "-jar", isolatedJar.toString(), "fingerprint", "--master-key-file",
				masterKeyFile.toString())
			.redirectErrorStream(true)
			.start();

		assertThat(process.waitFor()).isZero();
		assertThat(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim())
			.startsWith("SM3:");
	}

}
