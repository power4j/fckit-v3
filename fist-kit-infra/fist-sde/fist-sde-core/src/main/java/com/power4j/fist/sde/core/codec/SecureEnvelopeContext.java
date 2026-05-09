package com.power4j.fist.sde.core.codec;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SecureEnvelopeContext {

	private final String envelopeName;

	private final SecureEnvelopeFieldMapping fieldMapping;

	private final Charset charset;

	private final String mediaType;

	private final Type targetBodyType;

	private final Class<?> selectedConverterType;

	@Builder
	public SecureEnvelopeContext(String envelopeName, @Nullable SecureEnvelopeFieldMapping fieldMapping,
			@Nullable Charset charset, @Nullable String mediaType, @Nullable Type targetBodyType,
			@Nullable Class<?> selectedConverterType) {
		this.envelopeName = envelopeName;
		this.fieldMapping = fieldMapping == null ? SecureEnvelopeFieldMapping.defaults() : fieldMapping;
		this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
		this.mediaType = mediaType;
		this.targetBodyType = targetBodyType;
		this.selectedConverterType = selectedConverterType;
	}

	public static SecureEnvelopeContext defaults() {
		return SecureEnvelopeContext.builder()
			.envelopeName("default")
			.fieldMapping(SecureEnvelopeFieldMapping.defaults())
			.charset(StandardCharsets.UTF_8)
			.mediaType("application/json")
			.build();
	}

	public SecureEnvelopeContext withSelectedConverterType(@Nullable Class<?> selectedConverterType) {
		return SecureEnvelopeContext.builder()
			.envelopeName(this.envelopeName)
			.fieldMapping(this.fieldMapping)
			.charset(this.charset)
			.mediaType(this.mediaType)
			.targetBodyType(this.targetBodyType)
			.selectedConverterType(selectedConverterType)
			.build();
	}

	public String getEnvelopeName() {
		return this.envelopeName;
	}

	public SecureEnvelopeFieldMapping getFieldMapping() {
		return this.fieldMapping;
	}

	public Charset getCharset() {
		return this.charset;
	}

	public @Nullable String getMediaType() {
		return this.mediaType;
	}

	public @Nullable Type getTargetBodyType() {
		return this.targetBodyType;
	}

	public @Nullable Class<?> getSelectedConverterType() {
		return this.selectedConverterType;
	}

}
