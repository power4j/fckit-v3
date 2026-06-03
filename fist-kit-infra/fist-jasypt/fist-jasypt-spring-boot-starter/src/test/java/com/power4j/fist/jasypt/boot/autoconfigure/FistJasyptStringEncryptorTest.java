package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmEncryptedValue;
import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FistJasyptStringEncryptorTest {

	private final FistJasyptStringEncryptor encryptor = new FistJasyptStringEncryptor(new GmTextEncryptor(),
			"master-secret");

	private final FistJasyptStringEncryptor customEncryptor = new FistJasyptStringEncryptor(new GmTextEncryptor(),
			"master-secret", "ENC[", "]");

	@Test
	void shouldDecryptFullEnvelope() {
		String cipher = this.encryptor.encrypt("hmac-secret");

		assertThat(this.encryptor.decrypt(cipher)).isEqualTo("hmac-secret");
	}

	@Test
	void shouldDecryptEnvelopeBodyPassedByJasyptDetector() {
		String cipher = this.encryptor.encrypt("hmac-secret");
		String body = cipher.substring(GmEncryptedValue.PREFIX.length(),
				cipher.length() - GmEncryptedValue.SUFFIX.length());

		assertThat(this.encryptor.decrypt(body)).isEqualTo("hmac-secret");
	}

	@Test
	void shouldUseCustomCipherPrefixAndSuffix() {
		String cipher = this.customEncryptor.encrypt("hmac-secret");
		String body = cipher.substring("ENC[".length(), cipher.length() - "]".length());

		assertThat(cipher).startsWith("ENC[v1:");
		assertThat(this.customEncryptor.decrypt(body)).isEqualTo("hmac-secret");
		assertThat(this.customEncryptor.decrypt(cipher)).isEqualTo("hmac-secret");
	}

}
