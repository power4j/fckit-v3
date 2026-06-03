package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmEncryptedValue;
import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FistJasyptStringEncryptorTest {

	private final FistJasyptStringEncryptor encryptor = new FistJasyptStringEncryptor(new GmTextEncryptor(),
			"master-secret");

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

}
