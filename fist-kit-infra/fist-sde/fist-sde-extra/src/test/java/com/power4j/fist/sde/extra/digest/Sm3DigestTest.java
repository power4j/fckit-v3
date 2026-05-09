package com.power4j.fist.sde.extra.digest;

import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Sm3DigestTest {

	private final Sm3Digest digest = new Sm3Digest();

	@Test
	void shouldDigestWithSm3WhenProviderExists() {
		ensureBouncyCastle();

		byte[] output = this.digest.digest("abc".getBytes(StandardCharsets.UTF_8));

		assertThat(hex(output)).isEqualTo("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0");
	}

	@Test
	void shouldRejectWhenSm3ProviderMissing() {
		Security.removeProvider("BC");
		try {
			assertThatThrownBy(() -> this.digest.digest("abc".getBytes(StandardCharsets.UTF_8)))
				.isInstanceOf(SecureAlgorithmUnavailableException.class)
				.hasMessageContaining("SM3");
		}
		finally {
			ensureBouncyCastle();
		}
	}

	private static void ensureBouncyCastle() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static String hex(byte[] input) {
		StringBuilder builder = new StringBuilder(input.length * 2);
		for (byte value : input) {
			builder.append(String.format("%02x", value & 0xff));
		}
		return builder.toString();
	}

}
