package com.power4j.fist.jasypt.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link GmTextEncryptor} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class GmTextEncryptorTest {

	private final GmTextEncryptor encryptor = new GmTextEncryptor();

	@Test
	void encryptShouldReturnGmencEnvelopeAndDecryptPlainText() {
		String cipher = this.encryptor.encrypt("hmac-secret", "master-secret");

		assertThat(cipher).startsWith("GMENC(v1:");
		assertThat(this.encryptor.decrypt(cipher, "master-secret")).isEqualTo("hmac-secret");
	}

	@Test
	void decryptShouldRejectTamperedCipher() {
		String cipher = this.encryptor.encrypt("hmac-secret", "master-secret");
		String tampered = cipher.replace("v1:", "v1:AA");

		assertThatThrownBy(() -> this.encryptor.decrypt(tampered, "master-secret"))
			.isInstanceOf(GmConfigCryptoException.class)
			.hasMessageContaining("GMENC");
	}

	@Test
	void decryptShouldRejectWrongMasterKey() {
		String cipher = this.encryptor.encrypt("hmac-secret", "master-secret");

		assertThatThrownBy(() -> this.encryptor.decrypt(cipher, "other-secret"))
			.isInstanceOf(GmConfigCryptoException.class);
	}

	@Test
	void encryptShouldUseRandomSaltAndIv() {
		String first = this.encryptor.encrypt("hmac-secret", "master-secret");
		String second = this.encryptor.encrypt("hmac-secret", "master-secret");

		assertThat(first).isNotEqualTo(second);
		assertThat(first).doesNotContain("hmac-secret");
		assertThat(second).doesNotContain("hmac-secret");
	}

	@Test
	void parseShouldRejectInvalidEnvelopeFormat() {
		assertThatThrownBy(() -> GmEncryptedValue.parse("plain-text")).isInstanceOf(GmConfigCryptoException.class);
		assertThatThrownBy(() -> GmEncryptedValue.parse("GMENC(v2:a:b:c:d)"))
			.isInstanceOf(GmConfigCryptoException.class);
		assertThatThrownBy(() -> GmEncryptedValue.parse("GMENC(v1:a:b:c)")).isInstanceOf(GmConfigCryptoException.class);
		assertThatThrownBy(() -> GmEncryptedValue.parse("GMENC(v1:not-base64:b:c:d)"))
			.isInstanceOf(GmConfigCryptoException.class);
	}

}
